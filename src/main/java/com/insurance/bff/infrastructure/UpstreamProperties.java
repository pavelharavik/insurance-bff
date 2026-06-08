package com.insurance.bff.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for upstream system HTTP clients. Bound from the {@code upstream} prefix in
 * {@code application.yml}.
 */
@ConfigurationProperties(prefix = "upstream")
public record UpstreamProperties(SystemProperties systemA, SystemProperties systemB) {

  /**
   * @param url              base URL of the upstream system
   * @param connectTimeoutMs TCP connect timeout in milliseconds
   * @param readTimeoutMs    socket read timeout in milliseconds
   */
  public record SystemProperties(String url, int connectTimeoutMs, int readTimeoutMs) {

  }
}
