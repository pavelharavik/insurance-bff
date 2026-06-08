package com.insurance.bff.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.support.WebExchangeBindException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handle_apiException_returnsErrorResponseWithCorrectStatus() {
    var ex = new ApiException(HttpStatus.SERVICE_UNAVAILABLE, TopLevelErrorCode.SERVICE_UNAVAILABLE,
        "Service unavailable");

    ResponseEntity<ErrorResponse> response = handler.handle(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(503);
    assertThat(response.getBody().errorCode()).isEqualTo(TopLevelErrorCode.SERVICE_UNAVAILABLE);
    assertThat(response.getBody().message()).isEqualTo("Service unavailable");
    assertThat(response.getBody().errors()).isEmpty();
  }

  @Test
  void handle_apiException_notFound_returnsEmptyErrors() {
    var ex = new ApiException(HttpStatus.NOT_FOUND, TopLevelErrorCode.NOT_FOUND, "Not found");

    ResponseEntity<ErrorResponse> response = handler.handle(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(response.getBody().errorCode()).isEqualTo(TopLevelErrorCode.NOT_FOUND);
    assertThat(response.getBody().errors()).isEmpty();
  }

  @Test
  void handle_apiException_withFieldErrors_propagatesFieldErrors() {
    var fieldErrors = List.of(
        new ErrorResponse.FieldError(FieldErrorCode.REQUIRED, "must not be blank", "firstName"));
    var ex = new ApiException(HttpStatus.BAD_REQUEST, TopLevelErrorCode.VALIDATION_ERROR,
        "Validation failed", fieldErrors);

    ResponseEntity<ErrorResponse> response = handler.handle(ex);

    assertThat(response.getBody().errors()).hasSize(1);
    assertThat(response.getBody().errors().get(0).field()).isEqualTo("firstName");
    assertThat(response.getBody().errors().get(0).errorCode()).isEqualTo(FieldErrorCode.REQUIRED);
  }

  @Test
  void handle_webExchangeBindException_returns400WithValidationErrors() {
    var springFieldError = new org.springframework.validation.FieldError(
        "request", "firstName", null, false,
        new String[]{"NotBlank.request.firstName", "NotBlank.firstName", "NotBlank"},
        null, "must not be blank");
    var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(springFieldError);
    var ex = new WebExchangeBindException(null, bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handle(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody().errorCode()).isEqualTo(TopLevelErrorCode.VALIDATION_ERROR);
    assertThat(response.getBody().message()).isEqualTo("Request validation failed");
    assertThat(response.getBody().errors()).hasSize(1);
    assertThat(response.getBody().errors().get(0).field()).isEqualTo("firstName");
    assertThat(response.getBody().errors().get(0).errorCode()).isEqualTo(FieldErrorCode.REQUIRED);
  }

  @Test
  void handle_webExchangeBindException_mapsSizeCodeToTooLong() {
    var springFieldError = new org.springframework.validation.FieldError(
        "request", "lastName", null, false,
        new String[]{"Size.request.lastName", "Size.lastName", "Size.java.lang.String", "Size"},
        null, "size must be between 0 and 100");
    var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(springFieldError);
    var ex = new WebExchangeBindException(null, bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handle(ex);

    assertThat(response.getBody().errors().get(0).errorCode()).isEqualTo(FieldErrorCode.TOO_LONG);
  }

  @Test
  void handle_webExchangeBindException_mapsPastCodeToInvalidValue() {
    var springFieldError = new org.springframework.validation.FieldError(
        "request", "birthDate", null, false,
        new String[]{"Past.request.birthDate", "Past.birthDate", "Past.java.time.LocalDate",
            "Past"},
        null, "must be a past date");
    var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(springFieldError);
    var ex = new WebExchangeBindException(null, bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handle(ex);

    assertThat(response.getBody().errors().get(0).errorCode())
        .isEqualTo(FieldErrorCode.INVALID_VALUE);
  }
}