package com.insurance.bff.infrastructure.client.systemb;

import com.insurance.bff.domain.model.InsuranceData;

/**
 * Converts a raw {@link SystemBResponse} into the unified {@link InsuranceData} domain model.
 */
public interface SystemBMapper {

    /**
     * Maps System B XML fields to the unified domain record.
     *
     * @param response raw response from System B
     * @return normalised insurance data
     */
    InsuranceData map(SystemBResponse response);
}
