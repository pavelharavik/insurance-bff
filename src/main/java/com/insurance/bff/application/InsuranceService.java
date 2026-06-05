package com.insurance.bff.application;

import com.insurance.bff.application.exception.*;
import com.insurance.bff.application.port.SystemAClientPort;
import com.insurance.bff.application.port.SystemBClientPort;
import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Orchestrates parallel insurance lookups across System A and System B.
 *
 * <p>Both upstreams are subscribed to concurrently. The first successful result wins.
 * If both fail, the error with the highest priority is propagated:
 * {@link UpstreamErrorType#ERROR} &gt; {@link UpstreamErrorType#CLIENT_ERROR} &gt;
 * {@link UpstreamErrorType#UNAVAILABLE} &gt; not found.
 */
@Service
public class InsuranceService {

    private final SystemAClientPort clientA;
    private final SystemBClientPort clientB;

    public InsuranceService(SystemAClientPort clientA, SystemBClientPort clientB) {
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
                        .flatMap(t -> Mono.error(selectByPriority(t.getT1(), t.getT2(), patientId))));
    }

    private Mono<RuntimeException> captureError(Mono<InsuranceData> mono) {
        return mono
                .flatMap(ignored -> Mono.<RuntimeException>empty())
                .onErrorResume(e -> Mono.just((RuntimeException) e));
    }

    private RuntimeException selectByPriority(RuntimeException errorA, RuntimeException errorB, String patientId) {
        RuntimeException winner = priority(errorA) >= priority(errorB) ? errorA : errorB;
        String detail = extractDetail(winner);
        return switch (winner) {
            case SystemANotFoundException _, SystemBNotFoundException _ ->
                    new InsuranceNotFoundException(patientId);
            case SystemAUnavailableException _, SystemBUnavailableException _ ->
                    new InsuranceDataUnavailableException(UpstreamErrorType.UNAVAILABLE, detail);
            case SystemAClientErrorException _, SystemBClientErrorException _ ->
                    new InsuranceDataUnavailableException(UpstreamErrorType.CLIENT_ERROR, detail);
            case SystemAServerErrorException _, SystemBServerErrorException _ ->
                    new InsuranceDataUnavailableException(UpstreamErrorType.ERROR, detail);
            default ->
                    new InsuranceDataUnavailableException(UpstreamErrorType.ERROR, detail);
        };
    }

    private static String extractDetail(RuntimeException e) {
        Map<String, Object> details = switch (e) {
            case SystemAException ex -> ex.getDetails();
            case SystemBException ex -> ex.getDetails();
            default -> Map.of();
        };
        return details.isEmpty() ? null : details.toString();
    }

    private int priority(RuntimeException e) {
        return switch (e) {
            case SystemANotFoundException _, SystemBNotFoundException _ -> 1;
            case SystemAUnavailableException _, SystemBUnavailableException _ -> 2;
            case SystemAClientErrorException _, SystemBClientErrorException _ -> 3;
            case SystemAServerErrorException _, SystemBServerErrorException _ -> 4;
            default -> 0;
        };
    }
}
