package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * System B returned a 5xx server error or sent a response the BFF could not process.
 */
public final class SystemBServerErrorException extends SystemBException {

  public SystemBServerErrorException(Map<String, Object> details) {
    super("System B: server error", details);
  }

  public SystemBServerErrorException() {
    this(Map.of());
  }
}
