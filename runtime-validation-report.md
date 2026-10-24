# Runtime Validation Report

**Generated**: 2026-10-24  
**Target**: `D:\java_ecommerce\airfrance\airfrance`

## Summary

| Step | Status | Exit Code | Details |
| --- | --- | --- | --- |
| Application startup | PASS | 0 | `mvn test` started Spring Boot and embedded Tomcat on a random port |
| Unit and integration tests | PASS | 0 | 20 tests passed; 0 failures, errors, or skips |
| HTTP API endpoint tests | PASS | 0 | 9 tests across three suites; live HTTP calls exercised every API route |
| Browser tests | N/A | n/a | API-only backend; no rendered UI |

**Overall**: PASS

## Environment and test strategy

- Java: 21 (Temurin)
- Database: embedded H2; product and revision data are cleared before each API test
- Docker: unavailable; `docker info` reported that the daemon was not responding
- Node.js: available (`v26.7.0`)
- Playwright: not required for this JSON REST API
- HTTP test client: Java `HttpClient` against a Spring Boot `RANDOM_PORT` server

## Endpoint coverage

| Endpoint | Coverage |
| --- | --- |
| `GET /api/v1/products` | Search, pagination, localized listing, pagination validation, active-only visibility |
| `GET /api/v1/products/{id}` | English/French details, not found, inactive product hidden |
| `GET /api/v2/products` | Listing with both translations, active-only visibility |
| `GET /api/v2/products/{id}` | Bilingual detail, not found, inactive product hidden |
| `POST /api/v1/auth/token` | Valid login, invalid credentials, invalid request body |
| `GET /api/v1/admin/products` | Admin listing and filters; missing/invalid-token denial |
| `POST /api/v1/admin/products` | Creation, validation, duplicate SKU conflict; missing-token denial |
| `PUT /api/v1/admin/products/{id}` | Edit and persisted update, missing product, missing-token denial |
| `GET /api/v1/admin/products/{id}/revisions` | Revision history and attribution, missing product, missing-token denial |
| `POST /api/v1/admin/products/{id}/revisions/{revision}/restore` | Restore as a new revision, missing product, missing-token denial |

## Test files

- `src/test/java/com/airfrance/airfrance/api/PublicProductEndpointsTest.java`
- `src/test/java/com/airfrance/airfrance/api/AdminProductEndpointsTest.java`
- `src/test/java/com/airfrance/airfrance/api/ProductApiSecurityEndpointsTest.java`
- Existing service, mapper, and language tests remain part of `mvn test`.

## Evidence

- Command: `mvn test`
- Exit code: `0`
- Surefire reports: `target/surefire-reports/`
- Total: 20 tests, 0 failures, 0 errors, 0 skipped

## Issues found and fixed

- Spring Security's catch-all rule blocked error dispatches, transforming expected API error statuses. Error dispatches are now permitted while API routes retain their authorization rules.
- Controller parameter constraint violations were returned as HTTP 500. The API now maps these validation failures to HTTP 400.

## Known gaps

- Tests use H2 rather than the production database engine; database-specific behavior is not verified.
- Browser rendering is not applicable because this project exposes a JSON API only.
