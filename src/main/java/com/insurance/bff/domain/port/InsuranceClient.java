package com.insurance.bff.domain.port;

import com.insurance.bff.domain.model.InsuranceData;
import reactor.core.publisher.Mono;

/**
 * Output port for fetching a patient's insurance record from an upstream system.
 */
public interface InsuranceClient {

    /**
     * Retrieves and normalises the insurance record for the given patient.
     *
     * @param patientId the patient identifier to look up
     * @return a {@link Mono} emitting normalised insurance data on success,
     *         or terminating with {@link InsuranceNotFoundException} if the patient
     *         does not exist, or {@link InsuranceDataUnavailableException} on any
     *         other upstream error or timeout
     */
    Mono<InsuranceData> fetchById(String patientId);
}
