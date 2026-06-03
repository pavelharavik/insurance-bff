package com.insurance.bff.client;

import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.exception.UpstreamServiceException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Base class for HTTP-backed insurance client adapters.
 *
 * <p>Provides the common retrieve → onStatus → body pipeline and maps
 * {@link ResourceAccessException} (timeouts, connection failures) to
 * {@link UpstreamServiceException} with status 503. Subclasses build
 * the request (URI, headers) and supply the target response type.
 */
public abstract class AbstractHttpInsuranceClient implements InsuranceClient {

    protected final RestClient restClient;

    protected AbstractHttpInsuranceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Executes the request, translates HTTP errors to domain exceptions, and
     * deserialises the body.
     *
     * @param requestSpec already-configured request (URI and headers set by the subclass)
     * @param responseType target deserialisation type
     * @param patientId    patient identifier, used in exception messages
     */
    protected <T> T fetch(RestClient.RequestHeadersSpec<?> requestSpec,
                          Class<T> responseType,
                          String patientId) {
        try {
            return requestSpec
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        if (res.getStatusCode().value() == 404)
                            throw new InsuranceNotFoundException(patientId);
                        throw new UpstreamServiceException(res.getStatusCode().value());
                    })
                    .body(responseType);
        } catch (ResourceAccessException e) {
            throw new UpstreamServiceException(503, e);
        }
    }
}
