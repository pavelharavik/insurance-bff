package com.insurance.bff.presentation;

/**
 * Field-level error codes included in {@link ErrorResponse.FieldError#errorCode()}.
 */
public enum FieldErrorCode {
  REQUIRED,
  TOO_LONG,
  INVALID_FORMAT,
  INVALID_VALUE
}
