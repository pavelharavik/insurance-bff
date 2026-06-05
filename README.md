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
InsuranceService  ── Mono.firstWithValue() ── both upstreams subscribed concurrently
         │
         ├──▶ SystemAClient
         │      GET /patients/{id}/insurance   (JSON)
         │      SystemAResponse → SystemAMapperImpl → InsuranceData
         │
         └──▶ SystemBClient
                GET /insurance/{id}            (XML, decoded on boundedElastic)
                SystemBResponse → SystemBMapperImpl → InsuranceData

First successful InsuranceData wins → InsuranceResponse → HTTP 200 JSON
```

When both upstreams fail, the error with the highest priority is propagated:

| Priority | Upstream error | HTTP status |
|----------|---------------|-------------|
| 4 (highest) | 5xx server error | 500 |
| 3 | 4xx client error (non-404) | 500 |
| 2 | 503 / connection failure | 503 |
| 1 (lowest) | 404 not found | 404 |

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
# Compile, test, and verify coverage (JaCoCo enforces ≥ 95% line coverage)
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

## IDE setup

### IntelliJ IDEA — Google Java Style

The file [config/intellij-java-google-style.xml](config/intellij-java-google-style.xml) contains the official Google code style scheme for IntelliJ IDEA. It matches the Checkstyle rules enforced by the build (`google_checks.xml`).

**Installation:**

1. Open **Settings** → **Editor** → **Code Style** → **Java**
2. Click the gear icon (⚙) next to the scheme name → **Import Scheme** → **IntelliJ IDEA code style XML**
3. Select `config/intellij-java-google-style.xml`
4. Click **OK** — the scheme **GoogleStyle** will appear in the list
5. Select **GoogleStyle** as the active scheme and click **Apply**

After applying, use **Code** → **Reformat Code** (`Ctrl+Alt+L`) to auto-format files to the Google style.

> **Tip:** Enable **Reformat code** and **Optimize imports** in **Settings** → **Tools** → **Actions on Save** to keep files consistently formatted on every save.

## Observability

Actuator endpoints are available at `/actuator`:

| Endpoint | URL |
|----------|-----|
| Health (full details) | `GET /actuator/health` |
| Info | `GET /actuator/info` |
