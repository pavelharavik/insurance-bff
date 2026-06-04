package com.insurance.bff.infrastructure.client;

import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.port.InsuranceClient;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

/**
 * Base class for HTTP-backed insurance client adapters.
 *
 * <p>Handles the common retrieve → onStatus → bodyToMono pipeline. HTTP 404 is mapped
 * directly to {@link InsuranceNotFoundException}. All other error statuses are delegated
 * to {@link #onUpstreamHttpError} so each subclass can produce its own infrastructure exception,
 * which is then translated to a domain exception by the subclass before leaving the adapter.
 * Connection failures are also routed through {@link #onUpstreamHttpError}.
 */
public abstract class AbstractHttpInsuranceClient implements InsuranceClient {

    protected final WebClient webClient;

    protected AbstractHttpInsuranceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Executes the request reactively, translates HTTP errors, and deserialises the body.
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
                    UpstreamErrorType type = status == 503
                            ? UpstreamErrorType.UNAVAILABLE
                            : UpstreamErrorType.ERROR;
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> onUpstreamHttpError(type, status, body));
                })
                .bodyToMono(responseType)
                .onErrorMap(WebClientRequestException.class,
                        e -> onUpstreamHttpError(UpstreamErrorType.UNAVAILABLE, 503, null));
    }

    /**
     * Creates a subclass-specific infrastructure exception for non-404 upstream HTTP errors
     * and connection failures. The subclass is responsible for translating this exception
     * into a domain exception before it leaves {@code fetchById}.
     */
    protected abstract RuntimeException onUpstreamHttpError(UpstreamErrorType type, int statusCode, String responseBody);
}
