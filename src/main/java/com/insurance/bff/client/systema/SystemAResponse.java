package com.insurance.bff.client.systema;

/**
 * Raw JSON response returned by System A.
 *
 * <pre>
 * {
 *   "id":          "123",
 *   "description": "Patient: John Doe, status: active"
 * }
 * </pre>
 *
 * The {@code description} field encodes both the patient name and policy status
 * as a human-readable string. The mapper is responsible for parsing it.
 */
public record SystemAResponse(String id, String description) {}
