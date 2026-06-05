package com.insurance.bff.domain.exception;

/**
 * Semantic classification of upstream failures used by {@link InsuranceDataUnavailableException}.
 * Ordered by priority (lowest to highest).
 *
 * <ul>
 *   <li>{@link #UNAVAILABLE}  — 503/timeout: transient failure, retry may help</li>
 *   <li>{@link #CLIENT_ERROR} — 4xx (excl. 404): integration bug on BFF side</li>
 *   <li>{@link #ERROR}        — 5xx (excl. 503): upstream server error</li>
 * </ul>
 */
public enum UpstreamErrorType {
    UNAVAILABLE,
    CLIENT_ERROR,
    ERROR
}
