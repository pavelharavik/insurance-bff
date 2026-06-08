package com.insurance.bff.infrastructure.systema;

import com.insurance.bff.application.insurance.systema.SystemAClient;
import com.insurance.bff.application.insurance.systema.SystemAException;
import com.insurance.bff.domain.insurance.InsuranceData;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
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
public class SystemAHttpClient implements SystemAClient {

  private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
      new ParameterizedTypeReference<>() {
      };

  private final WebClient webClient;
  private final SystemAMapper mapper;

  public SystemAHttpClient(
      @Qualifier("systemAWebClient") WebClient webClient,
      SystemAMapper mapper) {
    this.webClient = webClient;
    this.mapper = mapper;
  }

  private static SystemAException resolveException(HttpStatusCode status,
      Map<String, Object> details) {
    if (status == HttpStatus.NOT_FOUND) {
      return new SystemAException.NotFound(details);
    }
    if (status == HttpStatus.SERVICE_UNAVAILABLE) {
      return new SystemAException.Unavailable(details);
    }
    if (status.is4xxClientError()) {
      return new SystemAException.ClientError(details);
    }
    return new SystemAException.ServerError(details);
  }

  @Override
  public Mono<InsuranceData> fetchById(String patientId) {
    return webClient.get().uri("/patients/{id}/insurance", patientId)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response ->
            response.bodyToMono(MAP_TYPE)
                .onErrorResume(e -> Mono.just(Map.of()))
                .defaultIfEmpty(Map.of())
                .map(details -> resolveException(response.statusCode(), details)))
        .bodyToMono(SystemAResponse.class)
        .onErrorMap(WebClientRequestException.class, e -> new SystemAException.Unavailable())
        .map(mapper::map);
  }
}
