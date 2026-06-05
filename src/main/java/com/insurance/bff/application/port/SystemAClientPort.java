package com.insurance.bff.application.port;

import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.model.InsuranceData;
import reactor.core.publisher.Mono;

/**
 * Output port for fetching insurance data from System A.
 */
public interface SystemAClientPort {

  /**
   * @param patientId the patient identifier to look up
   * @return a {@link Mono} emitting normalised insurance data on success, or terminating with
   * {@link InsuranceNotFoundException} (404) or {@link InsuranceDataUnavailableException} on any
   * other failure
   */
  Mono<InsuranceData> fetchById(String patientId);
}
