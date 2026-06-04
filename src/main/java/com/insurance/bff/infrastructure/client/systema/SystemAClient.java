package com.insurance.bff.infrastructure.client.systema;

import com.insurance.bff.application.port.SystemAClientPort;
import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

/**
 * Fetches insurance data from System A over HTTP/JSON.
 *
 * <p>Endpoint: {@code GET /patients/{id}/insurance}
 */
@Component
public class SystemAClient implements SystemAClientPort {

    private final WebClient webClient;
    private final SystemAMapper mapper;

    public SystemAClient(
            @Qualifier("systemAWebClient") WebClient webClient,
            SystemAMapper mapper) {
        this.webClient = webClient;
        this.mapper = mapper;
    }

    @Override
    public Mono<InsuranceData> fetchById(String patientId) {
        return webClient.get().uri("/patients/{id}/insurance", patientId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    int status = response.statusCode().value();
                    if (status == 404) return Mono.just(new InsuranceNotFoundException(patientId));
                    UpstreamErrorType type = status == 503
                            ? UpstreamErrorType.UNAVAILABLE
                            : UpstreamErrorType.ERROR;
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> new SystemAException(type, status, body));
                })
                .bodyToMono(SystemAResponse.class)
                .onErrorMap(WebClientRequestException.class,
                        e -> new SystemAException(UpstreamErrorType.UNAVAILABLE, 503, null))
                .map(mapper::map)
                .onErrorMap(SystemAException.class,
                        ex -> new InsuranceDataUnavailableException(
                                ex.getType(), ex.getStatusCode(), ex.getResponseBody()));
    }
}
