package com.insurance.bff.application;

import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import com.insurance.bff.application.port.SystemAClientPort;
import com.insurance.bff.application.port.SystemBClientPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Orchestrates parallel insurance lookups across System A and System B.
 *
 * <p>Both upstreams are subscribed to concurrently. The first successful result wins.
 * If both fail, the error with the highest priority is propagated:
 * {@link UpstreamErrorType#ERROR} &gt; {@link UpstreamErrorType#UNAVAILABLE} &gt; {@link UpstreamErrorType#NOT_FOUND}.
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
        RuntimeException winner = priority(getType(errorA)) >= priority(getType(errorB)) ? errorA : errorB;
        return getType(winner) == UpstreamErrorType.NOT_FOUND
                ? new InsuranceNotFoundException(patientId)
                : winner;
    }

    private UpstreamErrorType getType(RuntimeException e) {
        return switch (e) {
            case InsuranceDataUnavailableException ex -> ex.getType();
            case InsuranceNotFoundException ignored  -> UpstreamErrorType.NOT_FOUND;
            default                                  -> UpstreamErrorType.ERROR;
        };
    }

    private int priority(UpstreamErrorType type) {
        return switch (type) {
            case NOT_FOUND    -> 1;
            case UNAVAILABLE  -> 2;
            case CLIENT_ERROR -> 3;
            case ERROR        -> 4;
        };
    }
}
