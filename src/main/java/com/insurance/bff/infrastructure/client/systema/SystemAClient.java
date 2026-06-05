package com.insurance.bff.infrastructure.client.systema;

import com.insurance.bff.application.exception.SystemAClientErrorException;
import com.insurance.bff.application.exception.SystemAException;
import com.insurance.bff.application.exception.SystemANotFoundException;
import com.insurance.bff.application.exception.SystemAServerErrorException;
import com.insurance.bff.application.exception.SystemAUnavailableException;
import com.insurance.bff.application.port.SystemAClientPort;
import com.insurance.bff.domain.model.InsuranceData;
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
public class SystemAClient implements SystemAClientPort {

  private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
      new ParameterizedTypeReference<>() {
      };

  private final WebClient webClient;
  private final SystemAMapper mapper;

  public SystemAClient(
      @Qualifier("systemAWebClient") WebClient webClient,
      SystemAMapper mapper) {
    this.webClient = webClient;
    this.mapper = mapper;
  }

  private static SystemAException resolveException(HttpStatusCode status,
      Map<String, Object> details) {
      if (status == HttpStatus.NOT_FOUND) {
          return new SystemANotFoundException(details);
      }
      if (status == HttpStatus.SERVICE_UNAVAILABLE) {
          return new SystemAUnavailableException(details);
      }
      if (status.is4xxClientError()) {
          return new SystemAClientErrorException(details);
      }
    return new SystemAServerErrorException(details);
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
        .onErrorMap(WebClientRequestException.class, e -> new SystemAUnavailableException())
        .map(mapper::map);
  }
}
