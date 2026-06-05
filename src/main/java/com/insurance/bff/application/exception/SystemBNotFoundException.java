package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * System B returned 404 — patient not found.
 */
public final class SystemBNotFoundException extends SystemBException {

  public SystemBNotFoundException(Map<String, Object> details) {
    super("System B: not found", details);
  }

  public SystemBNotFoundException() {
    this(Map.of());
  }
}
