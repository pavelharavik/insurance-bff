package com.insurance.bff.infrastructure.client.systema;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.insurance.bff.application.exception.SystemAException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import reactor.test.StepVerifier;

import com.github.tomakehurst.wiremock.http.Fault;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SystemAClient} using WireMock to simulate System A.
 * The real {@link SystemAMapperImpl} runs end-to-end.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableWireMock(
        @ConfigureWireMock(name = "system-a", baseUrlProperties = "upstream.system-a.url")
)
class SystemAClientTest {

    private static final String PATIENT_ID   = "123";
    private static final String PATIENT_PATH = "/patients/" + PATIENT_ID + "/insurance";

    @InjectWireMock("system-a")
    private WireMockServer wireMock;

    @Autowired
    private SystemAClient client;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
    }

    @Test
    void fetchById_returnsMappedInsuranceData_on200() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(okJson("""
                        {
                          "id": "123",
                          "description": "Patient: Aliaksei Kozel, status: active"
                        }
                        """)));

        InsuranceData result = client.fetchById(PATIENT_ID).block();

        assertThat(result.id()).isEqualTo("123");
        assertThat(result.name()).isEqualTo("Aliaksei Kozel");
        assertThat(result.active()).isTrue();
    }

    @Test
    void fetchById_mapsInactiveStatus() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(okJson("""
                        {
                          "id": "123",
                          "description": "Patient: Aliaksei Kozel, status: inactive"
                        }
                        """)));

        assertThat(client.fetchById(PATIENT_ID).block().active()).isFalse();
    }

    @Test
    void fetchById_throwsSystemAException_on404() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(404)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(SystemAException.class);
                    assertThat(((SystemAException) ex).getType()).isEqualTo(UpstreamErrorType.NOT_FOUND);
                    assertThat(((SystemAException) ex).getStatusCode()).isEqualTo(404);
                })
                .verify();
    }

    @Test
    void fetchById_throwsSystemAException_on500() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(500)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(SystemAException.class);
                    assertThat(((SystemAException) ex).getStatusCode()).isEqualTo(500);
                })
                .verify();
    }

    @Test
    void fetchById_throwsSystemAException_on503() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(503)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(SystemAException.class);
                    assertThat(((SystemAException) ex).getStatusCode()).isEqualTo(503);
                })
                .verify();
    }

    @Test
    void fetchById_throwsSystemAException503_onConnectionFailure() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(SystemAException.class);
                    assertThat(((SystemAException) ex).getStatusCode()).isEqualTo(503);
                })
                .verify();
    }
}
