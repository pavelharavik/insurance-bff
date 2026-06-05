package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * Base sealed exception for System A upstream failures.
 * Each subtype represents a distinct failure category.
 * {@code details} carries the parsed JSON body returned by System A, or internal context
 * (e.g. a malformed field value) when the failure originates inside the BFF adapter.
 */
public abstract sealed class SystemAException extends RuntimeException
        permits SystemANotFoundException, SystemAUnavailableException,
                SystemAClientErrorException, SystemAServerErrorException {

    private final Map<String, Object> details;

    protected SystemAException(String message, Map<String, Object> details) {
        super(message);
        this.details = details != null ? details : Map.of();
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
