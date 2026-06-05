package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * System A is temporarily unavailable (503 or connection failure).
 */
public final class SystemAUnavailableException extends SystemAException {

  public SystemAUnavailableException(Map<String, Object> details) {
    super("System A: unavailable", details);
  }

  public SystemAUnavailableException() {
    this(Map.of());
  }
}
