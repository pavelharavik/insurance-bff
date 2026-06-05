package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * System A returned a 5xx server error or sent a response the BFF could not process.
 */
public final class SystemAServerErrorException extends SystemAException {

  public SystemAServerErrorException(Map<String, Object> details) {
    super("System A: server error", details);
  }

  public SystemAServerErrorException() {
    this(Map.of());
  }
}
