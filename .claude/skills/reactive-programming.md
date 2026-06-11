---
description: >
  Project Reactor patterns used in this codebase.
  Reference when writing or reviewing reactive chains in InsuranceService, HTTP clients, or controllers.
---

## Patterns in use

### Race two sources — first success wins

Used in `InsuranceService.getInsuranceData()`:

```java
Mono<InsuranceData> monoA = clientA.fetchById(patientId).cache();
Mono<InsuranceData> monoB = clientB.fetchById(patientId).cache();

return Mono.firstWithValue(monoA, monoB)
    .onErrorResume(e -> captureError(monoA).zipWith(captureError(monoB))
        .flatMap(t -> Mono.error(selectByPriority(t.getT1(), t.getT2(), patientId))));
```

`.cache()` is required: without it each subscription triggers a new HTTP call.
`Mono.firstWithValue` and the error fallback both subscribe independently — caching
ensures a single upstream request per source.

### Translating exceptions in controllers

Use `.onErrorMap` (not `.onErrorResume`) when translating one exception type to another
without switching to a different publisher:

```java
.onErrorMap(InsuranceNotFoundException.class,
    ex -> new ApiException(HttpStatus.NOT_FOUND, TopLevelErrorCode.NOT_FOUND, ex.getMessage()))
```

`.onErrorResume` is appropriate only when switching to a fallback `Mono`.

### Capturing errors for comparison

Used in `InsuranceService.captureError()` to inspect both upstream failures:

```java
private Mono<RuntimeException> captureError(Mono<InsuranceData> mono) {
  return mono
      .flatMap(ignored -> Mono.<RuntimeException>empty())
      .onErrorResume(e -> Mono.just((RuntimeException) e));
}
```

### Testing with StepVerifier

Always terminate with `.verify()`, `.verifyComplete()`, or `.verifyError(...)`:

```java
// Success
StepVerifier.create(mono)
    .expectNext(expected)
    .verifyComplete();

// Typed error
StepVerifier.create(mono)
    .expectError(InsuranceNotFoundException.class)
    .verify();

// Error with field assertion
StepVerifier.create(mono)
    .expectErrorSatisfies(ex -> {
      assertThat(ex).isInstanceOf(InsuranceDataUnavailableException.class);
      assertThat(((InsuranceDataUnavailableException) ex).getType())
          .isEqualTo(UpstreamErrorType.ERROR);
    })
    .verify();
```

Omitting `.verify()` means the chain never subscribes — the test always passes silently.

## Rules

- No `.block()` in production code — it blocks the Reactor thread pool
- No `Thread.sleep()` in production code
- When a `Mono` is consumed more than once in a single chain, apply `.cache()`
- Prefer `flatMap` over `map` when the transform itself returns a `Mono`
- Prefer `Mono.error(exception)` over throwing inside a lambda
- `Mono.fromCallable(() -> blockingOp())` requires an explicit bounded-elastic scheduler;
  do not use it on the default scheduler
