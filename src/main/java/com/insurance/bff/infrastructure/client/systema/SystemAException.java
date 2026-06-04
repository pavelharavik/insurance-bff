package com.insurance.bff.infrastructure.client.systema;

import com.insurance.bff.domain.exception.UpstreamErrorType;

/**
 * Internal exception thrown by {@link SystemAClient} before translation to domain exceptions.
 * Never propagates beyond the infrastructure layer.
 */
class SystemAException extends RuntimeException {

    private final UpstreamErrorType type;
    private final int statusCode;
    private final String responseBody;

    SystemAException(UpstreamErrorType type, int statusCode, String responseBody) {
        super("System A error: HTTP " + statusCode);
        this.type = type;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    UpstreamErrorType getType() {
        return type;
    }

    int getStatusCode() {
        return statusCode;
    }

    String getResponseBody() {
        return responseBody;
    }
}
