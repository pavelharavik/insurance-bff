package com.insurance.bff.infrastructure.client.systema;

import com.insurance.bff.domain.model.InsuranceData;

/**
 * Converts a raw {@link SystemAResponse} into the unified {@link InsuranceData} domain model.
 */
public interface SystemAMapper {

    /**
     * Parses the {@code description} field and maps all System A fields to the domain record.
     *
     * @param response raw response from System A
     * @return normalised insurance data
     */
    InsuranceData map(SystemAResponse response);
}
