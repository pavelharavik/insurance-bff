package com.insurance.bff.client.systema;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.exception.UpstreamServiceException;
import com.insurance.bff.model.InsuranceData;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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
 * The real {@link com.insurance.bff.mapper.SystemAMapperImpl} runs end-to-end.
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

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker("system-a").reset();
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
    void fetchById_throwsInsuranceNotFoundException_on404() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(404)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(InsuranceNotFoundException.class)
                            .hasMessageContaining(PATIENT_ID);
                })
                .verify();
    }

    @Test
    void fetchById_throwsUpstreamServiceException_on500() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(500)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(UpstreamServiceException.class);
                    assertThat(((UpstreamServiceException) ex).getStatusCode()).isEqualTo(500);
                })
                .verify();
    }

    @Test
    void fetchById_throwsUpstreamServiceException_on503() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(503)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(UpstreamServiceException.class);
                    assertThat(((UpstreamServiceException) ex).getStatusCode()).isEqualTo(503);
                })
                .verify();
    }

    @Test
    void fetchById_throwsUpstreamServiceException503_onConnectionFailure() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(UpstreamServiceException.class);
                    assertThat(((UpstreamServiceException) ex).getStatusCode()).isEqualTo(503);
                })
                .verify();
    }
}
