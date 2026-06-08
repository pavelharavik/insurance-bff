package com.insurance.bff.application.exception;

import java.util.Map;

/**
 * Base sealed exception for System A upstream failures. Each subtype represents a distinct failure
 * category. {@code details} carries the parsed JSON body returned by System A, or internal context
 * (e.g. a malformed field value) when the failure originates inside the BFF adapter.
 */
public abstract sealed class SystemAException extends RuntimeException
    permits SystemAException.NotFound, SystemAException.Unavailable,
    SystemAException.ClientError, SystemAException.ServerError {

  private final Map<String, Object> details;

  protected SystemAException(String message, Map<String, Object> details) {
    super(message);
    this.details = details != null ? details : Map.of();
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  /** System A returned 404 — patient not found. */
  public static final class NotFound extends SystemAException {

    public NotFound(Map<String, Object> details) {
      super("System A: not found", details);
    }

    public NotFound() {
      this(Map.of());
    }
  }

  /** System A is temporarily unavailable (503 or connection failure). */
  public static final class Unavailable extends SystemAException {

    public Unavailable(Map<String, Object> details) {
      super("System A: unavailable", details);
    }

    public Unavailable() {
      this(Map.of());
    }
  }

  /** System A rejected the request with a 4xx response — indicates an integration bug. */
  public static final class ClientError extends SystemAException {

    public ClientError(Map<String, Object> details) {
      super("System A: client error", details);
    }

    public ClientError() {
      this(Map.of());
    }
  }

  /** System A returned a 5xx server error or sent a response the BFF could not process. */
  public static final class ServerError extends SystemAException {

    public ServerError(Map<String, Object> details) {
      super("System A: server error", details);
    }

    public ServerError() {
      this(Map.of());
    }
  }
}
