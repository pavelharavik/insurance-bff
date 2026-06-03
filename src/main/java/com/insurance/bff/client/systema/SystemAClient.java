package com.insurance.bff.client.systema;

import com.insurance.bff.client.AbstractHttpInsuranceClient;
import com.insurance.bff.mapper.SystemAMapper;
import com.insurance.bff.model.InsuranceData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Fetches insurance data from System A over HTTP/JSON.
 *
 * <p>Endpoint: {@code GET /patients/{id}/insurance}
 */
@Component("systemAClient")
public class SystemAClient extends AbstractHttpInsuranceClient {

    private final SystemAMapper mapper;

    public SystemAClient(
            @Qualifier("systemAWebClient") WebClient webClient,
            SystemAMapper mapper) {
        super(webClient);
        this.mapper = mapper;
    }

    @Override
    public Mono<InsuranceData> fetchById(String patientId) {
        return fetch(
                webClient.get().uri("/patients/{id}/insurance", patientId),
                SystemAResponse.class,
                patientId)
                .map(mapper::map);
    }
}
