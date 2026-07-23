# Phase 3.8 — RBAC Review Report

**Date:** 2026-07-23  
**Scope:** Architecture, security, backend, frontend, permissions, role hierarchy, guards, decorators, API protection, navigation, cross-tenant authorization, privilege escalation  
**Verdict:** **No Critical issues remain** after EQB hardening (including Super Admin JWT path).

> Note: An Engineering Quality Board pass (2026-07-23) found and fixed a Critical gap where
> platform Super Admin (`tenantId == null`) could not obtain JWTs. See
> [EQB_RBAC_VALIDATION_REPORT.md](./EQB_RBAC_VALIDATION_REPORT.md).


---

## Architecture report

### Current model

```
JWT (user_id + token_version only)
        ↓
JwtPrincipalValidator → DB-fresh roles/permissions → AuthenticatedUser
        ↓
TenantFilter → resolve → validate (PlatformPrincipalSupport) → TenantContextHolder
        ↓
PermissionAuthorizationInterceptor (fail-closed classification)
        ↓
Controller (@PublicEndpoint | @RequireAuthenticated | @RequirePermission | @RequiresRole)
        ↓
Service (@RequirePermission defense-in-depth where applicable)
        ↓
Hibernate tenantFilter (optional executeWithoutTenantFilter = Super Admin only)
```

Frontend (UX only; backend remains source of truth):

```
Session profile → AuthorizationProvider → Redux slice
        ↓
GET /auth/authorization (refresh)
        ↓
RouteProtection (fail-closed /app catalog) + Can/Protected
        ↓
Navigation catalogs (permission-only) → Sidebar / Top nav / Cards / Quick actions
```

### Strengths

- Permissions are platform-global; grants are role-scoped; runtime checks use explicit permission codes (role hierarchy is structural, not silent inheritance).
- JWT never trusted for roles/permissions — principal rebuilt from DB every request.
- Single evaluation path: `PermissionGuard` → `AuthorizationService` → `PermissionEvaluator`.
- OpenAPI enrichment reads the same annotation support used for enforcement.
- Frontend navigation is permission-driven (no role gates in catalogs).

### Dependency direction (corrected in 3.8)

| Layer | Depends on |
| ----- | ---------- |
| `navigation` | `authorization` types + permission constants |
| `authorization` routes | **Independent** `PROTECTED_ROUTES` (not derived from nav UI) |
| Nav ↔ routes | Kept in sync via unit parity tests |

### Hardcoded roles — allowed exceptions

Only platform trust-bar uses an explicit role check, centralized in `PlatformPrincipalSupport`:

- `tenantId == null` **and** `SUPER_ADMIN`
- Used by `TenantFilter`, `TenantValidationService`, and `executeWithoutTenantFilter`

All other access control uses permission codes.

---

## Security report

### Issues found and fixed (Phase 3.8)

| Severity | Issue | Fix |
| -------- | ----- | --- |
| Critical | Interceptor fail-open for unannotated `/api/**` handlers | Fail-closed: require `@PublicEndpoint` / `@RequireAuthenticated` / `@RequirePermission` / `@RequiresRole`; else `MissingAuthorizationAnnotationException` → 403 |
| Critical | Unclassified handlers undetected until exploit | Startup `ControllerAuthorizationCoverageGuard` fails boot if any `/api` handler lacks classification |
| High | Duplicate independent `SUPER_ADMIN` checks | Consolidated into `PlatformPrincipalSupport` |
| High | `executeWithoutTenantFilter` only required a free-text reason | Now requires authenticated platform Super Admin |
| High | Frontend unknown `/app/*` routes fail-open | `resolveRouteAccess` denies unlisted app paths |
| Medium | AuthZ routes derived from nav catalog (inverted deps) | Independent `PROTECTED_ROUTES` + parity tests |
| Medium | `@RequiresPermission` could drift from `@RequirePermission` | Composed alias via Spring `@AliasFor` |
| Medium | Hospital settings only guarded at controller | `@RequirePermission` on service interface + impl |
| Low | Session `hasRole`/`hasPermission` parallel SoT | Deprecated in favor of `useAuthorization` |

### Verified secure behaviors

| Control | Status |
| ------- | ------ |
| Privilege escalation via JWT role claims | Blocked (DB principal) |
| Cross-tenant header spoofing (hospital user) | 403 `TenantMismatchException` |
| AuthN vs AuthZ status codes | 401 vs 403 correct; generic `AUTHZ_ACCESS_DENIED` bodies |
| Permission code leakage in API responses | Prevented |
| Token version / email / status checks | Enforced in `JwtPrincipalValidator` |
| Frontend UI bypass | UX only — APIs re-enforce |

### Residual (non-critical) risks

1. **Actuator `/actuator/prometheus` public** — intentional for Docker scrape; harden at network edge in production.
2. **Manual FE/BE permission constant sync** — no codegen; parity relies on review + tests.
3. **`@PreAuthorize` / method-security SPI** wired but unused — prefer `@RequirePermission` until SpEL path is integration-tested.
4. **Controller + service double checks** — intentional defense-in-depth; minor CPU cost per request.

---

## Performance recommendations

1. **Permission set reuse:** `hasAny`/`hasAll` build a `Set` per call; for hot paths, resolve once per request into a `Set` on `AuthenticatedUser` (already immutable) and pass that into matchers.
2. **Authorization refresh:** Frontend `staleTime: 60_000` is fine for UX; do not lower aggressively — backend already re-reads grants per request.
3. **Avoid N+1 role loads:** Keep `PermissionResolver` using batched fetches when user–role–permission graphs grow (staff module).
4. **OpenAPI customizer:** Runs at doc build/request time only — no runtime API impact.
5. **Startup coverage guard:** Scans handler map once at boot — acceptable; avoid reflecting every request.

---

## Refactoring recommendations

1. **Generate frontend `Permissions` from OpenAPI or shared JSON** exported by backend bootstrap to eliminate manual drift.
2. **Delete Phase 3.6 deprecated shims** (`features/auth/config/navigation.ts`, unused `session-home.tsx`) once no imports remain.
3. **Prefer permission annotations over `@RequiresRole`** for new APIs; reserve roles for platform trust-bar only.
4. **When clinical modules land:** register every page in `PROTECTED_ROUTES` before adding App Router pages (fail-closed catalog will 403 otherwise — by design).
5. **Consider class-level `@RequirePermission` defaults** on future module controllers, with method overrides for finer actions.
6. **Keep `RoleHierarchy` structural** — do not introduce silent permission inheritance without an explicit product decision and audit trail.

---

## Definition of Done (Phase 3.8)

- [x] Architecture reviewed and documented  
- [x] Security reviewed; Critical issues fixed  
- [x] Backend fail-closed API classification + startup guard  
- [x] Cross-tenant / Super Admin trust bar centralized  
- [x] Frontend route catalog fail-closed + independent of nav  
- [x] Tests updated (backend + frontend)  
- [x] Re-review: no Critical issues remain  

---

*Companion docs: [SECURITY.md](./SECURITY.md), [PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md), [phasesreadme.md](./phasesreadme.md)*
