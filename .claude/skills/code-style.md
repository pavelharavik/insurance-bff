---
description: >
  Code style rules: Google Checkstyle, PMD, Java 25 idioms, naming conventions, comment policy.
---

## Automated enforcement

`mvn verify` runs:
- **Checkstyle** with `google_checks.xml` — format, import order, brace style, line length (100)
- **PMD** — common Java anti-patterns

Fix violations before reporting work done. The build fails on any Checkstyle or PMD error.

## Java 25 idioms

**Records** for immutable data carriers:

```java
// Prefer
public record InsuranceData(String id, String name, boolean active) {}

// Over a class with private fields, constructor, and getters
```

**Sealed interfaces + pattern matching** for exhaustive type hierarchies:

```java
public sealed interface SystemAException
    permits NotFound, Unavailable, ClientError, ServerError {}

// Switch expression over instanceof chain:
String msg = switch (exception) {
  case SystemAException.NotFound e    -> "not found";
  case SystemAException.Unavailable e -> "unavailable";
  case SystemAException.ClientError e -> "client error";
  case SystemAException.ServerError e -> "server error";
};
```

**var** for local variables when the type is obvious from the right-hand side:

```java
var data = new InsuranceData("1", "Alice", true); // clear — use var
var x = service.fetch();                           // unclear — spell out the type
```

## Naming conventions

| Role                         | Suffix       | Example                    |
|------------------------------|--------------|----------------------------|
| Spring service (use-case)    | `Service`    | `InsuranceService`         |
| REST controller              | `Controller` | `InsuranceController`      |
| Port interface (application) | `Client`     | `SystemAClient`            |
| HTTP adapter (infrastructure)| `HttpClient` | `SystemAHttpClient`        |
| Object mapper                | `Mapper`     | `SystemAMapper`            |
| Domain exception             | `Exception`  | `InsuranceNotFoundException`|
| Application exception        | `Exception`  | `SystemAException`         |
| HTTP response DTO            | `Response`   | `InsuranceResponse`        |
| HTTP request DTO             | `Request`    | `InsuranceSearchRequest`   |

## Comments policy

Write a comment only when the **why** is non-obvious:
- A hidden constraint from an upstream system
- A subtle invariant the code relies on
- A workaround for a known bug or quirk

**Never write:**
- Comments that restate what the code does
- `@param` / `@return` Javadoc that repeats the parameter name
- TODO comments without a linked task

**Acceptable example:**

```java
// .cache() prevents a second HTTP call when the error fallback re-subscribes
Mono<InsuranceData> monoA = clientA.fetchById(patientId).cache();
```

## Null policy

- Do not return `null` from any method in `domain` or `application`
- Do not use `null` as a sentinel — throw a typed exception or return an empty reactive type
- `Optional` is acceptable in domain value objects for truly optional fields
- `@JsonInclude(NON_NULL)` is acceptable in presentation DTOs for optional response fields

## Import order (enforced by Checkstyle)

Google style: static imports first, then `java.*`, then `javax.*`/`jakarta.*`,
then third-party, alphabetically within each group.
Use IntelliJ's "Optimize Imports" with the Google Java Style profile.
