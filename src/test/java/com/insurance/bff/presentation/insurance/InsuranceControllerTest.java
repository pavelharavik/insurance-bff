package com.insurance.bff.presentation.insurance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.insurance.bff.application.insurance.InsuranceService;
import com.insurance.bff.domain.insurance.InsuranceData;
import com.insurance.bff.domain.insurance.InsuranceDataUnavailableException;
import com.insurance.bff.domain.insurance.InsuranceNotFoundException;
import com.insurance.bff.domain.insurance.UpstreamErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
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

  // ── GET /{patientId} ──────────────────────────────────────────────────────

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
        .jsonPath("$.errorCode").isEqualTo("NOT_FOUND")
        .jsonPath("$.message").isEqualTo("No insurance record found for patient: " + PATIENT_ID);
  }

  @Test
  void getInsurance_returns500_onInsuranceDataUnavailableError() {
    when(insuranceService.getInsuranceData(PATIENT_ID))
        .thenReturn(Mono.error(new InsuranceDataUnavailableException(UpstreamErrorType.ERROR)));

    webTestClient.get().uri("/insurance/{id}", PATIENT_ID)
        .exchange()
        .expectStatus().isEqualTo(500)
        .expectBody()
        .jsonPath("$.errorCode").isEqualTo("UPSTREAM_ERROR")
        .jsonPath("$.message")
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
        .jsonPath("$.errorCode").isEqualTo("SERVICE_UNAVAILABLE")
        .jsonPath("$.message").isEqualTo("Insurance data is temporarily unavailable");
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
        .jsonPath("$.errorCode").isEqualTo("UPSTREAM_ERROR")
        .jsonPath("$.message")
        .isEqualTo("Insurance data request was rejected by the upstream service");
  }

  // ── POST /search ──────────────────────────────────────────────────────────

  @Test
  void searchInsurance_returns200_withMappedFields() {
    when(insuranceService.getInsuranceData(any(InsuranceSearchRequest.class)))
        .thenReturn(Mono.just(DATA));

    webTestClient.post().uri("/insurance/search")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"birthDate\":\"1985-03-15\"}")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo("123")
        .jsonPath("$.name").isEqualTo("Alice")
        .jsonPath("$.is_active").isEqualTo(true)
        .jsonPath("$.current_date").exists();
  }

  @Test
  void searchInsurance_returns400_whenFirstNameBlank() {
    webTestClient.post().uri("/insurance/search")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"firstName\":\"\",\"lastName\":\"Smith\",\"birthDate\":\"1985-03-15\"}")
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR")
        .jsonPath("$.errors[0].field").isEqualTo("firstName")
        .jsonPath("$.errors[0].errorCode").isEqualTo("REQUIRED");
  }

  @Test
  void searchInsurance_returns400_whenLastNameMissing() {
    webTestClient.post().uri("/insurance/search")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"firstName\":\"Alice\"}")
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
  }

  @Test
  void searchInsurance_returns404_onInsuranceNotFoundException() {
    when(insuranceService.getInsuranceData(any(InsuranceSearchRequest.class)))
        .thenReturn(Mono.error(new InsuranceNotFoundException("999")));

    webTestClient.post().uri("/insurance/search")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            "{\"firstName\":\"Unknown\",\"lastName\":\"Person\",\"birthDate\":\"2000-01-01\"}")
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.errorCode").isEqualTo("NOT_FOUND");
  }

  @Test
  void searchInsurance_returns503_onInsuranceDataUnavailableUnavailable() {
    when(insuranceService.getInsuranceData(any(InsuranceSearchRequest.class)))
        .thenReturn(
            Mono.error(new InsuranceDataUnavailableException(UpstreamErrorType.UNAVAILABLE)));

    webTestClient.post().uri("/insurance/search")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"birthDate\":\"1985-03-15\"}")
        .exchange()
        .expectStatus().isEqualTo(503)
        .expectBody()
        .jsonPath("$.errorCode").isEqualTo("SERVICE_UNAVAILABLE");
  }
}