package com.insurance.bff.client.systemb;

import com.insurance.bff.client.AbstractHttpInsuranceClient;
import com.insurance.bff.mapper.SystemBMapper;
import com.insurance.bff.model.InsuranceData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Fetches insurance data from System B over HTTP/XML.
 *
 * <p>Endpoint: {@code GET /insurance/{id}}
 */
@Component("systemBClient")
public class SystemBClient extends AbstractHttpInsuranceClient {

    private final SystemBMapper mapper;

    public SystemBClient(
            @Qualifier("systemBRestClient") RestClient restClient,
            SystemBMapper mapper) {
        super(restClient);
        this.mapper = mapper;
    }

    @Override
    @CircuitBreaker(name = "system-b")
    public InsuranceData fetchById(String patientId) {
        return mapper.map(fetch(
                restClient.get().uri("/insurance/{id}", patientId).accept(MediaType.APPLICATION_XML),
                SystemBResponse.class,
                patientId));
    }
}
