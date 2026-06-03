package com.insurance.bff.service;

import com.insurance.bff.client.InsuranceClient;
import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.exception.UpstreamServiceException;
import com.insurance.bff.model.InsuranceData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.StructuredTaskScope;

/**
 * Orchestrates parallel insurance lookups across System A and System B.
 *
 * <p>Both upstreams are queried concurrently via virtual threads. The first
 * successful result wins. If both fail, the error with the highest priority is
 * re-thrown: 500 &gt; 503 &gt; 404.
 */
@Service
public class InsuranceService {

    private final InsuranceClient clientA;
    private final InsuranceClient clientB;

    public InsuranceService(
            @Qualifier("systemAClient") InsuranceClient clientA,
            @Qualifier("systemBClient") InsuranceClient clientB) {
        this.clientA = clientA;
        this.clientB = clientB;
    }

    /**
     * @param patientId the patient ID to look up
     * @return normalised insurance data from whichever upstream responds first with 200
     * @throws InsuranceNotFoundException if both upstreams return 404
     * @throws UpstreamServiceException   with the highest-priority status code if both fail
     */
    @Cacheable("insurance")
    public InsuranceData getInsuranceData(String patientId) {
        List<RuntimeException> errors = new CopyOnWriteArrayList<>();
        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<InsuranceData>anySuccessfulResultOrThrow())) {

            scope.fork(() -> fetchCapturing(clientA, patientId, errors));
            scope.fork(() -> fetchCapturing(clientB, patientId, errors));

            return scope.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // 503 signals a transient, retryable failure — appropriate for a cancelled scope
            throw new UpstreamServiceException(503, e);
        } catch (Exception e) {
            if (errors.size() == 2) throw selectByPriority(errors.get(0), errors.get(1));
            if (!errors.isEmpty())  throw errors.get(0);
            throw new UpstreamServiceException(503, e);
        }
    }

    private RuntimeException selectByPriority(RuntimeException errorA, RuntimeException errorB) {
        return priority(errorA) >= priority(errorB) ? errorA : errorB;
    }

    private int priority(RuntimeException e) {
        int code = switch (e) {
            case UpstreamServiceException use -> use.getStatusCode();
            case InsuranceNotFoundException  ignored -> 404;
            default                          -> 500;
        };
        return switch (code) {
            case 404 -> 1;
            case 503 -> 2;
            default  -> 3;
        };
    }

    private InsuranceData fetchCapturing(InsuranceClient client, String patientId,
                                          List<RuntimeException> errors) {
        try {
            return client.fetchById(patientId);
        } catch (RuntimeException e) {
            errors.add(e);
            throw e;
        }
    }

}
