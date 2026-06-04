package com.insurance.bff.domain.model;

/**
 * Internal domain representation of a patient's insurance record.
 *
 * @param id     patient identifier
 * @param name   patient full name
 * @param active whether the insurance policy is currently active
 */
public record InsuranceData(String id, String name, boolean active) {}
