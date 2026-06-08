package com.insurance.bff.presentation;

import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Presentation-layer exception carrying HTTP status, error code, and optional field errors.
 * Replaces {@code HttpException}. Created in controllers from domain exceptions and handled by
 * {@link GlobalExceptionHandler}, which renders it as an {@link ErrorResponse}.
 */
public class ApiException extends RuntimeException {

  private final HttpStatus status;
  private final TopLevelErrorCode errorCode;
  private final List<ErrorResponse.FieldError> fieldErrors;

  public ApiException(HttpStatus status, TopLevelErrorCode errorCode, String message) {
    this(status, errorCode, message, List.of());
  }

  public ApiException(HttpStatus status, TopLevelErrorCode errorCode, String message,
      List<ErrorResponse.FieldError> fieldErrors) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
    this.fieldErrors = fieldErrors;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public TopLevelErrorCode getErrorCode() {
    return errorCode;
  }

  public List<ErrorResponse.FieldError> getFieldErrors() {
    return fieldErrors;
  }

  public ErrorResponse toErrorResponse() {
    return new ErrorResponse(errorCode, getMessage(), fieldErrors);
  }
}
