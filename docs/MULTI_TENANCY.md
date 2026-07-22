# MULTI_TENANCY.md

# Healthcare Management System (HMS)

## Multi-Tenant Architecture (Phases 2.1–2.4)

**Version:** 1.3  
**Status:** Active  
**Scope:** Tenant domain, isolation strategy, identification, HTTP middleware (`TenantFilter`),
validation, exception hierarchy, and tenant-aware JPA persistence.  
**Out of scope:** Hospital registration business APIs beyond bootstrap already present,
patients, appointments, clinical modules.

---

# 1. Selected Strategy

## Shared Database + Shared Schema + Tenant ID

| Approach                                    | Decision                                                                          |
| ------------------------------------------- | --------------------------------------------------------------------------------- |
| Database-per-tenant                         | Rejected — operational cost too high for early SaaS scale                         |
| Schema-per-tenant                           | Rejected — migration and connection complexity outweighs benefit at current scale |
| **Shared DB + Shared Schema + `tenant_id`** | **Selected**                                                                      |

### Why this strategy

1. **Matches DATABASE.md / SECURITY.md** — every business table already requires `tenant_id`.
2. **Operational simplicity** — one Flyway history, one connection pool, one backup pipeline.
3. **Horizontal scale path** — backend remains stateless; tenancy is a query constraint, not a deployment unit.
4. **Healthcare auditability** — soft delete + audit fields remain uniform across tenants.
5. **Future escape hatch** — large enterprise customers can later move to dedicated schemas/DBs without rewriting domain models.

Isolation is **logical**, not physical. Cross-tenant leakage is prevented by application contracts (JWT claim, header confirmation, context, repository filters).

---

# 2. Tenant Domain

A **tenant** is an independent hospital (or clinic) operating on the platform.

```
Tenant (aggregate root)
 ├── identity: id (UUID), slug, email
 ├── classification: tenant_type (HOSPITAL | CLINIC | HOSPITAL_GROUP)
 ├── commercial: subscription_plan
 ├── lifecycle: status (PENDING | ACTIVE | SUSPENDED | INACTIVE)
 └── audit / soft-delete: created_*, updated_*, deleted_*
```

---

# 3. Tenant Lifecycle

```
                  activate()
     PENDING ──────────────────► ACTIVE
                                   │  │
                         suspend() │  │ deactivate()
                                   ▼  ▼
                               SUSPENDED
                                   │
                         activate()│  deactivate()
                                   ▼
                               INACTIVE
```

Only **ACTIVE** + not soft-deleted tenants are **operational**.

---

# 4. Tenant Identification Strategy (Phase 2.2)

| Priority | Source               | Implementation            | Status                                                                |
| -------- | -------------------- | ------------------------- | --------------------------------------------------------------------- |
| 1        | Header `X-Tenant-ID` | `HeaderTenantResolver`    | **Active**                                                            |
| 2        | Subdomain / Host     | `SubdomainTenantResolver` | Wired, **disabled** (`hms.tenant.resolution.subdomain-enabled=false`) |

When an authenticated principal already carries a `tenantId`, a resolved header value **must match** or the request fails closed (`TENANT_HEADER_MISMATCH`). This is isolation defence-in-depth, not authentication.

---

# 5. Phase 2.3 Tenant Middleware Pipeline

```mermaid
sequenceDiagram
    participant Client
    participant Jwt as JwtAuthenticationFilter
    participant Filter as TenantFilter
    participant Resolve as TenantResolutionService
    participant Validate as TenantValidation
    participant Holder as TenantContextHolder
    participant API as Controller / Service / Repository

    Client->>Jwt: Incoming Request
    Jwt->>Jwt: Authentication (optional Bearer)
    Jwt->>Filter: next

    alt Public bypass (login, register hospital, health, swagger, …)
        Filter->>API: ignore X-Tenant-ID; no bind
    else Protected
        Filter->>Resolve: resolveTenantId (header → …)
        alt No header
            Filter->>Filter: fall back to AuthenticatedUser.tenantId
        end
        alt Still empty and not Super Admin role
            Filter-->>Client: 403 TENANT_REQUIRED
        else Tenant id present
            Filter->>Validate: validate(id) + JWT match
            Validate-->>Filter: TenantContext
            Filter->>Holder: set(context)
            Filter->>API: continue
        end
    end

    API-->>Filter: Response
    Filter->>Holder: clear() in finally
```

### Components

| Piece      | Class                                              |
| ---------- | -------------------------------------------------- |
| Middleware | `TenantFilter` (`OncePerRequestFilter`)            |
| Resolution | `TenantResolutionService` + `HeaderTenantResolver` |
| Validation | `TenantValidation` / `TenantValidationService`     |
| Context    | `TenantContext` + `TenantContextHolder`            |
| Bypass     | `TenantBypassPaths` (public auth/health/swagger)   |

### Why no MVC interceptor

`TenantFilter` already runs once per request after authentication and before controllers. A `HandlerInterceptor` would duplicate bind/clear semantics without adding value.

### Exception hierarchy

```
TenantException
 ├── InvalidTenantIdentifierException   → 400
 ├── TenantNotFoundException             → 404
 ├── TenantNotActiveException            → 403
 ├── TenantMismatchException             → 403
 ├── TenantRequiredException             → 403
 └── TenantInvalidTransitionException   → 400 (domain lifecycle)
```

---

# 6. Isolation Strategy

1. Tenant-owned business entities extend `TenantOwnedEntity` (`tenant_id` + Hibernate filter).
2. Shared catalog types that also admit platform rows (system `Role`s) extend `TenantAwareEntity`
   with a selective filter: `tenant_id = :tenantId OR (tenant_id IS NULL AND system_role = true)`.
3. Platform-only rows (Super Admin users, global system roles) keep `tenant_id = NULL`.
4. Foreign keys from tenant-scoped tables → `tenants(id)`.
5. Soft delete via `@SQLRestriction("deleted = false")` (orthogonal to the tenant filter).
6. JWT carries `tenant_id`; header must not contradict it when both present.
7. `TenantFilter` fails closed on invalid/suspended tenants when a tenant id is required.

---

# 6.1 Phase 2.4 Tenant-Aware Persistence

### Chosen approach

| Option                                                                               | Decision                                                     |
| ------------------------------------------------------------------------------------ | ------------------------------------------------------------ |
| Per-repository JPA `Specification` / duplicated `findByTenantId…`                    | Rejected — easy to omit on `findById` / JPQL                 |
| Custom repository base only                                                          | Rejected — incomplete coverage without ORM-level enforcement |
| **`TenantOwnedEntity` + Hibernate `@Filter` + write listener + TX-bound enablement** | **Selected**                                                 |

### Hierarchy

```
BaseEntity
 └── TenantAwareEntity          // tenant_id (updatable=false) + TenantEntityListener
      ├── TenantOwnedEntity     // @Filter: tenant_id = :tenantId   (User, tokens, AuditLog, …)
      └── Role (selective)      // @Filter: tenant OR platform system_role
```

### Read path

1. `TenantFilter` binds `TenantContextHolder`.
2. `TenantPersistenceConfig` / `JpaTransactionManager.doBegin` enables Hibernate
   `tenantFilter` on the transactional `Session` with the bound tenant id.
3. Spring Data / JPQL / `findById` against `TenantOwnedEntity` subclasses automatically
   append `tenant_id = :tenantId` — repositories cannot accidentally return another
   tenant's rows while context is present.
4. When no tenant is bound (login, JWT validation, Super Admin without header), the
   filter stays disabled so bootstrap lookups continue to work.

### Write path

- `@PrePersist` stamps `tenant_id` from context when unset; rejects mismatches.
- `@PreUpdate` rejects foreign-tenant entities and rejects tenant-scoped mutation of
  platform rows (`tenant_id IS NULL`).
- `tenant_id` column is `updatable = false`.

### Hard rules

- **Native SQL / JDBC** against tenant-owned tables is forbidden unless the statement
  includes an explicit `tenant_id` predicate and is security-reviewed.
- `TenantHibernateFilterEnabler.executeWithoutTenantFilter(reason, …)` is Super Admin /
  bootstrap only; a non-blank `reason` is required and logged at WARN.

### Design review (Phase 2.4)

| Role               | Verdict                                                                                                            |
| ------------------ | ------------------------------------------------------------------------------------------------------------------ |
| database-architect | Approved — filter + `@SQLRestriction` soft-delete split; nullable `tenant_id` FK retained for platform rows        |
| software-architect | Approved — hierarchy + TX-bound enablement (not fragile `@Before` AOP); Specifications rejected for DRY isolation  |
| security-engineer  | Approved — strict owned-entity filter; Role limited to `system_role`; platform-row write guard; escape hatch gated |

Residual risks accepted for this phase: filter does not apply to native SQL (banned by policy);
fail-open when context is absent remains intentional for public auth / Super Admin.

---

# 7. Package Structure

```
com.healthcare.hms.common.persistence
├── BaseEntity.java
├── TenantAwareEntity.java
├── TenantOwnedEntity.java
├── TenantEntityListener.java
├── TenantPersistence.java          // filter name / conditions
└── package-info.java               // @FilterDef

com.healthcare.hms.tenant
├── entity/Tenant.java
├── enums/{TenantStatus, TenantType, SubscriptionPlan}
├── repository/TenantRepository.java
├── context/{TenantContext, TenantContextHolder}
├── resolution/{TenantResolver, HeaderTenantResolver, SubdomainTenantResolver, TenantResolutionService}
├── validation/{TenantValidation, TenantValidationService}
├── persistence/{TenantHibernateFilterEnabler, TenantPersistenceConfig}
├── web/{TenantFilter, TenantBypassPaths}
├── service/{TenantAccessService, impl/...}
└── exception/
    ├── TenantException.java
    ├── InvalidTenantIdentifierException.java
    ├── TenantNotFoundException.java
    ├── TenantNotActiveException.java
    ├── TenantMismatchException.java
    ├── TenantRequiredException.java
    └── TenantInvalidTransitionException.java
```

---

# 8. Thread Safety

- `TenantContextHolder` uses a **non-inheritable** `ThreadLocal`.
- Filter always calls `clear()` in `finally` to prevent pool-thread leakage.
- Unstructured `@Async` hand-off requires a future `TaskDecorator` (out of scope for Phase 2.4).

---

# 9. Request Lifecycle

```
Incoming Request
  ↓
Authentication (JwtAuthenticationFilter)
  ↓
Tenant Resolution (header / principal fallback)
  ↓
Tenant Validation (exists + ACTIVE + principal match)
  ↓
Tenant Context (TenantContextHolder.set)
  ↓
Controller → Service (@Transactional → enable Hibernate tenantFilter)
  ↓
Repository (auto WHERE tenant_id = :tenantId on TenantOwnedEntity)
  ↓
Response
  ↓
finally → TenantContextHolder.clear
```

### Phase 2.1

- [x] Shared Schema + Tenant ID strategy
- [x] Tenant aggregate, enums, repository, Flyway, audit, soft delete

### Phase 2.2

- [x] `TenantContext` + `TenantContextHolder`
- [x] `TenantResolver` + `HeaderTenantResolver`
- [x] Subdomain resolver extension point (disabled)

### Phase 2.3

- [x] `TenantFilter` (`OncePerRequestFilter`) after JWT
- [x] `TenantValidation` extracted from filter
- [x] Tenant exception hierarchy
- [x] Public endpoint bypass (login, register hospital, health, swagger, …)
- [x] Mandatory tenant on protected routes (principal fallback; Super Admin exempt)
- [x] Always clear context after request
- [x] No redundant MVC interceptor

### Phase 2.4

- [x] `TenantOwnedEntity` + Hibernate `tenantFilter`
- [x] `TenantEntityListener` write stamping / mismatch guards
- [x] TX-bound filter enablement via `TenantPersistenceConfig`
- [x] Selective Role filter for platform system roles
- [x] Cross-tenant isolation tests (unit + Testcontainers integration)

### Phase 2.7

- [x] Public paths ignore `X-Tenant-ID` (enumeration hardening)
- [x] Platform bypass requires `SUPER_ADMIN` (not null-tenant alone)
- [x] Null-tenant principals cannot bind arbitrary header tenants without `SUPER_ADMIN`
- [x] JWT principal uses DB `tenant_id`, roles, and permissions (claim drift rejected)
- [x] Conflicting duplicate `X-Tenant-ID` headers rejected
- [x] Legacy `POST /auth/register/admin` disabled (`410 Gone`)
- [x] `TenantNotFoundException` no longer echoes tenant UUIDs

### Phase 2.8

- [x] Production readiness review of Phases 2.1–2.7
- [x] Unauthenticated protected requests defer to Spring Security 401 (no `TENANT_REQUIRED` mask)
- [x] Unauthenticated tenant validation/enumeration closed on protected paths
- [x] Security-chain-only filter registration (`FilterRegistrationBean` disabled)
- [x] OpenAPI documents Bearer + `X-Tenant-ID`
- [x] Frontend atomic hospital registration aligned with backend contract
- [x] Frontend attaches confirming `X-Tenant-ID` from access token

---

End of MULTI_TENANCY.md
