package com.insurance.bff.application.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SystemAExceptionTest {

  @Test
  void notFound_withDetails_setsMessageAndDetails() {
    Map<String, Object> details = Map.of("code", 404);
    var ex = new SystemAException.NotFound(details);

    assertThat(ex.getMessage()).isEqualTo("System A: not found");
    assertThat(ex.getDetails()).isEqualTo(details);
  }

  @Test
  void notFound_noArg_usesEmptyDetails() {
    var ex = new SystemAException.NotFound();

    assertThat(ex.getDetails()).isEmpty();
  }

  @Test
  void unavailable_withDetails_setsMessageAndDetails() {
    Map<String, Object> details = Map.of("code", 503);
    var ex = new SystemAException.Unavailable(details);

    assertThat(ex.getMessage()).isEqualTo("System A: unavailable");
    assertThat(ex.getDetails()).isEqualTo(details);
  }

  @Test
  void unavailable_noArg_usesEmptyDetails() {
    assertThat(new SystemAException.Unavailable().getDetails()).isEmpty();
  }

  @Test
  void clientError_withDetails_setsMessageAndDetails() {
    Map<String, Object> details = Map.of("code", 422);
    var ex = new SystemAException.ClientError(details);

    assertThat(ex.getMessage()).isEqualTo("System A: client error");
    assertThat(ex.getDetails()).isEqualTo(details);
  }

  @Test
  void clientError_noArg_usesEmptyDetails() {
    assertThat(new SystemAException.ClientError().getDetails()).isEmpty();
  }

  @Test
  void serverError_withDetails_setsMessageAndDetails() {
    Map<String, Object> details = Map.of("error", "timeout");
    var ex = new SystemAException.ServerError(details);

    assertThat(ex.getMessage()).isEqualTo("System A: server error");
    assertThat(ex.getDetails()).isEqualTo(details);
  }

  @Test
  void serverError_noArg_usesEmptyDetails() {
    assertThat(new SystemAException.ServerError().getDetails()).isEmpty();
  }
}
