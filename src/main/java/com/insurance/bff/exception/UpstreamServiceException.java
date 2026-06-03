package com.insurance.bff.exception;

/**
 * Thrown when an upstream system returns an error response or is unreachable.
 * Carries the HTTP status code to be used by the error-priority logic in the service layer.
 */
public class UpstreamServiceException extends RuntimeException {

    private final int statusCode;

    /**
     * @param statusCode HTTP status code returned by (or synthesised for) the upstream
     */
    public UpstreamServiceException(int statusCode) {
        super("Upstream service error: HTTP " + statusCode);
        this.statusCode = statusCode;
    }

    /**
     * @param statusCode HTTP status code returned by (or synthesised for) the upstream
     * @param message    detail message (e.g. malformed response description)
     */
    public UpstreamServiceException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * @param statusCode HTTP status code returned by (or synthesised for) the upstream
     * @param cause      the underlying exception (e.g. a timeout)
     */
    public UpstreamServiceException(int statusCode, Throwable cause) {
        super("Upstream service error: HTTP " + statusCode, cause);
        this.statusCode = statusCode;
    }

    /** @return the HTTP status code associated with this failure */
    public int getStatusCode() {
        return statusCode;
    }
}
