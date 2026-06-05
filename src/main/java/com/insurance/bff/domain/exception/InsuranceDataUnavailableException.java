package com.insurance.bff.domain.exception;

/**
 * Thrown by the service layer when insurance data could not be retrieved from any upstream.
 * Carries a human-readable message and the {@link UpstreamErrorType} for HTTP status mapping
 * by the presentation layer.
 */
public class InsuranceDataUnavailableException extends RuntimeException {

    private final UpstreamErrorType type;

    public InsuranceDataUnavailableException(UpstreamErrorType type) {
        super(messageFor(type));
        this.type = type;
    }

    /**
     * @param detail additional context extracted from the upstream error response,
     *               appended to the base message
     */
    public InsuranceDataUnavailableException(UpstreamErrorType type, String detail) {
        super(detail != null && !detail.isBlank()
                ? messageFor(type) + ": " + detail
                : messageFor(type));
        this.type = type;
    }

    public UpstreamErrorType getType() {
        return type;
    }

    private static String messageFor(UpstreamErrorType type) {
        return switch (type) {
            case UNAVAILABLE  -> "Insurance data is temporarily unavailable";
            case CLIENT_ERROR -> "Insurance data request was rejected by the upstream service";
            case ERROR        -> "An upstream service error prevented insurance data retrieval";
        };
    }
}
