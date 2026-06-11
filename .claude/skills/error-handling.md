---
description: >
  Guide for adding a new domain exception or mapping a new error case in a controller.
  Ensures the error handling chain is followed correctly.
---

Use this guide whenever you are:
- Adding a new domain exception
- Adding a new controller endpoint that can fail
- Adding a new upstream error classification

## Adding a new domain exception

**Step 1 — Create the exception in `domain/insurance/`**

Plain Java class, no Spring annotations:

```java
public class SomethingNotFoundException extends RuntimeException {

  private final String id;

  public SomethingNotFoundException(String id) {
    super("No record found for: " + id);
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
```

**Step 2 — Throw it from the service (application layer)**

Translate an upstream exception into the domain exception inside `InsuranceService`
(or the relevant use-case service). The service must not import or reference HTTP concepts.

**Step 3 — Map it in the controller (presentation layer)**

In the controller method that calls the service, add `.onErrorMap(...)`:

```java
.onErrorMap(SomethingNotFoundException.class,
    ex -> new ApiException(HttpStatus.NOT_FOUND, TopLevelErrorCode.NOT_FOUND, ex.getMessage()))
```

**Step 4 — Do NOT touch `GlobalExceptionHandler`**

`GlobalExceptionHandler` handles `ApiException` and `WebExchangeBindException` only.
Never add a handler for a domain exception there.

**Step 5 — Write the controller test case**

In `InsuranceControllerTest` (or the relevant `*ControllerTest`), stub the service to throw
the new exception and assert the HTTP status and `errorCode`:

```java
@Test
void endpoint_returnsXxx_onSomethingNotFoundException() {
  when(service.method(any())).thenReturn(Mono.error(new SomethingNotFoundException("id")));

  webTestClient.get().uri("/endpoint/id")
      .exchange()
      .expectStatus().isNotFound()
      .expectBody()
      .jsonPath("$.errorCode").isEqualTo("NOT_FOUND")
      .jsonPath("$.message").isEqualTo("No record found for: id");
}
```

## Adding a new UpstreamErrorType

If a new upstream failure classification is needed:

1. Add the value to `UpstreamErrorType` enum in `domain/insurance/`
2. Add the corresponding message in `InsuranceDataUnavailableException.messageFor()`
3. Add the HTTP status mapping in `InsuranceController.toHttpStatus()` and `toErrorCode()`
4. Add test cases in `InsuranceControllerTest` (HTTP path) and `InsuranceServiceTest` (priority)

## ErrorResponse.errors contract

| Scenario               | `errors` field              |
|------------------------|-----------------------------|
| 400 validation failure | list of `FieldError` objects |
| all other HTTP errors  | empty list `[]`              |

Never populate `errors` outside of 400 validation responses.
The field must always be present — empty list, not absent, not null.
