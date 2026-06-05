package com.insurance.bff.integration.systemb;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.insurance.bff.application.exception.*;
import com.insurance.bff.domain.model.InsuranceData;
import com.insurance.bff.infrastructure.client.systemb.SystemBClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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
 * Integration tests for {@link SystemBClient} using WireMock to simulate System B.
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableWireMock(
        @ConfigureWireMock(name = "system-b", baseUrlProperties = "upstream.system-b.url")
)
class SystemBClientTest {

    private static final String PATIENT_ID   = "123";
    private static final String PATIENT_PATH = "/insurance/" + PATIENT_ID;

    private static final String ACTIVE_XML =
            "<insurance id=\"123\" first_name=\"Aliaksei\" last_name=\"Kozel\" birth_date=\"1990-01-01\" is_active=\"true\"/>";

    @InjectWireMock("system-b")
    private WireMockServer wireMock;

    @Autowired
    private SystemBClient client;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
    }

    @Test
    void fetchById_returnsMappedInsuranceData_on200() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(ACTIVE_XML)));

        InsuranceData result = client.fetchById(PATIENT_ID).block();

        assertThat(result.id()).isEqualTo("123");
        assertThat(result.name()).isEqualTo("Aliaksei Kozel");
        assertThat(result.active()).isTrue();
    }

    @Test
    void fetchById_mapsInactiveStatus() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<insurance id=\"123\" first_name=\"Aliaksei\" last_name=\"Kozel\" birth_date=\"1990-01-01\" is_active=\"false\"/>")));

        assertThat(client.fetchById(PATIENT_ID).block().active()).isFalse();
    }

    @Test
    void fetchById_throwsSystemBNotFoundException_on404() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(404)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectError(SystemBNotFoundException.class)
                .verify();
    }

    @Test
    void fetchById_throwsSystemBServerErrorException_on500() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(500)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectError(SystemBServerErrorException.class)
                .verify();
    }

    @Test
    void fetchById_throwsSystemBUnavailableException_on503() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withStatus(503)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectError(SystemBUnavailableException.class)
                .verify();
    }

    @Test
    void fetchById_throwsSystemBUnavailableException_onConnectionFailure() {
        wireMock.stubFor(get(urlPathEqualTo(PATIENT_PATH))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        StepVerifier.create(client.fetchById(PATIENT_ID))
                .expectError(SystemBUnavailableException.class)
                .verify();
    }
}
