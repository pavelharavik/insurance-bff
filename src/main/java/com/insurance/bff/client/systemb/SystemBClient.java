package com.insurance.bff.client.systemb;

import com.insurance.bff.client.AbstractHttpInsuranceClient;
import com.insurance.bff.mapper.SystemBMapper;
import com.insurance.bff.model.InsuranceData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    @CircuitBreaker(name = "system-b")
    public Mono<InsuranceData> fetchById(String patientId) {
        return fetch(
                webClient.get().uri("/insurance/{id}", patientId).accept(MediaType.APPLICATION_XML),
                SystemBResponse.class,
                patientId)
                .publishOn(Schedulers.boundedElastic())
                .map(mapper::map);
        /*
        * One important caveat: publishOn affects operators downstream of it, so mapper::map and anything further now run on boundedElastic.
        * However, the XmlMapper parsing itself happens inside bodyToMono (before the publishOn in the chain), so it still runs on the Netty
        * event loop — that part cannot be avoided with the current ResponseSpec API without a more invasive restructuring.
        * What this gives you: the event loop thread is released the moment bodyToMono emits the decoded SystemBResponse, instead of also
        * bearing the mapper work. For the benchmark comparison specifically, it means System B's presence on the event loop is bounded to
        * deserialization time only, and won't bleed into System A's request processing through downstream operators.
        */
    }
}
