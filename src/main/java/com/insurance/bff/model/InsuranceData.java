package com.insurance.bff.model;

/**
 * Internal domain representation of a patient's insurance record.
 *
 * <p>This is the canonical model passed between the service layer and the
 * upstream client adapters. It is upstream-agnostic — both System A and
 * System B mappers normalise their own response formats into this record.
 *
 * @param id     patient identifier
 * @param name   patient full name
 * @param active whether the insurance policy is currently active
 */
public record InsuranceData(
        String id,
        String name,
        boolean active
) {}
