package com.insurance.bff.infrastructure.client.systemb;

import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import com.insurance.bff.infrastructure.client.AbstractHttpInsuranceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Fetches insurance data from System B over HTTP/XML.
 *
 * <p>Endpoint: {@code GET /insurance/{id}}
 * <p>XML decoding is offloaded to a bounded-elastic thread to avoid blocking Netty event loops.
 */
@Component("systemBClient")
public class SystemBClient extends AbstractHttpInsuranceClient {

    private final SystemBMapper mapper;

    public SystemBClient(
            @Qualifier("systemBWebClient") WebClient webClient,
            SystemBMapper mapper) {
        super(webClient);
        this.mapper = mapper;
    }

    @Override
    public Mono<InsuranceData> fetchById(String patientId) {
        return fetch(
                webClient.get().uri("/insurance/{id}", patientId).accept(MediaType.APPLICATION_XML),
                SystemBResponse.class,
                patientId)
                .publishOn(Schedulers.boundedElastic())
                .map(mapper::map)
                .onErrorMap(SystemBException.class,
                        ex -> new InsuranceDataUnavailableException(
                                ex.getType(), ex.getStatusCode(), ex.getResponseBody()));
    }

    @Override
    protected RuntimeException onUpstreamHttpError(UpstreamErrorType type, int statusCode, String responseBody) {
        return new SystemBException(type, statusCode, responseBody);
    }
}
