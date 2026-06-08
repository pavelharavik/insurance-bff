package com.insurance.bff.application.insurance.systemb;

import java.util.Map;

/**
 * Base sealed exception for System B upstream failures. Each subtype represents a distinct failure
 * category. {@code details} carries the parsed JSON body returned by System B, or internal context
 * (e.g. a malformed field value) when the failure originates inside the BFF adapter.
 */
public abstract sealed class SystemBException extends RuntimeException
    permits SystemBException.NotFound, SystemBException.Unavailable,
    SystemBException.ClientError, SystemBException.ServerError {

  private final Map<String, Object> details;

  protected SystemBException(String message, Map<String, Object> details) {
    super(message);
    this.details = details != null ? details : Map.of();
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  /**
   * System B returned 404 — patient not found.
   */
  public static final class NotFound extends SystemBException {

    public NotFound(Map<String, Object> details) {
      super("System B: not found", details);
    }

    public NotFound() {
      this(Map.of());
    }
  }

  /**
   * System B is temporarily unavailable (503 or connection failure).
   */
  public static final class Unavailable extends SystemBException {

    public Unavailable(Map<String, Object> details) {
      super("System B: unavailable", details);
    }

    public Unavailable() {
      this(Map.of());
    }
  }

  /**
   * System B rejected the request with a 4xx response — indicates an integration bug.
   */
  public static final class ClientError extends SystemBException {

    public ClientError(Map<String, Object> details) {
      super("System B: client error", details);
    }

    public ClientError() {
      this(Map.of());
    }
  }

  /**
   * System B returned a 5xx server error or sent a response the BFF could not process.
   */
  public static final class ServerError extends SystemBException {

    public ServerError(Map<String, Object> details) {
      super("System B: server error", details);
    }

    public ServerError() {
      this(Map.of());
    }
  }
}
