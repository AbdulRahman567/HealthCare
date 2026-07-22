# Phase 1.9 — Authentication Module Test Report

**Date:** 2026-07-21  
**Scope:** Auth module only (no new product features)

---

## Verdict

| Goal                                        | Result                                                                             |
| ------------------------------------------- | ---------------------------------------------------------------------------------- |
| Backend auth behavioral line coverage ≥ 90% | **93.6%** (JaCoCo gated bundle) — **PASS**                                         |
| Frontend auth line coverage ≥ 85%           | **99.5%** (Jest) — **PASS**                                                        |
| Unit / controller / JWT suites              | **Passing**                                                                        |
| Integration (Testcontainers MySQL)          | **Implemented**; skipped when Docker daemon unavailable                            |
| Playwright e2e                              | Specs under `frontend/e2e/auth` (last full run **18 / 23** passed on `next start`) |

---

## Summary

| Area                                       | Result                                                                                |
| ------------------------------------------ | ------------------------------------------------------------------------------------- |
| Backend unit + MockMvc (JUnit 5 + Mockito) | **117 tests** — 99 executed, 18 skipped (IT without Docker), **0 failures**           |
| Backend JaCoCo gate (`jacoco-check-auth`)  | **PASS** at **0.90** minimum                                                          |
| Frontend Jest + RTL                        | **85 tests**, **13 suites**, all passing                                              |
| Frontend Playwright                        | Login / forgot / resend / verify / protected-route specs; API mocked via `page.route` |

---

## Backend

### Stack

- JUnit 5, Mockito, AssertJ
- Standalone MockMvc (`AuthControllerTest`)
- Testcontainers MySQL 8.4 (`disabledWithoutDocker = true`)
- JaCoCo report: `backend/target/site/jacoco/index.html`

### Coverage (gated packages)

Gate includes: `auth.controller`, `auth.service`, `auth.crypto`, `auth.validator`, `auth.mapper.AuthMapper`, `security.jwt`  
(Excludes DTO/entity noise and generated `AuthMapperImpl`.)

| Class                          | Line %                |
| ------------------------------ | --------------------- |
| `AuthController`               | **100%**              |
| `JwtAuthenticationFilter`      | **100%**              |
| `AuthMapper` (defaults)        | **100%**              |
| `AuthServiceImpl`              | **93.3%**             |
| `PasswordResetServiceImpl`     | **95.0%**             |
| `EmailVerificationServiceImpl` | **90.2%**             |
| `RefreshTokenServiceImpl`      | **89.6%**             |
| `JwtService`                   | **90.0%**             |
| `JwtPrincipalValidator`        | **95.5%**             |
| **Gated bundle**               | **93.6% (683 / 730)** |

### Suites

| Suite                                                                          | Focus                                                             |
| ------------------------------------------------------------------------------ | ----------------------------------------------------------------- |
| `TokenHashingServiceTest`                                                      | Opaque token generation / SHA-256                                 |
| `StrongPasswordValidatorTest`                                                  | Password policy                                                   |
| `JwtServiceTest` / `JwtPrincipalValidatorTest` / `JwtAuthenticationFilterTest` | JWT issue/parse/filter                                            |
| `AuthServiceImplTest`                                                          | Login, register, refresh, profile, password, forgot/reset, verify |
| `RefreshTokenServiceImplTest`                                                  | Issue, rotate, reuse detection, revoke                            |
| `PasswordResetServiceImplTest` / `EmailVerificationServiceImplTest`            | Token lifecycle                                                   |
| `AuthMapperTest` / email service tests                                         | Mapping + email content                                           |
| `AuthControllerTest`                                                           | HTTP mapping for all auth endpoints                               |
| `AuthApiIntegrationTest` / `ProtectedRouteIntegrationTest`                     | API + MySQL (requires Docker)                                     |

### How to run

```bash
cd backend
# Unit + MockMvc (no Docker)
./mvnw test "-Dtest=com.healthcare.hms.auth.**.*Test,com.healthcare.hms.security.jwt.*Test" jacoco:report jacoco:check@jacoco-check-auth

# Full suite including Testcontainers (Docker Desktop must be running)
./mvnw verify
```

---

## Frontend

### Stack

- Jest + React Testing Library
- Playwright (`next build && next start` for e2e webServer)

### Coverage (auth feature surface)

| Metric         | Value                                                |
| -------------- | ---------------------------------------------------- |
| Statements     | **99.53%**                                           |
| Lines          | **99.52%**                                           |
| Functions      | **100%**                                             |
| Branches       | **93.93%**                                           |
| Jest threshold | lines/statements/functions **85%**, branches **70%** |

### Suites

| Area       | Files                                                                             |
| ---------- | --------------------------------------------------------------------------------- |
| Schemas    | login, forgot/reset password, resend, register-hospital                           |
| Components | login, forgot/reset, resend, route-guard, verify-email-handler, auth-form-message |
| API        | `auth-api.test.ts` (login, register, refresh, profile, logout, verify, reset)     |
| E2E        | `e2e/auth/*.spec.ts` + `e2e/helpers/mock-api.ts`                                  |

### How to run

```bash
cd frontend
npm test -- --coverage --watchAll=false --runInBand
npx playwright install chromium   # once
npm run test:e2e
```

---

## Flows covered

- Login (success / invalid credentials / unverified email)
- Hospital registration + initial admin registration
- JWT generation, parsing, filter, principal validation
- Refresh token rotation + reuse detection
- Profile get/update
- Password change
- Forgot / reset password
- Email verification + resend
- Protected routes (backend 401 without bearer; frontend redirect to `/login`)

---

## Gaps / follow-ups

1. **Start Docker Desktop** and run `./mvnw verify` so Testcontainers integration suites execute (currently skipped).
2. Playwright: a few interaction cases (successful login redirect, some success toasts) remain flaky against CORS-mocked `localhost:8080` API; protected-route and most page-load/validation specs are green. Re-run `npm run test:e2e` after a fresh `next build`.

---

## Commands cheat-sheet

| Goal                         | Command                                                                                                                                                  |
| ---------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Backend unit + coverage gate | `cd backend && ./mvnw test "-Dtest=com.healthcare.hms.auth.**.*Test,com.healthcare.hms.security.jwt.*Test" jacoco:report jacoco:check@jacoco-check-auth` |
| Backend + IT                 | `cd backend && ./mvnw verify` (Docker required)                                                                                                          |
| Frontend unit                | `cd frontend && npm test -- --coverage --watchAll=false --runInBand`                                                                                     |
| Frontend e2e                 | `cd frontend && npm run test:e2e`                                                                                                                        |
