package com.insurance.bff.client.systema;

import com.insurance.bff.client.AbstractHttpInsuranceClient;
import com.insurance.bff.mapper.SystemAMapper;
import com.insurance.bff.model.InsuranceData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Fetches insurance data from System A over HTTP/JSON.
 *
 * <p>Endpoint: {@code GET /patients/{id}/insurance}
 */
@Component("systemAClient")
public class SystemAClient extends AbstractHttpInsuranceClient {

    private final SystemAMapper mapper;

    public SystemAClient(
            @Qualifier("systemARestClient") RestClient restClient,
            SystemAMapper mapper) {
        super(restClient);
        this.mapper = mapper;
    }

    @Override
    @CircuitBreaker(name = "system-a")
    public InsuranceData fetchById(String patientId) {
        return mapper.map(fetch(
                restClient.get().uri("/patients/{id}/insurance", patientId),
                SystemAResponse.class,
                patientId));
    }
}
