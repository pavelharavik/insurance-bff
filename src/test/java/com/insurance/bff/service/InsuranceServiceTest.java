package com.insurance.bff.service;

import com.insurance.bff.client.InsuranceClient;
import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.exception.UpstreamServiceException;
import com.insurance.bff.model.InsuranceData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InsuranceService}.
 * {@link InsuranceClient} is a functional interface, so lambda stubs are
 * more concise and readable than Mockito mocks.
 */
class InsuranceServiceTest {

    private static final String        PATIENT_ID = "123";
    private static final InsuranceData DATA_A     = new InsuranceData("123", "Alice", true);
    private static final InsuranceData DATA_B     = new InsuranceData("123", "Bob",   false);

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static InsuranceClient returning(InsuranceData data) {
        return patientId -> data;
    }

    private static InsuranceClient throwing(RuntimeException ex) {
        return patientId -> { throw ex; };
    }

    private InsuranceService service(InsuranceClient a, InsuranceClient b) {
        return new InsuranceService(a, b);
    }

    // ── Success cases ─────────────────────────────────────────────────────────

    @Test
    void getInsuranceData_returnsAResult_whenOnlyASucceeds() {
        var svc = service(returning(DATA_A), throwing(new InsuranceNotFoundException(PATIENT_ID)));

        assertThat(svc.getInsuranceData(PATIENT_ID)).isEqualTo(DATA_A);
    }

    @Test
    void getInsuranceData_returnsBResult_whenOnlyBSucceeds() {
        var svc = service(throwing(new InsuranceNotFoundException(PATIENT_ID)), returning(DATA_B));

        assertThat(svc.getInsuranceData(PATIENT_ID)).isEqualTo(DATA_B);
    }

    @Test
    void getInsuranceData_returnsEitherResult_whenBothSucceed() {
        var svc = service(returning(DATA_A), returning(DATA_B));

        assertThat(svc.getInsuranceData(PATIENT_ID)).isIn(DATA_A, DATA_B);
    }

    // ── Error priority cases ──────────────────────────────────────────────────

    @Test
    void getInsuranceData_throws404_whenBothReturn404() {
        var svc = service(
                throwing(new InsuranceNotFoundException(PATIENT_ID)),
                throwing(new InsuranceNotFoundException(PATIENT_ID)));

        assertThatThrownBy(() -> svc.getInsuranceData(PATIENT_ID))
                .isInstanceOf(InsuranceNotFoundException.class);
    }

    @Test
    void getInsuranceData_throws500_whenA500andB404() {
        var svc = service(
                throwing(new UpstreamServiceException(500)),
                throwing(new InsuranceNotFoundException(PATIENT_ID)));

        assertThatThrownBy(() -> svc.getInsuranceData(PATIENT_ID))
                .isInstanceOf(UpstreamServiceException.class)
                .satisfies(ex -> assertThat(((UpstreamServiceException) ex).getStatusCode()).isEqualTo(500));
    }

    @Test
    void getInsuranceData_throws503_whenA404andB503() {
        var svc = service(
                throwing(new InsuranceNotFoundException(PATIENT_ID)),
                throwing(new UpstreamServiceException(503)));

        assertThatThrownBy(() -> svc.getInsuranceData(PATIENT_ID))
                .isInstanceOf(UpstreamServiceException.class)
                .satisfies(ex -> assertThat(((UpstreamServiceException) ex).getStatusCode()).isEqualTo(503));
    }

    @Test
    void getInsuranceData_throws500_whenA500andB503() {
        var svc = service(
                throwing(new UpstreamServiceException(500)),
                throwing(new UpstreamServiceException(503)));

        assertThatThrownBy(() -> svc.getInsuranceData(PATIENT_ID))
                .isInstanceOf(UpstreamServiceException.class)
                .satisfies(ex -> assertThat(((UpstreamServiceException) ex).getStatusCode()).isEqualTo(500));
    }

    @Test
    void getInsuranceData_throws503_whenBothReturn503() {
        var svc = service(
                throwing(new UpstreamServiceException(503)),
                throwing(new UpstreamServiceException(503)));

        assertThatThrownBy(() -> svc.getInsuranceData(PATIENT_ID))
                .isInstanceOf(UpstreamServiceException.class)
                .satisfies(ex -> assertThat(((UpstreamServiceException) ex).getStatusCode()).isEqualTo(503));
    }
}
