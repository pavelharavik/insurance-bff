# Insurance BFF

BFF (Backend for Frontend) that aggregates patient insurance data from System A (REST/JSON)
and System B (REST/XML) using Spring WebFlux. Both upstreams are queried in parallel;
the first successful result wins.

## Package Structure

```
com.insurance.bff
├── domain/           # Pure domain: entities, domain exceptions, value objects
│   └── insurance/    # InsuranceData, InsuranceNotFoundException,
│                     # InsuranceDataUnavailableException, UpstreamErrorType
├── application/      # Use cases and port interfaces (depends only on domain)
│   └── insurance/    # InsuranceService, SystemAClient, SystemBClient,
│                     # SystemAException, SystemBException
├── infrastructure/   # HTTP adapters, mappers, config (depends on application + domain)
│   ├── systema/      # SystemAHttpClient, SystemAMapper, SystemAResponse
│   ├── systemb/      # SystemBHttpClient, SystemBMapper, SystemBResponse
│   └── (root)        # WebClientConfig, UpstreamProperties
└── presentation/     # Controllers, DTOs, error handling (depends on application + domain)
    ├── insurance/    # InsuranceController, InsuranceResponse, InsuranceSearchRequest
    └── (root)        # GlobalExceptionHandler, ApiException, ErrorResponse,
                      # TopLevelErrorCode, FieldErrorCode
```

**Dependency rules — never violate:**
- `domain` → no project dependencies
- `application` → imports `domain.*` only
- `infrastructure` and `presentation` → import `application.*` and `domain.*`
- Cross-shortcuts (e.g., `presentation` importing `infrastructure` types) are forbidden

**Package placement:**
- Business-scenario-specific classes → `insurance/` sub-package of their layer
- Shared across scenarios → layer root (no cross-scenario code currently exists)

## Error Handling

### The invariant

Domain and application exceptions **must not reach `GlobalExceptionHandler` directly.**
Controllers translate them to `ApiException`; `GlobalExceptionHandler` maps `ApiException` to HTTP.

```
application throws domain exception
    → controller.onErrorMap(DomainException.class,
          ex -> new ApiException(status, errorCode, ex.getMessage()))
    → GlobalExceptionHandler.handle(ApiException)
    → ResponseEntity<ErrorResponse>
```

### GlobalExceptionHandler handles exactly two types

| Exception                  | Behaviour                                              |
|----------------------------|--------------------------------------------------------|
| `ApiException`             | maps HTTP status + `errorCode` + `message`             |
| `WebExchangeBindException` | 400 with `VALIDATION_ERROR` + populated `errors` list  |

**Never add a domain or application exception handler to `GlobalExceptionHandler`.**

### ErrorResponse structure

```json
{ "errorCode": "NOT_FOUND", "message": "...", "errors": [] }
```

The `errors` array is **populated only for 400 validation errors.**
For all other error responses it must be an empty list — not absent, not null.

### UpstreamErrorType → HTTP mapping (InsuranceController)

| UpstreamErrorType | HTTP | TopLevelErrorCode    |
|-------------------|------|----------------------|
| `UNAVAILABLE`     | 503  | `SERVICE_UNAVAILABLE`|
| `CLIENT_ERROR`    | 500  | `UPSTREAM_ERROR`     |
| `ERROR`           | 500  | `UPSTREAM_ERROR`     |

## Testing

### Source locations

| Test type       | Source root           | JUnit tag              |
|-----------------|-----------------------|------------------------|
| Unit + slice    | `src/test/java`       | (none)                 |
| Integration     | `src/integration/java`| `@Tag("integration")`  |

### Commands

```bash
mvn test                              # unit tests only
mvn verify                            # unit + integration + coverage + Checkstyle + PMD
mvn test -Dtest=InsuranceControllerTest  # single class
```

### Coverage

Minimum **95% line coverage** enforced by JaCoCo on `mvn verify`.
Never skip with `-DskipCoverageCheck`.

### Test patterns by layer

| Layer                      | Approach                                                                              |
|----------------------------|---------------------------------------------------------------------------------------|
| `domain`                   | Pure unit, no Spring, no Mockito; assert fields and message text                      |
| `application/InsuranceService` | Lambda mocks for `SystemAClient`/`SystemBClient`; `StepVerifier` assertions; cover success, `InsuranceNotFoundException`, each `UpstreamErrorType`, priority selection |
| `infrastructure` mappers   | Pure unit; each field mapping, null/empty variants                                    |
| `presentation` controllers | `@WebFluxTest`; `@MockitoBean InsuranceService`; each HTTP status path; JSON body assertions via `WebTestClient` |

### Required workflow

After implementing or modifying **any** production code:

1. Write or update the corresponding `*Test.java` in `src/test/java`
2. Run `mvn test`
3. Fix all failures before reporting done — never skip or comment out assertions
4. *(Recommended)* Self-review: re-read the diff, check for rule violations

## Tech Stack

- Java 25 — records, sealed interfaces, pattern matching in switch expressions
- Spring Boot 3.4.5 with Spring WebFlux
- Project Reactor (`Mono` / `Flux`)
- Jackson with XML support (System B speaks XML)
- springdoc-openapi 2.8.8 for Swagger UI
- WireMock (`wiremock-spring-boot 3.2.0`) for integration tests
- JaCoCo 0.8.14, Checkstyle (Google style), PMD

## Code Conventions

- **No comments that explain what** — well-named identifiers do that
- **Add a comment only when the why is non-obvious**: hidden constraint, subtle invariant, upstream quirk
- Records for immutable data; sealed interfaces + pattern matching for exhaustive type hierarchies
- No `null` in domain objects; use empty reactive types or typed exceptions
- Naming: `*Service`, `*Controller`, `*Client` (port interface), `*HttpClient` (adapter),
  `*Mapper`, `*Exception`, `*Response`, `*Request`
- Javadoc only on public API where intent is not obvious from the signature
