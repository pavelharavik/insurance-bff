package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * System A returned 404 — patient not found.
 */
public final class SystemANotFoundException extends SystemAException {

  public SystemANotFoundException(Map<String, Object> details) {
    super("System A: not found", details);
  }

  public SystemANotFoundException() {
    this(Map.of());
  }
}
