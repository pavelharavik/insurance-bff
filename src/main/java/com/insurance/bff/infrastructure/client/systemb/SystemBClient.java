package com.insurance.bff.infrastructure.client.systemb;

import com.insurance.bff.application.port.SystemBClientPort;
import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Fetches insurance data from System B over HTTP/XML.
 *
 * <p>Endpoint: {@code GET /insurance/{id}}
 * <p>XML decoding is offloaded to a bounded-elastic thread to avoid blocking Netty event loops.
 */
@Component
public class SystemBClient implements SystemBClientPort {

    private final WebClient webClient;
    private final SystemBMapper mapper;

    public SystemBClient(
            @Qualifier("systemBWebClient") WebClient webClient,
            SystemBMapper mapper) {
        this.webClient = webClient;
        this.mapper = mapper;
    }

    @Override
    public Mono<InsuranceData> fetchById(String patientId) {
        return webClient.get().uri("/insurance/{id}", patientId).accept(MediaType.APPLICATION_XML)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    int status = response.statusCode().value();
                    if (status == 404) return Mono.just(new InsuranceNotFoundException(patientId));
                    UpstreamErrorType type = status == 503
                            ? UpstreamErrorType.UNAVAILABLE
                            : UpstreamErrorType.ERROR;
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> new SystemBException(type, status, body));
                })
                .bodyToMono(SystemBResponse.class)
                .onErrorMap(WebClientRequestException.class,
                        e -> new SystemBException(UpstreamErrorType.UNAVAILABLE, 503, null))
                .publishOn(Schedulers.boundedElastic())
                .map(mapper::map)
                .onErrorMap(SystemBException.class,
                        ex -> new InsuranceDataUnavailableException(
                                ex.getType(), ex.getStatusCode(), ex.getResponseBody()));
    }
}
