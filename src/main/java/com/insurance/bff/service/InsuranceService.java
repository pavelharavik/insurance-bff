package com.insurance.bff.service;

import com.insurance.bff.client.InsuranceClient;
import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.exception.UpstreamServiceException;
import com.insurance.bff.model.InsuranceData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Orchestrates parallel insurance lookups across System A and System B.
 *
 * <p>Both upstreams are subscribed to concurrently. The first successful result
 * wins. If both fail, the error with the highest priority is propagated:
 * 500 &gt; 503 &gt; 404.
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
     * @return a {@link Mono} emitting data from whichever upstream responds first with 200
     */
    public Mono<InsuranceData> getInsuranceData(String patientId) {
        Mono<InsuranceData> monoA = clientA.fetchById(patientId).cache();
        Mono<InsuranceData> monoB = clientB.fetchById(patientId).cache();

        return Mono.firstWithValue(monoA, monoB)
                .onErrorResume(e -> captureError(monoA).zipWith(captureError(monoB))
                        .flatMap(t -> Mono.error(selectByPriority(t.getT1(), t.getT2()))));
    }

    /**
     * Returns the error from a cached Mono that is known to have failed.
     * If the Mono succeeds (should not happen in the error-recovery path), returns empty.
     */
    private Mono<RuntimeException> captureError(Mono<InsuranceData> mono) {
        return mono
                .flatMap(ignored -> Mono.<RuntimeException>empty())
                .onErrorResume(e -> Mono.just((RuntimeException) e));
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
}
