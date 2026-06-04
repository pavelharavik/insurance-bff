package com.insurance.bff.infrastructure.client.systema;

import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import com.insurance.bff.infrastructure.client.AbstractHttpInsuranceClient;
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
                .map(mapper::map)
                .onErrorMap(SystemAException.class,
                        ex -> new InsuranceDataUnavailableException(
                                ex.getType(), ex.getStatusCode(), ex.getResponseBody()));
    }

    @Override
    protected RuntimeException onUpstreamHttpError(UpstreamErrorType type, int statusCode, String responseBody) {
        return new SystemAException(type, statusCode, responseBody);
    }
}
