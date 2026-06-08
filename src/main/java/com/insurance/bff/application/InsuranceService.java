package com.insurance.bff.application;

import com.insurance.bff.application.exception.SystemAException;
import com.insurance.bff.application.exception.SystemBException;
import com.insurance.bff.application.port.SystemAClient;
import com.insurance.bff.application.port.SystemBClient;
import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.exception.InsuranceNotFoundException;
import com.insurance.bff.domain.exception.UpstreamErrorType;
import com.insurance.bff.domain.model.InsuranceData;
import java.util.Map;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Orchestrates parallel insurance lookups across System A and System B.
 *
 * <p>Both upstreams are subscribed to concurrently. The first successful result wins.
 * If both fail, the error with the highest priority is propagated: {@link UpstreamErrorType#ERROR}
 * &gt; {@link UpstreamErrorType#CLIENT_ERROR} &gt; {@link UpstreamErrorType#UNAVAILABLE} &gt; not
 * found.
 */
@Service
public class InsuranceService {

  private final SystemAClient clientA;
  private final SystemBClient clientB;

  public InsuranceService(SystemAClient clientA, SystemBClient clientB) {
    this.clientA = clientA;
    this.clientB = clientB;
  }

  /**
   * @param patientId the patient ID to look up
   * @return a {@link Mono} emitting data from whichever upstream responds first with 200
   */
  public Mono<InsuranceData> getInsuranceData(String patientId) {
    Mono<InsuranceData> monoA = clientA.fetchById(patientId).cache();
    Mono<InsuranceData> monoB = clientB.fetchById(patientId).cache();

    return Mono.firstWithValue(monoA, monoB)
        .onErrorResume(e -> captureError(monoA).zipWith(captureError(monoB))
            .flatMap(t -> Mono.error(selectByPriority(t.getT1(), t.getT2(), patientId))));
  }

  private Mono<RuntimeException> captureError(Mono<InsuranceData> mono) {
    return mono
        .flatMap(ignored -> Mono.<RuntimeException>empty())
        .onErrorResume(e -> Mono.just((RuntimeException) e));
  }

  private RuntimeException selectByPriority(RuntimeException errorA, RuntimeException errorB,
      String patientId) {
    RuntimeException winner = priority(errorA) >= priority(errorB) ? errorA : errorB;
    String detail = extractDetail(winner);
    return switch (winner) {
      case SystemAException.NotFound _, SystemBException.NotFound _ ->
          new InsuranceNotFoundException(patientId);
      case SystemAException.Unavailable _, SystemBException.Unavailable _ ->
          new InsuranceDataUnavailableException(UpstreamErrorType.UNAVAILABLE, detail);
      case SystemAException.ClientError _, SystemBException.ClientError _ ->
          new InsuranceDataUnavailableException(UpstreamErrorType.CLIENT_ERROR, detail);
      case SystemAException.ServerError _, SystemBException.ServerError _ ->
          new InsuranceDataUnavailableException(UpstreamErrorType.ERROR, detail);
      default -> new InsuranceDataUnavailableException(UpstreamErrorType.ERROR, detail);
    };
  }

  private static String extractDetail(RuntimeException e) {
    Map<String, Object> details = switch (e) {
      case SystemAException ex -> ex.getDetails();
      case SystemBException ex -> ex.getDetails();
      default -> Map.of();
    };
    return details.isEmpty() ? null : details.toString();
  }

  private int priority(RuntimeException e) {
    return switch (e) {
      case SystemAException.NotFound _, SystemBException.NotFound _ -> 1;
      case SystemAException.Unavailable _, SystemBException.Unavailable _ -> 2;
      case SystemAException.ClientError _, SystemBException.ClientError _ -> 3;
      case SystemAException.ServerError _, SystemBException.ServerError _ -> 4;
      default -> 0;
    };
  }
}
