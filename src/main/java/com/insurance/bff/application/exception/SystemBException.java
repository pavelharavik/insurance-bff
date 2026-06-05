package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * Base sealed exception for System B upstream failures. Each subtype represents a distinct failure
 * category. {@code details} carries the parsed JSON body returned by System B, or internal context
 * (e.g. a malformed field value) when the failure originates inside the BFF adapter.
 */
public abstract sealed class SystemBException extends RuntimeException
    permits SystemBNotFoundException, SystemBUnavailableException,
    SystemBClientErrorException, SystemBServerErrorException {

  private final Map<String, Object> details;

  protected SystemBException(String message, Map<String, Object> details) {
    super(message);
    this.details = details != null ? details : Map.of();
  }

  public Map<String, Object> getDetails() {
    return details;
  }
}
