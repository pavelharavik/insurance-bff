---
description: >
  Run after implementing or modifying production code.
  Identifies required tests, writes missing ones, runs mvn test, and reports results.
---

Follow this workflow for every task that touches production code.

## Step 1 — Identify changed production files

List all files modified or created under `src/main/java/`.

## Step 2 — Locate or create test files

For each changed class in `src/main/`, find or create the corresponding `*Test.java`
in `src/test/java/` at the same package path:

```
src/main/java/com/insurance/bff/application/insurance/InsuranceService.java
→ src/test/java/com/insurance/bff/application/insurance/InsuranceServiceTest.java
```

## Step 3 — Determine required test cases

Apply this guide by layer:

**domain exceptions**
- Constructor with mandatory fields — assert `getMessage()` text and field getters
- Constructor variants (with/without `detail`) if multiple exist

**application/InsuranceService**
- Both clients succeed → returns either result
- Only A succeeds → returns A result
- Only B succeeds → returns B result
- Both return 404 → `InsuranceNotFoundException`
- Mixed errors → correct `UpstreamErrorType` wins based on priority (ERROR > CLIENT_ERROR > UNAVAILABLE > not found)
- Detail message propagated from higher-priority error
- Search overload: known patient resolves to ID, unknown patient falls back to ID "999"

**infrastructure mappers**
- Each response field maps to the correct `InsuranceData` field
- Null or missing optional fields handled without NPE

**presentation controllers**
- 200: service returns data → correct JSON fields including `current_date`
- 404: service throws `InsuranceNotFoundException` → `errorCode: NOT_FOUND`, correct message
- 503: service throws `InsuranceDataUnavailableException(UNAVAILABLE)` → `errorCode: SERVICE_UNAVAILABLE`
- 500: service throws `InsuranceDataUnavailableException(ERROR)` → `errorCode: UPSTREAM_ERROR`
- 500: service throws `InsuranceDataUnavailableException(CLIENT_ERROR)` → `errorCode: UPSTREAM_ERROR`
- 400: each required field missing or blank → `errorCode: VALIDATION_ERROR`, correct `field` and `errorCode` in `errors[0]`

## Step 4 — Write missing or update existing tests

Add test cases for all new or changed code paths.
Negative scenarios (exception types, validation constraints) are the most commonly missed —
check them explicitly.

## Step 5 — Run unit tests

```bash
mvn test
```

## Step 6 — Report result

State: N tests passed, M failed.
If failures exist, diagnose and fix before proceeding.
Do not mark the task done while any test is red.

## Step 7 — Coverage note

Full coverage enforcement (95% line) runs on `mvn verify`.
Run it only when explicitly asked or before a release check.
