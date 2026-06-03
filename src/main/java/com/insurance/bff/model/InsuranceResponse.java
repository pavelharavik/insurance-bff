package com.insurance.bff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Unified HTTP response body returned to BFF callers.
 *
 * <p>Field names are snake_case as required by the API contract.
 * {@code current_date} is always set to the BFF's current UTC time at the
 * moment the response is built — it is not sourced from any upstream system.
 *
 * @param id          patient identifier
 * @param name        patient full name
 * @param currentDate UTC timestamp at which the BFF assembled this response
 * @param active      whether the insurance policy is currently active
 */
public record InsuranceResponse(
        String id,
        String name,
        @JsonProperty("current_date") Instant currentDate,
        @JsonProperty("is_active")    boolean active
) {

    /**
     * Converts the internal {@link InsuranceData} domain record to the API response,
     * stamping {@code current_date} with the BFF's current UTC time.
     *
     * @param data internal domain record produced by an upstream client adapter
     * @return response ready for serialisation
     */
    public static InsuranceResponse from(InsuranceData data) {
        return new InsuranceResponse(
                data.id(),
                data.name(),
                Instant.now(),
                data.active()
        );
    }
}
