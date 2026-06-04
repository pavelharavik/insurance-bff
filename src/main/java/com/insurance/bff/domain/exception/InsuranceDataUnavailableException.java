package com.insurance.bff.domain.exception;

/**
 * Thrown by the service layer when insurance data could not be retrieved from any upstream.
 * Carries the upstream HTTP status code and raw response body for transparent error propagation.
 */
public class InsuranceDataUnavailableException extends RuntimeException {

    private final UpstreamErrorType type;
    private final int statusCode;
    private final String responseBody;

    public InsuranceDataUnavailableException(UpstreamErrorType type, int statusCode, String responseBody) {
        super("Insurance data unavailable: HTTP " + statusCode);
        this.type = type;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public UpstreamErrorType getType() {
        return type;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
