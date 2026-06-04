package com.insurance.bff.domain.exception;

/**
 * Semantic classification of upstream failures, ordered by priority (lowest to highest).
 * Used by the service layer to select the most significant error when both upstreams fail.
 */
public enum UpstreamErrorType {
    NOT_FOUND,
    UNAVAILABLE,
    ERROR
}
