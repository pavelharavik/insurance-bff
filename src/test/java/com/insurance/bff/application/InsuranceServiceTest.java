package com.insurance.bff.application;

import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import com.insurance.bff.domain.port.InsuranceClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class InsuranceServiceTest {

    private static final String        PATIENT_ID = "123";
    private static final InsuranceData DATA_A     = new InsuranceData("123", "Alice", true);
    private static final InsuranceData DATA_B     = new InsuranceData("123", "Bob",   false);

    private static InsuranceClient returning(InsuranceData data) {
        return patientId -> Mono.just(data);
    }

    private static InsuranceClient throwing(RuntimeException ex) {
        return patientId -> Mono.error(ex);
    }

    private InsuranceService service(InsuranceClient a, InsuranceClient b) {
        return new InsuranceService(a, b);
    }

    // ── Success cases ──────────────────────────────────────────────────────────

    @Test
    void getInsuranceData_returnsAResult_whenOnlyASucceeds() {
        var svc = service(returning(DATA_A), throwing(new InsuranceNotFoundException(PATIENT_ID)));

        StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
                .expectNext(DATA_A)
                .verifyComplete();
    }

    @Test
    void getInsuranceData_returnsBResult_whenOnlyBSucceeds() {
        var svc = service(throwing(new InsuranceNotFoundException(PATIENT_ID)), returning(DATA_B));

        StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
                .expectNext(DATA_B)
                .verifyComplete();
    }

    @Test
    void getInsuranceData_returnsEitherResult_whenBothSucceed() {
        var svc = service(returning(DATA_A), returning(DATA_B));

        StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
                .assertNext(data -> assertThat(data).isIn(DATA_A, DATA_B))
                .verifyComplete();
    }

    // ── Error priority cases ───────────────────────────────────────────────────

    @Test
    void getInsuranceData_throws404_whenBothReturn404() {
        var svc = service(
                throwing(new InsuranceNotFoundException(PATIENT_ID)),
                throwing(new InsuranceNotFoundException(PATIENT_ID)));

        StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
                .expectError(InsuranceNotFoundException.class)
                .verify();
    }

    @Test
    void getInsuranceData_throwsError_whenAErrorAndB404() {
        var svc = service(
                throwing(new InsuranceDataUnavailableException(UpstreamErrorType.ERROR, 500, null)),
                throwing(new InsuranceNotFoundException(PATIENT_ID)));

        StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
                    assertThat(((InsuranceDataUnavailableException) ex).getStatusCode()).isEqualTo(500);
                })
                .verify();
    }

    @Test
    void getInsuranceData_throwsUnavailable_whenA404andBUnavailable() {
        var svc = service(
                throwing(new InsuranceNotFoundException(PATIENT_ID)),
                throwing(new InsuranceDataUnavailableException(UpstreamErrorType.UNAVAILABLE, 503, null)));

        StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
                    assertThat(((InsuranceDataUnavailableException) ex).getStatusCode()).isEqualTo(503);
                })
                .verify();
    }

    @Test
    void getInsuranceData_throwsError_whenAErrorAndBUnavailable() {
        var svc = service(
                throwing(new InsuranceDataUnavailableException(UpstreamErrorType.ERROR, 500, null)),
                throwing(new InsuranceDataUnavailableException(UpstreamErrorType.UNAVAILABLE, 503, null)));

        StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
                    assertThat(((InsuranceDataUnavailableException) ex).getStatusCode()).isEqualTo(500);
                })
                .verify();
    }

    @Test
    void getInsuranceData_throwsUnavailable_whenBothUnavailable() {
        var svc = service(
                throwing(new InsuranceDataUnavailableException(UpstreamErrorType.UNAVAILABLE, 503, null)),
                throwing(new InsuranceDataUnavailableException(UpstreamErrorType.UNAVAILABLE, 503, null)));

        StepVerifier.create(svc.getInsuranceData(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
                    assertThat(((InsuranceDataUnavailableException) ex).getStatusCode()).isEqualTo(503);
                })
                .verify();
    }
}
