package com.insurance.bff.presentation.insurance;

import com.insurance.bff.application.insurance.InsuranceService;
import com.insurance.bff.domain.insurance.InsuranceDataUnavailableException;
import com.insurance.bff.domain.insurance.InsuranceNotFoundException;
import com.insurance.bff.domain.insurance.UpstreamErrorType;
import com.insurance.bff.presentation.ApiException;
import com.insurance.bff.presentation.TopLevelErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            ex -> new ApiException(HttpStatus.NOT_FOUND, TopLevelErrorCode.NOT_FOUND,
                ex.getMessage()))
        .onErrorMap(InsuranceDataUnavailableException.class,
            ex -> new ApiException(toHttpStatus(ex.getType()), toErrorCode(ex.getType()),
                ex.getMessage()));
  }

  /**
   * Searches for insurance data by patient name and birth date.
   *
   * @param request search criteria
   * @return insurance response with BFF timestamp
   */
  @PostMapping("/search")
  public Mono<InsuranceResponse> searchInsurance(
      @RequestBody @Valid InsuranceSearchRequest request) {
    return insuranceService.getInsuranceData(request)
        .map(InsuranceResponse::from)
        .onErrorMap(InsuranceNotFoundException.class,
            ex -> new ApiException(HttpStatus.NOT_FOUND, TopLevelErrorCode.NOT_FOUND,
                ex.getMessage()))
        .onErrorMap(InsuranceDataUnavailableException.class,
            ex -> new ApiException(toHttpStatus(ex.getType()), toErrorCode(ex.getType()),
                ex.getMessage()));
  }

  private static HttpStatus toHttpStatus(UpstreamErrorType type) {
    return switch (type) {
      case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
      case CLIENT_ERROR, ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  private static TopLevelErrorCode toErrorCode(UpstreamErrorType type) {
    return switch (type) {
      case UNAVAILABLE -> TopLevelErrorCode.SERVICE_UNAVAILABLE;
      case CLIENT_ERROR, ERROR -> TopLevelErrorCode.UPSTREAM_ERROR;
    };
  }
}
