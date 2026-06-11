# CLI Rules

## Commits

Never create git commits. The user commits all changes.

## Tests after every code change

After modifying any file under `src/main/`, run `mvn test` and confirm it passes
before reporting the task as done. This is mandatory, not optional.

## Write tests in the same response

When writing new production code, always write the corresponding unit test in the same
response. Do not defer tests to a follow-up message.

## When to use mvn verify vs mvn test

Run `mvn verify` only when explicitly asked to:
- check coverage
- run integration tests
- do a full build

Do not use `mvn verify` for routine unit-test checks — it is significantly slower and
runs Checkstyle, PMD, and integration tests unnecessarily.

## Never skip tests or coverage

Do not use `-DskipTests`, `-DskipCoverageCheck`, or `--no-verify`.
If tests fail, diagnose and fix the root cause.

## No speculative additions

Implement exactly what was requested. Do not add extra error codes, extra endpoints,
extra validation constraints, or extra exception types that were not asked for.
