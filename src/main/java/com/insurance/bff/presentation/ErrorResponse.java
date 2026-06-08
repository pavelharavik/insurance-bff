package com.insurance.bff.presentation;

import java.util.List;

/**
 * Unified error response body returned by all error handlers.
 *
 * <p>For validation errors the {@code errors} list contains one entry per invalid field.
 * For all other errors the list is empty.
 *
 * @param errorCode top-level error classification
 * @param message   human-readable description
 * @param errors    per-field validation errors; empty for non-validation failures
 */
public record ErrorResponse(TopLevelErrorCode errorCode, String message, List<FieldError> errors) {

  /**
   * Describes a single field-level validation failure.
   *
   * @param errorCode violation type
   * @param message   human-readable description of this specific violation
   * @param field     name of the offending request field
   */
  public record FieldError(FieldErrorCode errorCode, String message, String field) {

  }

  public static ErrorResponse of(TopLevelErrorCode errorCode, String message) {
    return new ErrorResponse(errorCode, message, List.of());
  }
}
