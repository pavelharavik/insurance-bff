# Insurance BFF

A Backend-for-Frontend service that aggregates insurance data from two upstream systems (System A and System B) and exposes a single, unified REST API to consumers.

## Architecture

```
GET  /insurance/{patientId}
POST /insurance/search
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

| Priority | Upstream error | HTTP status | `errorCode` |
|----------|---------------|-------------|-------------|
| 4 (highest) | 5xx server error | 500 | `UPSTREAM_ERROR` |
| 3 | 4xx client error (non-404) | 500 | `UPSTREAM_ERROR` |
| 2 | 503 / connection failure | 503 | `SERVICE_UNAVAILABLE` |
| 1 (lowest) | 404 not found | 404 | `NOT_FOUND` |

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
# Look up by patient ID
curl http://localhost:8080/insurance/001

# Search by name and birth date
curl -X POST http://localhost:8080/insurance/search \
     -H 'Content-Type: application/json' \
     -d '{"firstName":"Alice","lastName":"Smith","birthDate":"1985-03-15"}'
```

WireMock stub data:

| Patient ID | First name | Last name | Birth date | System A | System B |
|------------|-----------|-----------|------------|----------|----------|
| `001` | Alice | Smith | 1985-03-15 | active | active |
| `002` | Bob | Johnson | 1990-01-15 | inactive | 404 |
| `003` | Carol | White | 1990-07-22 | 404 | active |

Any other input resolves to patient ID `999`, which returns 404 from both upstreams.

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

See [`openapi.yaml`](openapi.yaml) for the full specification.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/insurance/{patientId}` | Look up insurance by patient ID |
| `POST` | `/insurance/search` | Search by first name, last name, and birth date |

All error responses use the format `{"errorCode": "...", "message": "...", "errors": [...]}`.

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
