package com.insurance.bff.presentation.insurance;

import static org.mockito.Mockito.when;

import com.insurance.bff.application.insurance.InsuranceService;
import com.insurance.bff.domain.insurance.InsuranceData;
import com.insurance.bff.domain.insurance.InsuranceDataUnavailableException;
import com.insurance.bff.domain.insurance.InsuranceNotFoundException;
import com.insurance.bff.domain.insurance.UpstreamErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(InsuranceController.class)
class InsuranceControllerTest {

  private static final String PATIENT_ID = "123";
  private static final InsuranceData DATA = new InsuranceData("123", "Alice", true);

  @Autowired
  private WebTestClient webTestClient;

  @MockitoBean
  private InsuranceService insuranceService;

  @Test
  void getInsurance_returns200_withMappedFields() {
    when(insuranceService.getInsuranceData(PATIENT_ID)).thenReturn(Mono.just(DATA));

    webTestClient.get().uri("/insurance/{id}", PATIENT_ID)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo("123")
        .jsonPath("$.name").isEqualTo("Alice")
        .jsonPath("$.is_active").isEqualTo(true)
        .jsonPath("$.current_date").exists();
  }

  @Test
  void getInsurance_returns404_onInsuranceNotFoundException() {
    when(insuranceService.getInsuranceData(PATIENT_ID))
        .thenReturn(Mono.error(new InsuranceNotFoundException(PATIENT_ID)));

    webTestClient.get().uri("/insurance/{id}", PATIENT_ID)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.detail").isEqualTo("No insurance record found for patient: " + PATIENT_ID);
  }

  @Test
  void getInsurance_returns500_onInsuranceDataUnavailableError() {
    when(insuranceService.getInsuranceData(PATIENT_ID))
        .thenReturn(Mono.error(new InsuranceDataUnavailableException(UpstreamErrorType.ERROR)));

    webTestClient.get().uri("/insurance/{id}", PATIENT_ID)
        .exchange()
        .expectStatus().isEqualTo(500)
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("An upstream service error prevented insurance data retrieval");
  }

  @Test
  void getInsurance_returns503_onInsuranceDataUnavailableUnavailable() {
    when(insuranceService.getInsuranceData(PATIENT_ID))
        .thenReturn(
            Mono.error(new InsuranceDataUnavailableException(UpstreamErrorType.UNAVAILABLE)));

    webTestClient.get().uri("/insurance/{id}", PATIENT_ID)
        .exchange()
        .expectStatus().isEqualTo(503)
        .expectBody()
        .jsonPath("$.detail").isEqualTo("Insurance data is temporarily unavailable");
  }

  @Test
  void getInsurance_returns500_onInsuranceDataUnavailableClientError() {
    when(insuranceService.getInsuranceData(PATIENT_ID))
        .thenReturn(
            Mono.error(new InsuranceDataUnavailableException(UpstreamErrorType.CLIENT_ERROR)));

    webTestClient.get().uri("/insurance/{id}", PATIENT_ID)
        .exchange()
        .expectStatus().isEqualTo(500)
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Insurance data request was rejected by the upstream service");
  }
}
