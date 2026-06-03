package com.insurance.bff.client;

import com.insurance.bff.model.InsuranceData;
import reactor.core.publisher.Mono;

/**
 * Contract for fetching a patient's insurance record from an upstream system.
 * Each upstream system provides its own implementation.
 */
public interface InsuranceClient {

    /**
     * Retrieves and normalises the insurance record for the given patient.
     *
     * @param patientId the patient identifier to look up
     * @return a {@link Mono} emitting normalised insurance data on success,
     *         or terminating with {@link com.insurance.bff.exception.InsuranceNotFoundException}
     *         if the upstream returns 404, or {@link com.insurance.bff.exception.UpstreamServiceException}
     *         on any other upstream error or timeout
     */
    Mono<InsuranceData> fetchById(String patientId);
}
