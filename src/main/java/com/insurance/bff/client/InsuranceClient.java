package com.insurance.bff.client;

import com.insurance.bff.model.InsuranceData;

/**
 * Contract for fetching a patient's insurance record from an upstream system.
 * Each upstream system provides its own implementation.
 */
public interface InsuranceClient {

    /**
     * Retrieves and normalises the insurance record for the given patient.
     *
     * @param patientId the patient identifier to look up
     * @return normalised insurance data
     * @throws com.insurance.bff.exception.InsuranceNotFoundException if the upstream returns 404
     * @throws com.insurance.bff.exception.UpstreamServiceException   on any other upstream error or timeout
     */
    InsuranceData fetchById(String patientId);
}
