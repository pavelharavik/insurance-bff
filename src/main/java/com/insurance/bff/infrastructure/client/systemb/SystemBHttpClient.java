package com.insurance.bff.infrastructure.client.systemb;

import com.insurance.bff.application.exception.SystemBClientErrorException;
import com.insurance.bff.application.exception.SystemBException;
import com.insurance.bff.application.exception.SystemBNotFoundException;
import com.insurance.bff.application.exception.SystemBServerErrorException;
import com.insurance.bff.application.exception.SystemBUnavailableException;
import com.insurance.bff.application.port.SystemBClient;
import com.insurance.bff.domain.model.InsuranceData;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
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
public class SystemBHttpClient implements SystemBClient {

  private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
      new ParameterizedTypeReference<>() {
      };

  private final WebClient webClient;
  private final SystemBMapper mapper;

  public SystemBHttpClient(
      @Qualifier("systemBWebClient") WebClient webClient,
      SystemBMapper mapper) {
    this.webClient = webClient;
    this.mapper = mapper;
  }

  private static SystemBException resolveException(HttpStatusCode status,
      Map<String, Object> details) {
      if (status == HttpStatus.NOT_FOUND) {
          return new SystemBNotFoundException(details);
      }
      if (status == HttpStatus.SERVICE_UNAVAILABLE) {
          return new SystemBUnavailableException(details);
      }
      if (status.is4xxClientError()) {
          return new SystemBClientErrorException(details);
      }
    return new SystemBServerErrorException(details);
  }

  @Override
  public Mono<InsuranceData> fetchById(String patientId) {
    return webClient.get().uri("/insurance/{id}", patientId).accept(MediaType.APPLICATION_XML)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response ->
            response.bodyToMono(MAP_TYPE)
                .onErrorResume(e -> Mono.just(Map.of()))
                .defaultIfEmpty(Map.of())
                .map(details -> resolveException(response.statusCode(), details)))
        .bodyToMono(SystemBResponse.class)
        .onErrorMap(WebClientRequestException.class, e -> new SystemBUnavailableException())
        .publishOn(Schedulers.boundedElastic())
        .map(mapper::map);
  }
}
