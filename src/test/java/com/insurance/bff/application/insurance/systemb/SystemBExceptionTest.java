package com.insurance.bff.application.insurance.systemb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SystemBExceptionTest {

  @Test
  void notFound_withDetails_setsMessageAndDetails() {
    Map<String, Object> details = Map.of("code", 404);
    var ex = new SystemBException.NotFound(details);

    assertThat(ex.getMessage()).isEqualTo("System B: not found");
    assertThat(ex.getDetails()).isEqualTo(details);
  }

  @Test
  void notFound_noArg_usesEmptyDetails() {
    assertThat(new SystemBException.NotFound().getDetails()).isEmpty();
  }

  @Test
  void unavailable_withDetails_setsMessageAndDetails() {
    Map<String, Object> details = Map.of("code", 503);
    var ex = new SystemBException.Unavailable(details);

    assertThat(ex.getMessage()).isEqualTo("System B: unavailable");
    assertThat(ex.getDetails()).isEqualTo(details);
  }

  @Test
  void unavailable_noArg_usesEmptyDetails() {
    assertThat(new SystemBException.Unavailable().getDetails()).isEmpty();
  }

  @Test
  void clientError_withDetails_setsMessageAndDetails() {
    Map<String, Object> details = Map.of("code", 422);
    var ex = new SystemBException.ClientError(details);

    assertThat(ex.getMessage()).isEqualTo("System B: client error");
    assertThat(ex.getDetails()).isEqualTo(details);
  }

  @Test
  void clientError_noArg_usesEmptyDetails() {
    assertThat(new SystemBException.ClientError().getDetails()).isEmpty();
  }

  @Test
  void serverError_withDetails_setsMessageAndDetails() {
    Map<String, Object> details = Map.of("error", "timeout");
    var ex = new SystemBException.ServerError(details);

    assertThat(ex.getMessage()).isEqualTo("System B: server error");
    assertThat(ex.getDetails()).isEqualTo(details);
  }

  @Test
  void serverError_noArg_usesEmptyDetails() {
    assertThat(new SystemBException.ServerError().getDetails()).isEmpty();
  }
}
