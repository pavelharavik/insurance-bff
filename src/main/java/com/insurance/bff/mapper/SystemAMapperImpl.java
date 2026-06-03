package com.insurance.bff.mapper;

import com.insurance.bff.client.systema.SystemAResponse;
import com.insurance.bff.exception.UpstreamServiceException;
import com.insurance.bff.model.InsuranceData;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link SystemAMapper}.
 *
 * <p>System A encodes name and status in a single {@code description} string:
 * {@code "Patient: <name>, status: <status>"}. This implementation parses
 * that format to extract the individual fields.
 *
 * <ul>
 *   <li>{@code id}          → {@code id}</li>
 *   <li>{@code description} → {@code name} (text between "Patient: " and ", status: ")</li>
 *   <li>{@code description} → {@code active} (true when status equals "active", case-insensitive)</li>
 * </ul>
 */
@Component
public class SystemAMapperImpl implements SystemAMapper {

    private static final String PATIENT_PREFIX  = "Patient: ";
    private static final String STATUS_SEPARATOR = ", status: ";

    @Override
    public InsuranceData map(SystemAResponse response) {
        String description = response.description();
        int statusIndex    = description != null ? description.lastIndexOf(STATUS_SEPARATOR) : -1;
        if (statusIndex < 0) {
            throw new UpstreamServiceException(500, "Unexpected System A description format: " + description);
        }

        String name   = description.substring(PATIENT_PREFIX.length(), statusIndex);
        String status = description.substring(statusIndex + STATUS_SEPARATOR.length()).trim();

        return new InsuranceData(response.id(), name, "active".equalsIgnoreCase(status));
    }
}
