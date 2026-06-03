package com.insurance.bff.exception;

/**
 * Thrown when no insurance record is found for the requested patient ID.
 * Maps to HTTP 404 at the controller boundary.
 */
public class InsuranceNotFoundException extends RuntimeException {

    private final String patientId;

    /**
     * @param patientId the ID for which no insurance record was found
     */
    public InsuranceNotFoundException(String patientId) {
        super("No insurance record found for patient: " + patientId);
        this.patientId = patientId;
    }

    /** @return the patient ID that was not found */
    public String getPatientId() {
        return patientId;
    }
}
