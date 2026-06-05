package com.insurance.bff.domain.exception;

/**
 * Semantic classification of upstream failures, ordered by priority (lowest to highest).
 * Used by the service layer to select the most significant error when both upstreams fail.
 *
 * <ul>
 *   <li>{@link #NOT_FOUND}    — 404: patient does not exist in this upstream</li>
 *   <li>{@link #UNAVAILABLE}  — 503/timeout: transient infrastructure failure, retry may help</li>
 *   <li>{@link #CLIENT_ERROR} — 4xx (excl. 404): BFF sent an invalid request; indicates an integration bug</li>
 *   <li>{@link #ERROR}        — 5xx (excl. 503): upstream server error</li>
 * </ul>
 */
public enum UpstreamErrorType {
    NOT_FOUND,
    UNAVAILABLE,
    CLIENT_ERROR,
    ERROR
}
