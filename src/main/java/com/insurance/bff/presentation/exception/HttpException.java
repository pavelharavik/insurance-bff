package com.insurance.bff.presentation.exception;

/**
 * Presentation-layer exception carrying an HTTP status code and optional upstream response body.
 * Created in the controller from domain exceptions and handled by
 * {@link com.insurance.bff.presentation.GlobalExceptionHandler}.
 */
public class HttpException extends RuntimeException {

  private final int statusCode;
  private final String upstreamBody;

  public HttpException(int statusCode, String detail, String upstreamBody) {
    super(detail);
    this.statusCode = statusCode;
    this.upstreamBody = upstreamBody;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getUpstreamBody() {
    return upstreamBody;
  }
}
