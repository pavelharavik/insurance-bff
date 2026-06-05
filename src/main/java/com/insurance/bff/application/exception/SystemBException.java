package com.insurance.bff.application.exception;

import com.insurance.bff.domain.exception.UpstreamErrorType;

/**
 * Thrown by the System B adapter to signal an upstream failure before the service
 * applies error-priority logic and produces a business exception.
 */
public class SystemBException extends RuntimeException {

    private final UpstreamErrorType type;
    private final int statusCode;
    private final String responseBody;

    public SystemBException(UpstreamErrorType type, int statusCode, String responseBody) {
        super("System B error: HTTP " + statusCode);
        this.type = type;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public UpstreamErrorType getType() { return type; }
    public int getStatusCode() { return statusCode; }
    public String getResponseBody() { return responseBody; }
}
