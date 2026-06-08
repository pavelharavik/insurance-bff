package com.insurance.bff.application.insurance.systema;

import com.insurance.bff.domain.insurance.InsuranceData;
import com.insurance.bff.domain.insurance.InsuranceDataUnavailableException;
import com.insurance.bff.domain.insurance.InsuranceNotFoundException;
import reactor.core.publisher.Mono;

/**
 * Output port for fetching insurance data from System A.
 */
public interface SystemAClient {

  /**
   * @param patientId the patient identifier to look up
   * @return a {@link Mono} emitting normalised insurance data on success, or terminating with
   * {@link InsuranceNotFoundException} (404) or {@link InsuranceDataUnavailableException} on any
   * other failure
   */
  Mono<InsuranceData> fetchById(String patientId);
}
