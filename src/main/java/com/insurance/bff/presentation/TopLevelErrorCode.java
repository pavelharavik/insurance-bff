package com.insurance.bff.presentation;

/**
 * Top-level error codes included in {@link ErrorResponse#errorCode()}.
 */
public enum TopLevelErrorCode {
  NOT_FOUND,
  SERVICE_UNAVAILABLE,
  UPSTREAM_ERROR,
  VALIDATION_ERROR
}
