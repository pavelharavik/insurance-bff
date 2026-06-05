package com.insurance.bff.infrastructure.client.systemb;

import com.insurance.bff.application.exception.SystemBException;
import com.insurance.bff.application.port.SystemBClientPort;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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

    private static UpstreamErrorType resolveType(HttpStatusCode status) {
        if (status == HttpStatus.NOT_FOUND)           return UpstreamErrorType.NOT_FOUND;
        if (status == HttpStatus.SERVICE_UNAVAILABLE) return UpstreamErrorType.UNAVAILABLE;
        if (status.is4xxClientError())                return UpstreamErrorType.CLIENT_ERROR;
        return UpstreamErrorType.ERROR;
    }

    @Override
    public Mono<InsuranceData> fetchById(String patientId) {
        return webClient.get().uri("/insurance/{id}", patientId).accept(MediaType.APPLICATION_XML)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    HttpStatusCode status = response.statusCode();
                    UpstreamErrorType type = resolveType(status);
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> new SystemBException(type, status.value(), body));
                })
                .bodyToMono(SystemBResponse.class)
                .onErrorMap(WebClientRequestException.class,
                        e -> new SystemBException(UpstreamErrorType.UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE.value(), null))
                .publishOn(Schedulers.boundedElastic())
                .map(mapper::map);
    }
}
