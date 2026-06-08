package com.insurance.bff.application.insurance;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.bff.application.insurance.systema.SystemAClient;
import com.insurance.bff.application.insurance.systema.SystemAException;
import com.insurance.bff.application.insurance.systemb.SystemBClient;
import com.insurance.bff.application.insurance.systemb.SystemBException;
import com.insurance.bff.domain.insurance.InsuranceData;
import com.insurance.bff.domain.insurance.InsuranceDataUnavailableException;
import com.insurance.bff.domain.insurance.InsuranceNotFoundException;
import com.insurance.bff.domain.insurance.UpstreamErrorType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class InsuranceServiceTest {

  private static final String PATIENT_ID = "123";
  private static final InsuranceData DATA_A = new InsuranceData("123", "Alice", true);
  private static final InsuranceData DATA_B = new InsuranceData("123", "Bob", false);

  private static SystemAClient succeedA(InsuranceData data) {
    return patientId -> Mono.just(data);
  }

  private static SystemBClient succeedB(InsuranceData data) {
    return patientId -> Mono.just(data);
  }

  private static SystemAClient failA(SystemAException ex) {
    return patientId -> Mono.error(ex);
  }

  private static SystemBClient failB(SystemBException ex) {
    return patientId -> Mono.error(ex);
  }

  private InsuranceService service(SystemAClient a, SystemBClient b) {
    return new InsuranceService(a, b);
  }

  // ── Success cases ──────────────────────────────────────────────────────────

  @Test
  void getInsuranceData_returnsAResult_whenOnlyASucceeds() {
    var svc = service(succeedA(DATA_A), failB(new SystemBException.NotFound()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectNext(DATA_A)
        .verifyComplete();
  }

  @Test
  void getInsuranceData_returnsBResult_whenOnlyBSucceeds() {
    var svc = service(failA(new SystemAException.NotFound()), succeedB(DATA_B));

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
        failA(new SystemAException.NotFound()),
        failB(new SystemBException.NotFound()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectError(InsuranceNotFoundException.class)
        .verify();
  }

  @Test
  void getInsuranceData_throwsError_whenAErrorAndB404() {
    var svc = service(
        failA(new SystemAException.ServerError()),
        failB(new SystemBException.NotFound()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(
            ex -> assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class))
        .verify();
  }

  @Test
  void getInsuranceData_throwsUnavailable_whenA404andBUnavailable() {
    var svc = service(
        failA(new SystemAException.NotFound()),
        failB(new SystemBException.Unavailable()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(
            ex -> assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class))
        .verify();
  }

  @Test
  void getInsuranceData_throwsError_whenAErrorAndBUnavailable() {
    var svc = service(
        failA(new SystemAException.ServerError()),
        failB(new SystemBException.Unavailable()));

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
        failA(new SystemAException.Unavailable()),
        failB(new SystemBException.Unavailable()));

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
        failA(new SystemAException.ClientError()),
        failB(new SystemBException.NotFound()));

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
        failA(new SystemAException.ServerError(Map.of("field", "patientId"))),
        failB(new SystemBException.Unavailable()));

    StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
        .expectErrorSatisfies(ex -> {
          assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
          assertThat(ex.getMessage()).contains("patientId");
        })
        .verify();
  }
}
