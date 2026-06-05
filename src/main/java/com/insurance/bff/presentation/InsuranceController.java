package com.insurance.bff.presentation;

import com.insurance.bff.application.InsuranceService;
import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.presentation.exception.HttpException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST entry point for patient insurance lookups.
 */
@RestController
@RequestMapping("/insurance")
public class InsuranceController {

  private final InsuranceService insuranceService;

  public InsuranceController(InsuranceService insuranceService) {
    this.insuranceService = insuranceService;
  }

  /**
   * Returns normalised insurance data for the given patient.
   *
   * @param patientId patient identifier
   * @return insurance response with BFF timestamp
   */
  @GetMapping("/{patientId}")
  public Mono<InsuranceResponse> getInsurance(@PathVariable String patientId) {
    return insuranceService.getInsuranceData(patientId)
        .map(InsuranceResponse::from)
        .onErrorMap(InsuranceNotFoundException.class,
            ex -> new HttpException(404, ex.getMessage(), null))
        .onErrorMap(InsuranceDataUnavailableException.class,
            ex -> new HttpException(toHttpStatus(ex.getType()), ex.getMessage(), null));
  }

  private static int toHttpStatus(UpstreamErrorType type) {
    return switch (type) {
      case UNAVAILABLE -> 503;
      case CLIENT_ERROR -> 500;
      case ERROR -> 500;
    };
  }
}
