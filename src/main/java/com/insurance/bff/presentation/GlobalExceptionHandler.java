package com.insurance.bff.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * Renders {@link ApiException} and validation errors as {@link ErrorResponse} JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handle(ApiException ex) {
    return ResponseEntity.status(ex.getStatus()).body(ex.toErrorResponse());
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ErrorResponse> handle(WebExchangeBindException ex) {
    List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> new ErrorResponse.FieldError(
            toFieldErrorCode(fe.getCodes()),
            fe.getDefaultMessage(),
            fe.getField()))
        .toList();
    ErrorResponse body = new ErrorResponse(
        TopLevelErrorCode.VALIDATION_ERROR, "Request validation failed", fieldErrors);
    return ResponseEntity.badRequest().body(body);
  }

  private static FieldErrorCode toFieldErrorCode(String[] codes) {
    String code = codes != null && codes.length > 0 ? codes[codes.length - 1] : "";
    return switch (code) {
      case "NotBlank", "NotNull", "NotEmpty" -> FieldErrorCode.REQUIRED;
      case "Size", "Max" -> FieldErrorCode.TOO_LONG;
      case "Pattern", "Email" -> FieldErrorCode.INVALID_FORMAT;
      default -> FieldErrorCode.INVALID_VALUE;
    };
  }
}
