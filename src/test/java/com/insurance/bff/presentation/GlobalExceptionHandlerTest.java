package com.insurance.bff.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.bff.presentation.exception.HttpException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handle_returnsProblemDetailWithCorrectStatus() {
    var ex = new HttpException(503, "Service unavailable", null);

    ResponseEntity<ProblemDetail> response = handler.handle(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(503);
    assertThat(response.getBody().getDetail()).isEqualTo("Service unavailable");
  }

  @Test
  void handle_includesUpstreamBodyProperty_whenNotNull() {
    var ex = new HttpException(502, "Gateway error", "{\"error\":\"timeout\"}");

    ResponseEntity<ProblemDetail> response = handler.handle(ex);

    assertThat(response.getBody().getProperties())
        .containsEntry("upstream_body", "{\"error\":\"timeout\"}");
  }

  @Test
  void handle_omitsUpstreamBodyProperty_whenNull() {
    var ex = new HttpException(404, "Not found", null);

    ResponseEntity<ProblemDetail> response = handler.handle(ex);

    var props = response.getBody().getProperties();
    assertThat(props == null || !props.containsKey("upstream_body")).isTrue();
  }
}
