package com.insurance.bff.domain.exception;

/**
 * Thrown when no insurance record is found for the requested patient ID.
 */
public class InsuranceNotFoundException extends RuntimeException {

    private final String patientId;

    public InsuranceNotFoundException(String patientId) {
        super("No insurance record found for patient: " + patientId);
        this.patientId = patientId;
    }

    public String getPatientId() {
        return patientId;
    }
}
