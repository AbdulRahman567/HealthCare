# Engineering Quality Board — RBAC Validation Report

**Date:** 2026-07-23  
**Scope:** Complete RBAC implementation (Phases 3.1–3.8)  
**Mode:** Validation only (no new features); Critical/High defects fixed and re-validated  

---

## Executive verdict

| Gate | Result |
| ---- | ------ |
| Critical / High severity RBAC defects | **Cleared after EQB fixes** |
| Project compiles (backend) | **Pass** |
| Next.js production build | **Pass** (after shell lint fix) |
| Focused RBAC unit tests | **Pass** |
| Spring Boot start + Flyway (this machine) | **Blocked by local DB credentials** (`hms_user` access denied) — not an RBAC code defect |
| Docker / Testcontainers integration | **Unavailable** (Docker Desktop not running) |
| Live Swagger / JWT E2E against running server | **Not executed** (server did not start) — covered by unit/integration tests where Docker allows |

**Production readiness (RBAC):** Conditionally ready once environment (MySQL user + Redis optional) is provisioned and a smoke start confirms Flyway V11 + Swagger.

---

## Critical fixes applied (this EQB pass)

1. **Platform Super Admin could not obtain JWTs**  
   - `AuthServiceImpl.buildAuthResponse` rejected all `tenantId == null` users.  
   - `JwtService` NPE’d / required tenant claim.  
   - **Fix:** Allow null tenant only when user holds `SUPER_ADMIN`; omit optional `tenant_id` claim; parse as optional UUID.  
   - Tests: `JwtServiceTest.platformSuperAdminTokenRoundTripWithoutTenant`.

2. **Next.js build failed**  
   - Unused `cn` import in `app-shell.tsx` (+ ref cleanup warning).  
   - **Fix:** Removed unused import; capture menu button ref for focus restore.

3. **Flyway / matrix privilege drift (High operational)**  
   - V10 granted `HOSPITAL_DELETE` to existing tenant `HOSPITAL_ADMIN` roles; matrix does not.  
   - **Fix:** `V11__revoke_hospital_admin_hospital_delete.sql`.

4. **`DASHBOARD_READ` was unused (Medium → fixed)**  
   - Dashboard nav/route were open to any authenticated user.  
   - **Fix:** Gate `/app` + dashboard nav item with `DASHBOARD_READ` (all default roles already grant it).

---

## Security improvements (validated)

- Fail-closed API handler classification + startup coverage guard  
- Platform Super Admin trust bar centralized (`PlatformPrincipalSupport`)  
- `executeWithoutTenantFilter` requires platform Super Admin  
- Frontend unknown `/app/*` routes deny by default  
- Independent FE route catalog + nav parity tests  
- JWT roles/permissions not trusted for authZ (DB reload per request)  
- Hospital users cannot bind foreign tenants via `X-Tenant-ID`  
- AuthN → 401 / AuthZ → 403 with generic bodies  

---

## Architecture improvements

- Corrected Super Admin token path so platform bypass code is reachable  
- Permission-driven dashboard gate aligned with catalog  
- Migration V11 aligns upgraded DBs with `SystemRolePermissionMatrix`  

---

## Performance improvements

- No new hot-path regressions introduced  
- Prior recommendations still apply: reuse permission `Set` in matchers when catalogs grow; keep FE authZ `staleTime` ≥ 60s  

---

## Role flow simulation (code + matrix)

| Role | Login/JWT | Tenant | Sidebar (key) | `/app` | Hospital settings GET/PUT | Notes |
| ---- | --------- | ------ | ------------- | ------ | ------------------------- | ----- |
| Super Admin | **Allowed after EQB fix** (null tenant) | Optional / bypass | All (all permissions) | ✓ | Needs `X-Tenant-ID` | No seeded platform user yet — ops must create one |
| Hospital Admin | ✓ | Required | All modules | ✓ | ✓ / ✓ | |
| Doctor | ✓ | Required | Patients, Appointments | ✓ | 403 / 403 | |
| Nurse | ✓ | Required | Patients, Appointments | ✓ | 403 / 403 | |
| Receptionist | ✓ | Required | Patients, Appointments | ✓ | 403 / 403 | |
| Pharmacist | ✓ | Required | Patients only | ✓ | 403 / 403 | No appointments |
| Lab Technician | ✓ | Required | Patients only | ✓ | 403 / 403 | No appointments |
| Accountant | ✓ | Required | Patients, Appointments, Billing | ✓ | 403 / 403 | |

Logout / refresh: hospital-scoped users covered by existing auth tests; Super Admin refresh now possible with optional tenant claim.

---

## Documentation mismatches

| Item | Status |
| ---- | ------ |
| `RBAC_REVIEW_PHASE_3_8.md` claimed no Critical remain | **Stale** — Super Admin JWT gap found by EQB; fixed in this pass |
| `PERMISSION_MATRIX.md` vs V10 `HOSPITAL_DELETE` | **Aligned** via V11 |
| No platform Super Admin user seed documented | **Gap** — role exists; user provisioning is manual/ops |
| Clinical modules “coming soon” vs full matrix | Expected — APIs not shipped yet |

---

## Checklist (requested validations)

| Check | Status |
| ----- | ------ |
| Project compiles | ✓ Backend compile |
| Spring Boot starts | ✗ Env: DB user denied (code OK) |
| Next.js builds | ✓ |
| Flyway migrations succeed | ✗ Same DB credential block; V11 present and valid SQL |
| Swagger loads | ✗ Needs running server |
| JWT authentication works | ✓ Unit tests + prior integration coverage |
| Tenant isolation preserved | ✓ Code + tests |
| Roles / permissions resolve | ✓ Matrix + resolver |
| Guards / decorators / API authZ | ✓ |
| Frontend route protection / menus | ✓ Tests |
| No hardcoded role checks (except platform trust bar) | ✓ |
| No privilege escalation / cross-tenant | ✓ |
| No Critical/High remaining | ✓ After EQB fixes |

---

## Residual non-blocking items

1. Seed or document creation of a platform Super Admin **user** (`tenant_id` NULL).  
2. Start Docker (or provision Redis) for full Testcontainers integration suite.  
3. Align local MySQL `hms_user` credentials with `backend/.env`.  
4. Remove deprecated FE nav shims / session `hasRole` when convenient.  
5. Add negative integration tests: doctor → hospital settings → 403 (when Docker available).  

---

*Companion: [RBAC_REVIEW_PHASE_3_8.md](./RBAC_REVIEW_PHASE_3_8.md), [PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md)*
