package com.insurance.bff.client;

import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.exception.UpstreamServiceException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

/**
 * Base class for HTTP-backed insurance client adapters.
 *
 * <p>Provides the common retrieve → onStatus → bodyToMono pipeline and maps
 * {@link WebClientRequestException} (timeouts, connection failures) to
 * {@link UpstreamServiceException} with status 503. Subclasses build
 * the request (URI, headers) and supply the target response type.
 */
public abstract class AbstractHttpInsuranceClient implements InsuranceClient {

    protected final WebClient webClient;

    protected AbstractHttpInsuranceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Executes the request reactively, translates HTTP errors to domain exceptions,
     * and deserialises the body.
     *
     * @param requestSpec  already-configured request (URI and headers set by the subclass)
     * @param responseType target deserialisation type
     * @param patientId    patient identifier, used in exception messages
     */
    protected <T> Mono<T> fetch(WebClient.RequestHeadersSpec<?> requestSpec,
                                Class<T> responseType,
                                String patientId) {
        return requestSpec
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    int status = response.statusCode().value();
                    if (status == 404) return Mono.just(new InsuranceNotFoundException(patientId));
                    return Mono.just(new UpstreamServiceException(status));
                })
                .bodyToMono(responseType)
                .onErrorMap(WebClientRequestException.class, e -> new UpstreamServiceException(503, e));
    }
}
