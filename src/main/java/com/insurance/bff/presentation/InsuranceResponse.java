package com.insurance.bff.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insurance.bff.domain.model.InsuranceData;

import java.time.Instant;

/**
 * Unified HTTP response body returned to BFF callers.
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

    public static InsuranceResponse from(InsuranceData data) {
        return new InsuranceResponse(data.id(), data.name(), Instant.now(), data.active());
    }
}
