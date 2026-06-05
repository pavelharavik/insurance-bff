package com.insurance.bff.infrastructure.client.systema;

/**
 * Raw JSON response returned by System A.
 *
 * <pre>
 * {
 *   "id":          "123",
 *   "description": "Patient: John Doe, status: active"
 * }
 * </pre>
 */
public record SystemAResponse(String id, String description) {

}
