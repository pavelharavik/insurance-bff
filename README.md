# Insurance BFF

A Backend-for-Frontend service that aggregates insurance data from two upstream systems (System A and System B) and exposes a single, unified REST API to consumers.

## Architecture

```
GET /insurance/{patientId}
         │
         ▼
InsuranceController
         │
         ▼
InsuranceService  ──── @Cacheable("insurance") ──── Caffeine (60 s TTL, max 10 000 entries)
         │
         │  StructuredTaskScope — two virtual threads run concurrently
         │
         ├──▶ SystemAClient  (@CircuitBreaker "system-a")
         │      GET /patients/{id}/insurance   (JSON)
         │      SystemAResponse → SystemAMapper → InsuranceData
         │
         └──▶ SystemBClient  (@CircuitBreaker "system-b")
                GET /insurance/{id}            (XML)
                SystemBResponse → SystemBMapper → InsuranceData

First successful InsuranceData wins → InsuranceResponse → HTTP 200 JSON
```

When both upstreams fail, the error with the highest priority is returned: **500 > 503 > 404**.

## Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 25+     |
| Maven | 3.9+  |
| Docker + Docker Compose | any recent |

## Running locally

```bash
# Build the JAR first
mvn package -DskipTests

# Start the BFF (port 8080) + WireMock stubs (System A: 8081, System B: 8082)
docker-compose up
```

Try it:

```bash
curl http://localhost:8080/insurance/001
```

WireMock stub data:

| Patient ID | System A | System B | Expected result |
|------------|----------|----------|-----------------|
| `001` | Alice Smith, active | Alice Smith, active | 200 — first responder wins |
| `002` | Bob Johnson, inactive | 404 | 200 — System A wins |
| `003` | 404 | Carol White, active | 200 — System B wins |

## Building and testing

```bash
# Compile, test, and verify coverage (JaCoCo enforces ≥ 80% line coverage)
mvn verify

# Tests only, skip coverage check
mvn test
```

## Configuration

All upstream settings are bound via `UpstreamProperties` from `application.yml`:

```yaml
upstream:
  system-a:
    url: http://localhost:8081        # override with SYSTEM_A_URL env var
    connect-timeout-ms: 2000
    read-timeout-ms: 5000
  system-b:
    url: http://localhost:8082        # override with SYSTEM_B_URL env var
    connect-timeout-ms: 2000
    read-timeout-ms: 5000
```

Circuit breaker settings (Resilience4j, count-based window of 10 calls, 50% threshold):

```yaml
resilience4j.circuitbreaker.instances:
  system-a:
    waitDurationInOpenState: 30s
    permittedNumberOfCallsInHalfOpenState: 3
    ignoredExceptions:
      - com.insurance.bff.exception.InsuranceNotFoundException
  system-b:  # same settings
```

## API

See [`openapi.yaml`](openapi.yaml) for the full OpenAPI 3.1 specification.

### `GET /insurance/{patientId}`

Returns the insurance record for the given patient, sourced from whichever upstream responds successfully first.

**Path parameters**

| Name | Type | Description |
|------|------|-------------|
| `patientId` | string | Patient identifier |

**Responses**

| Status | Description |
|--------|-------------|
| `200 OK` | Insurance record found |
| `404 Not Found` | No record in either upstream |
| `500 Internal Server Error` | One or both upstreams returned 5xx |
| `503 Service Unavailable` | Both upstreams timed out or are unavailable |

**200 response body**

```json
{
  "id":           "001",
  "name":         "Alice Smith",
  "is_active":    true,
  "current_date": "2026-06-01T10:00:00Z"
}
```

**Error response body** (RFC 7807 Problem Details)

```json
{
  "type":   "about:blank",
  "title":  "Not Found",
  "status": 404,
  "detail": "No insurance record found for patient: 001"
}
```

## Observability

Actuator endpoints are available at `/actuator`:

| Endpoint | URL |
|----------|-----|
| Health (full details) | `GET /actuator/health` |
| Info | `GET /actuator/info` |
| Circuit breaker states | `GET /actuator/circuitbreakers` |
