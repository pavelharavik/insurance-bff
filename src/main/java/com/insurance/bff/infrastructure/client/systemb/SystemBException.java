package com.insurance.bff.infrastructure.client.systemb;

import com.insurance.bff.domain.exception.UpstreamErrorType;

/**
 * Internal exception thrown by {@link SystemBClient} before translation to domain exceptions.
 * Never propagates beyond the infrastructure layer.
 */
class SystemBException extends RuntimeException {

    private final UpstreamErrorType type;
    private final int statusCode;
    private final String responseBody;

    SystemBException(UpstreamErrorType type, int statusCode, String responseBody) {
        super("System B error: HTTP " + statusCode);
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
