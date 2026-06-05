package com.insurance.bff.infrastructure.client.systema;

import com.insurance.bff.application.exception.SystemAServerErrorException;
import com.insurance.bff.domain.model.InsuranceData;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link SystemAMapper}.
 *
 * <p>System A encodes name and status in a single {@code description} string:
 * {@code "Patient: <name>, status: <status>"}. Uses {@code lastIndexOf} on the separator to handle
 * names that themselves contain commas.
 */
@Component
public class SystemAMapperImpl implements SystemAMapper {

  private static final String PATIENT_PREFIX = "Patient: ";
  private static final String STATUS_SEPARATOR = ", status: ";

  @Override
  public InsuranceData map(SystemAResponse response) {
    String description = response.description();
    int statusIndex = description != null ? description.lastIndexOf(STATUS_SEPARATOR) : -1;
    if (statusIndex < 0) {
      throw new SystemAServerErrorException(Map.of("description", String.valueOf(description)));
    }
    String name = description.substring(PATIENT_PREFIX.length(), statusIndex);
    String status = description.substring(statusIndex + STATUS_SEPARATOR.length()).trim();
    return new InsuranceData(response.id(), name, "active".equalsIgnoreCase(status));
  }
}
