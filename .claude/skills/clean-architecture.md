---
description: >
  Check where a new class belongs, or verify that existing code respects layer dependency rules.
---

## Layer map

```
com.insurance.bff
├── domain/           No Spring. No framework. Pure Java.
│   └── insurance/    InsuranceData (record), InsuranceNotFoundException,
│                     InsuranceDataUnavailableException, UpstreamErrorType (enum)
│
├── application/      @Service beans and port interfaces. Imports domain.* only.
│   └── insurance/    InsuranceService
│                     SystemAClient (interface), SystemBClient (interface)
│                     SystemAException (sealed), SystemBException (sealed)
│
├── infrastructure/   @Component adapters. Imports application.* and domain.*.
│   ├── systema/      SystemAHttpClient, SystemAMapper, SystemAResponse
│   ├── systemb/      SystemBHttpClient, SystemBMapper, SystemBResponse
│   └── (root)        WebClientConfig, UpstreamProperties
│
└── presentation/     @RestController, @RestControllerAdvice. Imports application.* and domain.*.
    ├── insurance/    InsuranceController, InsuranceResponse, InsuranceSearchRequest
    └── (root)        GlobalExceptionHandler, ApiException, ErrorResponse,
                      TopLevelErrorCode, FieldErrorCode
```

## Allowed imports per layer

| Layer            | May import                                                      |
|------------------|-----------------------------------------------------------------|
| `domain`         | JDK only                                                        |
| `application`    | `domain.*`, JDK, Spring stereotype annotations (`@Service`)     |
| `infrastructure` | `application.*`, `domain.*`, Spring, WebClient, Jackson         |
| `presentation`   | `application.*`, `domain.*`, Spring WebFlux, Jakarta Validation |

## Decision guide for a new class

**1. Core business concept — entity, value object, domain exception?**
→ `domain/insurance/`

**2. Use-case logic, orchestration, or port interface (implemented by infrastructure)?**
→ `application/insurance/`

**3. Calls an external HTTP system, maps external DTOs, or provides infrastructure config?**
→ `infrastructure/systema/` or `infrastructure/systemb/` (or `infrastructure/` root for config)

**4. HTTP entry point, request/response DTO, or HTTP error handler?**
→ `presentation/insurance/` for scenario-specific code
→ `presentation/` root for shared components (e.g., `GlobalExceptionHandler`)

**5. Used across multiple business scenarios?**
→ Layer root, not inside `insurance/`

## Violation examples

```java
// WRONG: infrastructure imports presentation
import com.insurance.bff.presentation.ErrorResponse; // inside SystemAHttpClient

// WRONG: application imports infrastructure
import com.insurance.bff.infrastructure.systema.SystemAHttpClient; // inside InsuranceService
// (InsuranceService must import the port interface: application.insurance.systema.SystemAClient)

// WRONG: domain imports Spring
import org.springframework.stereotype.Service; // inside InsuranceData

// WRONG: domain exception in GlobalExceptionHandler
@ExceptionHandler(InsuranceNotFoundException.class) // must live in InsuranceController instead
```
