package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * System B is temporarily unavailable (503 or connection failure).
 */
public final class SystemBUnavailableException extends SystemBException {

  public SystemBUnavailableException(Map<String, Object> details) {
    super("System B: unavailable", details);
  }

  public SystemBUnavailableException() {
    this(Map.of());
  }
}
