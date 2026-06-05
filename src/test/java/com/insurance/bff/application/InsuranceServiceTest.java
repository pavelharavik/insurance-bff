package com.insurance.bff.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.bff.application.exception.SystemAClientErrorException;
import com.insurance.bff.application.exception.SystemAException;
import com.insurance.bff.application.exception.SystemANotFoundException;
import com.insurance.bff.application.exception.SystemAServerErrorException;
import com.insurance.bff.application.exception.SystemAUnavailableException;
import com.insurance.bff.application.exception.SystemBException;
import com.insurance.bff.application.exception.SystemBNotFoundException;
import com.insurance.bff.application.exception.SystemBUnavailableException;
import java.util.Map;
import com.insurance.bff.application.port.SystemAClientPort;
import com.insurance.bff.application.port.SystemBClientPort;
import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class InsuranceServiceTest {

  private static final String PATIENT_ID = "123";
  private static final InsuranceData DATA_A = new InsuranceData("123", "Alice", true);
  private static final InsuranceData DATA_B = new InsuranceData("123", "Bob", false);

  private static SystemAClientPort succeedA(InsuranceData data) {
    return patientId -> Mono.just(data);
  }

  private static SystemBClientPort succeedB(InsuranceData data) {
    return patientId -> Mono.just(data);
  }

  private static SystemAClientPort failA(SystemAException ex) {
    return patientId -> Mono.error(ex);
  }

  private static SystemBClientPort failB(SystemBException ex) {
    return patientId -> Mono.error(ex);
  }

  private InsuranceService service(SystemAClientPort a, SystemBClientPort b) {
    return new InsuranceService(a, b);
  }

  // ── Success cases ──────────────────────────────────────────────────────────

  @Test
  void getInsuranceData_returnsAResult_whenOnlyASucceeds() {
    var svc = service(succeedA(DATA_A), failB(new SystemBNotFoundException()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectNext(DATA_A)
        .verifyComplete();
  }

  @Test
  void getInsuranceData_returnsBResult_whenOnlyBSucceeds() {
    var svc = service(failA(new SystemANotFoundException()), succeedB(DATA_B));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectNext(DATA_B)
        .verifyComplete();
  }

  @Test
  void getInsuranceData_returnsEitherResult_whenBothSucceed() {
    var svc = service(succeedA(DATA_A), succeedB(DATA_B));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .assertNext(data -> assertThat(data).isIn(DATA_A, DATA_B))
        .verifyComplete();
  }

  // ── Error priority cases ───────────────────────────────────────────────────

  @Test
  void getInsuranceData_throws404_whenBothReturn404() {
    var svc = service(
        failA(new SystemANotFoundException()),
        failB(new SystemBNotFoundException()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectError(InsuranceNotFoundException.class)
        .verify();
  }

  @Test
  void getInsuranceData_throwsError_whenAErrorAndB404() {
    var svc = service(
        failA(new SystemAServerErrorException()),
        failB(new SystemBNotFoundException()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(
            ex -> assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class))
        .verify();
  }

  @Test
  void getInsuranceData_throwsUnavailable_whenA404andBUnavailable() {
    var svc = service(
        failA(new SystemANotFoundException()),
        failB(new SystemBUnavailableException()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(
            ex -> assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class))
        .verify();
  }

  @Test
  void getInsuranceData_throwsError_whenAErrorAndBUnavailable() {
    var svc = service(
        failA(new SystemAServerErrorException()),
        failB(new SystemBUnavailableException()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(ex -> {
          assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
          assertThat(((InsuranceDataUnavailableException) ex).getType()).isEqualTo(
              UpstreamErrorType.ERROR);
        })
        .verify();
  }

  @Test
  void getInsuranceData_throwsUnavailable_whenBothUnavailable() {
    var svc = service(
        failA(new SystemAUnavailableException()),
        failB(new SystemBUnavailableException()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(ex -> {
          assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
          assertThat(((InsuranceDataUnavailableException) ex).getType()).isEqualTo(
              UpstreamErrorType.UNAVAILABLE);
        })
        .verify();
  }

  @Test
  void getInsuranceData_throwsClientError_whenAClientErrorAndBNotFound() {
    var svc = service(
        failA(new SystemAClientErrorException()),
        failB(new SystemBNotFoundException()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(ex -> {
          assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
          assertThat(((InsuranceDataUnavailableException) ex).getType())
              .isEqualTo(UpstreamErrorType.CLIENT_ERROR);
        })
        .verify();
  }

  @Test
  void getInsuranceData_includesUpstreamDetail_ofHigherPriorityError() {
    // A: ERROR (priority 4, wins), B: UNAVAILABLE (priority 2)
    // detail comes from A because A has the highest priority
    var svc = service(
        failA(new SystemAServerErrorException(Map.of("field", "patientId"))),
        failB(new SystemBUnavailableException()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(ex -> {
          assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
          assertThat(ex.getMessage()).contains("patientId");
        })
        .verify();
  }
}
