# HMS Phases README

**Project:** Healthcare Management System (HMS) SaaS  
**Purpose:** Complete inventory of what has been built, phase by phase — deliverables, modules, and what each important file/folder is for.  
**Source of truth companions:** [ROADMAP.md](./ROADMAP.md), [MULTI_TENANCY.md](./MULTI_TENANCY.md), [PROJECT_CONTEXT.md](./PROJECT_CONTEXT.md), [ARCHITECTURE.md](./ARCHITECTURE.md)

**Current progress:** Phases **1 → 2.8** plus **Phase 3.1–3.8** (RBAC through complete RBAC
review/hardening) are delivered. Clinical modules and Phase 3 hospital ops beyond RBAC/authz
UI are **not started** (empty package stubs only).

---

## Progress snapshot

| Phase | Title | Status |
| ----- | ----- | ------ |
| 0 | Informal bootstrap / product docs (not in ROADMAP) | Done (informal) |
| 1 | Project foundation | Done |
| 1.9 | Auth module testing gate | Done |
| 2 | Authentication & authorization (core flows) | Done (core) |
| 2.1 | Multi-tenant foundation | Done |
| 2.2 | Tenant resolution | Done |
| 2.3 | Tenant middleware | Done |
| 2.4 | Tenant-aware persistence | Done |
| 2.5 | Hospital registration (atomic) | Done |
| 2.6 | Hospital settings | Done |
| 2.7 | Tenant security audit | Done |
| 2.8 | Multi-tenant production readiness review | Done |
| 2.x | Remaining RBAC / clinical auth surfaces | In progress / deferred |
| 3.1 | RBAC domain design | Done |
| 3.2 | Authorization infrastructure | Done |
| 3.3 | Permission-based authorization | Done |
| 3.4 | Secure existing APIs | Done |
| 3.5 | Default system roles | Done |
| 3.6 | Frontend authorization | Done |
| 3.7 | Dynamic navigation | Done |
| 3.8 | Complete RBAC review | Done |
| 3.x | Departments, staff assignment, multi-hospital ops | Not started |
| 4+ | Staff, patients, clinical, … | Not started |

---

## Phase 0 — Informal bootstrap (pre-ROADMAP)

ROADMAP does **not** define a Phase 0. In practice this was the product-definition and monorepo kickoff that became Phase 1.

### What was done

- Product vision, roles, architecture principles, and phased plan written as docs
- Engineering standards and security posture defined for AI/dev consistency
- Empty clinical module directories reserved for later phases

### Key files (docs & structure)

| Path | Purpose |
| ---- | ------- |
| `docs/PRD.md` | Product requirements, personas, feature scope |
| `docs/ARCHITECTURE.md` | High-level system architecture, auth & tenant lifecycle |
| `docs/ENGINEERING_RULES.md` | Coding standards, folder rules, SOLID, DoD |
| `docs/ROADMAP.md` | Phased delivery plan and status |
| `docs/PROJECT_CONTEXT.md` | Long-term context + architectural decisions (ADRs) |
| `docs/SECURITY.md` | Security policies, tenant isolation rules |
| `docs/DATABASE.md` | Target schema design (includes Phase 3+ tables) |
| `docs/API.md` | REST API catalog (implemented + planned) |
| `docs/DESIGN_SYSTEM.md` | UI tokens, components, status colors |
| `docs/TESTING.md` | Test strategy / pyramid |
| `docs/DEPLOYMENT.md` | Deployment, Nginx, monitoring |
| `backend/.../patients` (etc.) | Reserved empty packages for later phases |

---

## Phase 1 — Project foundation

### Objective

Establish monorepo, FE/BE apps, Docker, DB connectivity, quality tooling, health checks, and API docs.

### What was done

- Root npm workspace + Spring Boot Maven backend
- Next.js 15 App Router frontend with TypeScript, Tailwind, shadcn/ui
- Docker Compose stack: frontend, backend, MySQL, Redis, Nginx, Prometheus, Grafana
- Environment templates (root / frontend / backend)
- Flyway baseline + identity schema start
- Actuator health + custom `/api/v1/system/health`
- OpenAPI / Swagger UI
- Husky + Commitlint + Prettier + ESLint
- CI folder skeleton (workflows not fully filled yet)

### Root / infrastructure files

| Path | Purpose |
| ---- | ------- |
| `package.json` | Root scripts: `dev`, `build`, `lint`, `docker:up` / `down` |
| `docker-compose.yml` | Local multi-service orchestration |
| `docker/nginx/default.conf` | Reverse proxy: `/` → frontend, `/api/` → backend |
| `docker/monitoring/prometheus/prometheus.yml` | Scrapes backend Actuator Prometheus endpoint |
| `frontend/Dockerfile` | Frontend image (build-args for `NEXT_PUBLIC_*`) |
| `backend/Dockerfile` | Backend image |
| `.env.example` | Root env template (DB, JWT, CORS, tenant flag) |
| `frontend/.env.example` | Frontend public env |
| `backend/.env.example` | Backend secrets / DB / Redis / tenant flag |
| `.husky/pre-commit` | Runs lint + format before commit |
| `.husky/commit-msg` | Enforces conventional commits |
| `commitlint.config.cjs` | Commit message rules |
| `.prettierrc.json` / `.editorconfig` | Formatting consistency |
| `.github/workflows/.gitkeep` | CI placeholder (pipelines still skeleton) |
| `README.md` | Quick start and current scope |

### Backend foundation files

| Path | Purpose |
| ---- | ------- |
| `backend/pom.xml` | Maven deps: Spring Boot 3, Security, JPA, Flyway, Redis, OpenAPI, JaCoCo |
| `backend/src/main/java/.../HmsBackendApplication.java` | Spring Boot entrypoint |
| `backend/src/main/resources/application.properties` | Datasource, JPA, JWT, mail, tenant resolution flags |
| `backend/.../common/health/HealthController.java` | `GET /api/v1/system/health` |
| `backend/.../common/api/*` | Standard `ApiResponse` / error envelope |
| `backend/.../common/exception/GlobalExceptionHandler.java` | Centralized HTTP error mapping |
| `backend/.../common/persistence/BaseEntity.java` | Shared id, audit, soft-delete, version columns |
| `backend/.../config/OpenApiConfig.java` | Swagger security schemes (Bearer + later `X-Tenant-ID`) |
| `backend/.../db/migration/V1__platform_baseline.sql` | Platform metadata bootstrap |
| `backend/.../db/migration/V2__identity_domain.sql` | Users, roles, permissions, refresh tokens, seeds |

### Frontend foundation files

| Path | Purpose |
| ---- | ------- |
| `frontend/package.json` | Next.js app deps and scripts |
| `frontend/src/app/layout.tsx` | Root layout, fonts, providers |
| `frontend/src/app/globals.css` | Design tokens / Tailwind theme |
| `frontend/src/app/page.tsx` | Public landing / home |
| `frontend/src/app/api/health/route.ts` | Frontend health JSON |
| `frontend/src/components/ui/*` | shadcn UI primitives (button, input, card, …) |
| `frontend/src/providers/app-providers.tsx` | Redux + React Query + Session + toasts |
| `frontend/src/store/index.ts` | Redux Toolkit store scaffolding |
| `frontend/src/lib/env.ts` | Typed env access |
| `frontend/src/lib/utils.ts` | `cn()` helper |
| `frontend/components.json` | shadcn CLI config |
| `frontend/eslint.config.mjs` | ESLint / Next rules |

### Definition of Done (met)

- Project runs locally (`npm run dev` / Docker Compose)
- Health endpoints respond
- Swagger available at `/swagger-ui`
- Quality hooks run on commit

---

## Phase 1.9 — Authentication module testing

Documented in [TEST_REPORT_PHASE_1_9.md](./TEST_REPORT_PHASE_1_9.md).

### What was done

- Backend auth unit + MockMvc suites; JaCoCo gate ≥ 90% (reported **93.6%**)
- Frontend Jest + RTL suites (reported **99.5%** line coverage on auth)
- Playwright e2e specs under `frontend/e2e/auth`
- Testcontainers MySQL integration tests (skip when Docker unavailable)

### Key test locations

| Path | Purpose |
| ---- | ------- |
| `backend/src/test/java/com/healthcare/hms/auth/**` | Auth unit / controller / IT tests |
| `frontend/src/features/auth/__tests__/**` | Schema, form, API, route-guard tests |
| `frontend/e2e/auth/**` | Playwright auth flows |
| `docs/TEST_REPORT_PHASE_1_9.md` | Coverage and results report |

---

## Phase 2 — Authentication & authorization (core)

### Objective

Secure login session lifecycle, JWT + refresh tokens, password reset, email verification, RBAC scaffolding, protected FE routes.

### What was done

- Login / logout / refresh / profile / change password
- Opaque refresh tokens with rotation + reuse detection
- Password reset (forgot → email → reset)
- Email verification (verify + resend); activates tenant/hospital on first admin verify
- Strong password policy
- Roles & permissions entities + seed catalog
- `@RequiresRole` / `@RequiresPermission` interceptor
- Frontend auth pages, session provider, route guard, middleware cookie flag
- Legacy `POST /api/v1/auth/register/admin` returns **410 Gone** (replaced by Phase 2.5 atomic registration)

### Backend — `auth` module

| Path | Purpose |
| ---- | ------- |
| `auth/controller/AuthController.java` | REST: login, logout, refresh, profile, password, verify, hospital alias |
| `auth/service/AuthService.java` + `impl/AuthServiceImpl.java` | Orchestrates auth business rules |
| `auth/service/RefreshTokenService.java` + `impl/` | Issue / rotate / revoke refresh tokens |
| `auth/service/PasswordResetService.java` + `impl/` | Forgot / reset password flow |
| `auth/service/EmailVerificationService.java` + `impl/` | Issue / consume verification tokens |
| `auth/service/*EmailService.java` | Sends verification / reset emails |
| `auth/entity/RefreshToken.java` | Persisted hashed refresh token |
| `auth/entity/PasswordResetToken.java` | Single-use reset token |
| `auth/entity/EmailVerificationToken.java` | Single-use email verify token |
| `auth/repository/*` | Token repositories |
| `auth/dto/request/*` | Login, refresh, logout, forgot/reset, verify, profile DTOs |
| `auth/dto/response/AuthResponse.java` | Access + refresh + user profile payload |
| `auth/dto/response/UserProfileResponse.java` | Profile shape |
| `auth/mapper/AuthMapper.java` | Entity → profile DTO |
| `auth/crypto/TokenHashingService.java` | Hash opaque tokens at rest |
| `auth/validator/StrongPassword*.java` | Password policy (≥12, upper/lower/digit/special) |
| `auth/config/*Properties.java` | TTL / email verification config |

### Backend — `users` module (identity / RBAC data)

| Path | Purpose |
| ---- | ------- |
| `users/entity/User.java` | User account (tenant-owned) |
| `users/entity/Role.java` | Role with selective Hibernate filter (tenant + platform system roles) |
| `users/entity/Permission.java` | Permission catalog row |
| `users/repository/*` | User / role / permission persistence |
| `users/constant/PermissionConstants.java` | Permission code constants |
| `users/enums/RoleType.java`, `UserStatus.java` | Role and status enums |

### Backend — `security` module

| Path | Purpose |
| ---- | ------- |
| `security/config/SecurityConfig.java` | Stateless JWT filter chain + public endpoints |
| `security/config/CorsConfig.java` | CORS from env |
| `security/jwt/JwtService.java` | Create / parse access JWTs |
| `security/jwt/JwtAuthenticationFilter.java` | Bearer extraction → SecurityContext |
| `security/jwt/JwtPrincipalValidator.java` | Reload user from DB; reject claim drift |
| `security/jwt/JwtClaims.java` | Claim payload record |
| `security/principal/AuthenticatedUser.java` | Security principal |
| `security/annotation/RequiresPermission.java` | Method-level permission check |
| `security/annotation/RequiresRole.java` | Method-level role check |
| `security/authorization/*` | Interceptor + authorization service |
| `security/handler/RestAuthenticationEntryPoint.java` | JSON 401 |
| `security/handler/RestAccessDeniedHandler.java` | JSON 403 |
| `security/resolver/CurrentUserArgumentResolver.java` | `@CurrentUser` injection |
| `security/SecurityConstants.java` | Headers, claims, **PUBLIC_ENDPOINTS** |

### Backend — `audit` + `common` (auth support)

| Path | Purpose |
| ---- | ------- |
| `audit/entity/AuditLog.java` | Audit row (tenant-owned) |
| `audit/service/AuditLogService.java` | Write audit events (login, register, …) |
| `common/email/*` | SMTP / logging email senders |
| `common/exception/auth/*` | Auth-specific exceptions |
| `db/migration/V3__tenants_and_audit.sql` | Tenants + audit_logs + Hospital Admin grants |
| `db/migration/V4__password_reset_tokens.sql` | Password reset table |
| `db/migration/V5__email_verification.sql` | Email verification flags + tokens |

### Frontend — auth feature

| Path | Purpose |
| ---- | ------- |
| `features/auth/api/auth-api.ts` | Axios calls for all auth endpoints |
| `features/auth/types/auth.types.ts` | TS contracts (login, register, profile, …) |
| `features/auth/validation/*-schema.ts` | Zod validation per form |
| `features/auth/hooks/use-*-mutation.ts` | TanStack Query mutations |
| `features/auth/components/login-form.tsx` | Login UI |
| `features/auth/components/register-hospital-form.tsx` | Atomic hospital + admin registration UI |
| `features/auth/components/forgot-password-form.tsx` | Forgot password UI |
| `features/auth/components/reset-password-form.tsx` | Reset password UI |
| `features/auth/components/resend-verification-form.tsx` | Resend verification UI |
| `features/auth/components/verify-email-handler.tsx` | Consumes verify token from URL |
| `features/auth/components/route-guard.tsx` | Client role/permission gate |
| `features/auth/components/protected-shell.tsx` | Authenticated chrome |
| `features/auth/components/profile-panel.tsx` | Profile display |
| `features/auth/components/session-home.tsx` | Post-login home summary |
| `features/auth/config/navigation.ts` | Auth-related nav helpers |
| `app/(auth)/login/page.tsx` | Login route |
| `app/(auth)/register/hospital/page.tsx` | Register hospital route |
| `app/(auth)/forgot-password/page.tsx` | Forgot password route |
| `app/(auth)/reset-password/page.tsx` | Reset password route |
| `app/(auth)/verify-email/**` | Verify + success/failed pages |
| `app/(auth)/resend-verification/page.tsx` | Resend verification route |
| `app/(protected)/app/page.tsx` | Authenticated home |
| `app/(protected)/app/profile/page.tsx` | Profile page |
| `app/forbidden/page.tsx` | 403 page |
| `middleware.ts` | Edge redirect using session cookie flag |
| `providers/session-provider.tsx` | Session state, signIn/signOut, role helpers |
| `services/http/api-client.ts` | Axios: Bearer, refresh queue, `X-Tenant-ID` from JWT |
| `lib/auth-token.ts` | Access/refresh token storage |
| `lib/session-constants.ts` | Session cookie name |
| `lib/auth-events.ts` | Session invalidation events |
| `lib/api-error.ts` | API error → user message |

### Auth request lifecycle (working)

```
Login → validate user → JWT access + opaque refresh
  → Protected request: JwtAuthenticationFilter → principal from DB
  → (Phase 2.3+) TenantFilter → controller → response
  → Refresh rotates refresh token; reuse detected → revoke family
```

---

## Phase 2.1 — Multi-tenant foundation

### What was done

- Chose **Shared DB + Shared Schema + `tenant_id`**
- `Tenant` aggregate (slug, type, plan, lifecycle status)
- Tenant enums, repository, Flyway FKs from identity/audit tables
- Cross-cutting `com.healthcare.hms.tenant` package (separate from hospital business APIs)

### Key files

| Path | Purpose |
| ---- | ------- |
| `tenant/entity/Tenant.java` | Tenant aggregate root |
| `tenant/enums/TenantStatus.java` | PENDING / ACTIVE / SUSPENDED / INACTIVE |
| `tenant/enums/TenantType.java` | HOSPITAL / CLINIC / HOSPITAL_GROUP |
| `tenant/enums/SubscriptionPlan.java` | BASIC → ENTERPRISE |
| `tenant/repository/TenantRepository.java` | Tenant persistence |
| `tenant/package-info.java` | Module documentation |
| `db/migration/V6__tenant_foundation.sql` | `tenant_type`, indexes, FKs → `tenants` |
| `docs/MULTI_TENANCY.md` | Strategy, lifecycle, isolation rules |

---

## Phase 2.2 — Tenant resolution

### What was done

- Request-scoped tenant context holder
- Header resolver for `X-Tenant-ID` (primary)
- Subdomain resolver wired but **disabled** (`TENANT_SUBDOMAIN_RESOLUTION_ENABLED=false`)
- Ordered resolver composition

### Key files

| Path | Purpose |
| ---- | ------- |
| `tenant/context/TenantContext.java` | Immutable bound tenant snapshot |
| `tenant/context/TenantContextHolder.java` | Non-inheritable ThreadLocal context |
| `tenant/resolution/TenantResolver.java` | Resolver strategy interface |
| `tenant/resolution/HeaderTenantResolver.java` | Parse / validate `X-Tenant-ID` (rejects conflicting duplicates) |
| `tenant/resolution/SubdomainTenantResolver.java` | Host-based extension (disabled by default) |
| `tenant/resolution/TenantResolutionService.java` | First enabled resolver wins |
| `tenant/resolution/TenantResolverOrders.java` | Priority constants |
| `tenant/resolution/TenantIdentificationSource.java` | HEADER / SUBDOMAIN enum |

---

## Phase 2.3 — Tenant middleware

### What was done

- `TenantFilter` after JWT in Security chain
- Resolve → validate (exists + ACTIVE + principal match) → bind → clear in `finally`
- Public path bypass shared with `SecurityConstants.PUBLIC_ENDPOINTS`
- Tenant exception hierarchy + GlobalExceptionHandler mapping
- Super Admin may omit tenant; hospital users require tenant

### Key files

| Path | Purpose |
| ---- | ------- |
| `tenant/web/TenantFilter.java` | OncePerRequestFilter middleware |
| `tenant/web/TenantBypassPaths.java` | Public paths that skip tenant bind |
| `tenant/validation/TenantValidation.java` | Validation contract |
| `tenant/validation/TenantValidationService.java` | Principal match + active tenant load |
| `tenant/service/TenantAccessService.java` + `impl/` | Load / require active tenant → context |
| `tenant/exception/TenantException.java` | Base |
| `tenant/exception/InvalidTenantIdentifierException.java` | 400 — bad UUID / conflicting headers |
| `tenant/exception/TenantNotFoundException.java` | 404 — no echo of UUID (enumeration hardening) |
| `tenant/exception/TenantNotActiveException.java` | 403 — suspended / inactive / pending |
| `tenant/exception/TenantMismatchException.java` | 403 — header ≠ JWT tenant |
| `tenant/exception/TenantRequiredException.java` | 403 — missing tenant on protected route |
| `tenant/exception/TenantInvalidTransitionException.java` | 400 — illegal lifecycle transition |
| `tenant/exception/TenantAccessException.java` | Access-level tenant errors |

### Middleware order

```
Request → JwtAuthenticationFilter → TenantFilter → Controllers
                                              ↓ finally
                                    TenantContextHolder.clear()
```

---

## Phase 2.4 — Tenant-aware persistence

### What was done

- Entity hierarchy: `BaseEntity` → `TenantAwareEntity` → `TenantOwnedEntity`
- Hibernate `@Filter` `tenantFilter` enabled on JPA transaction begin
- Write listener stamps / rejects mismatched `tenant_id`
- Selective Role filter includes platform `system_role` rows
- Escape hatch `executeWithoutTenantFilter` for bootstrap / Super Admin only

### Key files

| Path | Purpose |
| ---- | ------- |
| `common/persistence/TenantAwareEntity.java` | Nullable `tenant_id` + entity listener |
| `common/persistence/TenantOwnedEntity.java` | Strict `tenant_id = :tenantId` filter |
| `common/persistence/TenantEntityListener.java` | `@PrePersist` / `@PreUpdate` guards |
| `common/persistence/TenantPersistence.java` | Filter name + condition constants |
| `common/persistence/package-info.java` | `@FilterDef` registration |
| `tenant/persistence/TenantPersistenceConfig.java` | Custom `JpaTransactionManager` enables filter in `doBegin` |
| `tenant/persistence/TenantHibernateFilterEnabler.java` | Enable / disable filter helpers |

### Read/write rule

- **Read:** Hibernate appends `tenant_id = :tenantId` when context bound  
- **Write:** Listener auto-stamps from context; rejects cross-tenant mutations  
- **Native SQL:** Forbidden without explicit `tenant_id` predicate (policy)

---

## Phase 2.5 — Hospital registration (atomic)

### What was done

- Single transaction: **Tenant + default Hospital + tenant-scoped roles + initial admin**
- Public `POST /api/v1/hospitals/register` (alias also on `/api/v1/auth/register/hospital`)
- Tenant & hospital start `PENDING`; activate after admin email verification
- Frontend form collects hospital + administrator fields

### Key files

| Path | Purpose |
| ---- | ------- |
| `hospitals/controller/HospitalRegistrationController.java` | Public register endpoint |
| `hospitals/service/HospitalRegistrationService.java` + `impl/` | Atomic registration orchestration |
| `hospitals/dto/request/HospitalRegistrationRequest.java` | Hospital + admin payload |
| `hospitals/dto/response/HospitalRegistrationResponse.java` | Ids, statuses, provisioned roles |
| `hospitals/mapper/HospitalRegistrationMapper.java` | Response mapping |
| `hospitals/entity/Hospital.java` | Default hospital profile (tenant-owned) |
| `hospitals/enums/HospitalStatus.java` | Hospital lifecycle status |
| `hospitals/repository/HospitalRepository.java` | Hospital persistence |
| `hospitals/bootstrap/TenantRoleProvisioner.java` | Creates tenant-scoped roles on register |
| `hospitals/bootstrap/DefaultTenantRoleCatalog.java` | Permission sets per role type |
| `db/migration/V7__hospitals.sql` | `hospitals` table |
| FE `register-hospital-form.tsx` + schema + mutation | UI + Zod + API call to `/hospitals/register` |

### Registration flow

```
POST /hospitals/register
  → create Tenant (PENDING)
  → create default Hospital (PENDING)
  → provision HOSPITAL_ADMIN / DOCTOR / … roles for tenant
  → create admin User (emailVerified=false)
  → send verification email
  → return HospitalRegistrationResponse
```

---

## Phase 2.6 — Hospital settings

### What was done

- Tenant-isolated get/update of default hospital settings
- Profile, logo URL, timezone, currency, language, contact, address, working hours
- Custom validators for locale / hours
- Permissions: `HOSPITAL_READ` / `HOSPITAL_UPDATE`

### Key files

| Path | Purpose |
| ---- | ------- |
| `hospitals/controller/HospitalSettingsController.java` | `GET/PUT /api/v1/hospitals/settings` |
| `hospitals/service/HospitalSettingsService.java` + `impl/` | Load/update via `TenantContextHolder` |
| `hospitals/dto/request/UpdateHospitalSettingsRequest.java` | Settings update payload |
| `hospitals/dto/response/HospitalSettingsResponse.java` | Settings response |
| `hospitals/mapper/HospitalSettingsMapper.java` | Mapping |
| `hospitals/model/WorkingHours.java`, `WorkingDayHours.java` | Hours model |
| `hospitals/validator/ValidTimezone*.java` | Timezone validation |
| `hospitals/validator/ValidCurrency*.java` | Currency validation |
| `hospitals/validator/ValidLanguage*.java` | Language validation |
| `hospitals/validator/ValidWorkingHours*.java` | Working hours validation |
| `db/migration/V8__hospital_settings.sql` | Settings columns + `working_hours` JSON |

---

## Phase 2.7 — Tenant security audit

### What was done

- Public paths ignore `X-Tenant-ID` (no unauthenticated enumeration)
- Platform bypass requires `SUPER_ADMIN` role (not null-tenant alone)
- JWT principal uses **DB** tenant/roles/permissions; claim drift rejected
- Conflicting duplicate `X-Tenant-ID` headers rejected
- Legacy admin registration disabled (`410 Gone`)
- `TenantNotFoundException` does not echo tenant UUIDs

### Key files touched

| Path | Purpose in audit |
| ---- | ---------------- |
| `tenant/web/TenantFilter.java` | Fail-closed binding rules |
| `tenant/validation/TenantValidationService.java` | Principal ↔ header match |
| `security/jwt/JwtPrincipalValidator.java` | DB-backed principal |
| `tenant/resolution/HeaderTenantResolver.java` | Conflict rejection |
| `auth/controller/AuthController.java` | Legacy admin → 410 |
| `security/SecurityConstants.java` | Hardened public path list |

---

## Phase 2.8 — Multi-tenant production readiness

### What was done (review + fixes)

- Full lifecycle review: register → verify → login → JWT → tenant → API → filter → response
- Unauthenticated protected requests no longer masked as `TENANT_REQUIRED` (defer to 401)
- Servlet auto-registration disabled for JWT/Tenant filters (Security chain only)
- OpenAPI documents Bearer + `X-Tenant-ID`
- Frontend registration contract aligned with atomic backend DTO
- Frontend attaches confirming `X-Tenant-ID` from access token
- Env/Docker expose `TENANT_SUBDOMAIN_RESOLUTION_ENABLED`
- Docs updated (`MULTI_TENANCY`, `ROADMAP`, `PROJECT_CONTEXT`)

### Notable fixed / hardened files

| Path | Change |
| ---- | ------ |
| `tenant/web/TenantFilter.java` | Auth-first gate before tenant validate |
| `security/config/SecurityConfig.java` | `FilterRegistrationBean.setEnabled(false)` |
| `config/OpenApiConfig.java` | `tenantHeader` API key scheme |
| FE `api-client.ts` | Attach `X-Tenant-ID` from JWT claim |
| FE register hospital types/schema/form | Match `HospitalRegistrationRequest` |

---

## Phase 3.1 — RBAC domain design

### Objective

Refine the Role / Permission domain for enterprise RBAC: naming convention, group/action
enums, structural role hierarchy, default system role grants, indexes, and audit fields.
No new controllers, frontend, business modules, or authorization evaluation changes.

### What was done

- `PermissionGroup` + `PermissionAction` enums
- `PermissionNaming` (`{GROUP}_{ACTION}`) and `RoleHierarchy` (levels + parents)
- Refined `Permission` (group, action, system_permission) and `Role` (hierarchy_level, parent, assignable)
- Flyway `V9__rbac_domain.sql` — schema, backfill, platform hierarchy, default system grants
- Repository queries for group/action and tenant hierarchy
- `TenantRoleProvisioner` sets hierarchy + parent (Hospital Admin = tenant root)

### Key files

| Path | Purpose |
| ---- | ------- |
| `users/enums/PermissionGroup.java` | Resource groups (PATIENT, VISIT, …) |
| `users/enums/PermissionAction.java` | READ / CREATE / WRITE / DELETE |
| `users/rbac/PermissionNaming.java` | Canonical code convention |
| `users/rbac/RoleHierarchy.java` | Levels, parents, assignability |
| `users/entity/Permission.java` | Refined permission catalog entity |
| `users/entity/Role.java` | Refined role + parent hierarchy |
| `users/repository/PermissionRepository.java` | Group/action lookups |
| `users/repository/RoleRepository.java` | Hierarchy-aware queries |
| `db/migration/V9__rbac_domain.sql` | Schema + seeds |
| `hospitals/bootstrap/TenantRoleProvisioner.java` | Tenant role hierarchy on register |

### Entity relationships (Phase 3.1)

```
Tenant (optional on Role)
   └── Role ──parent──► Role
         │
         ├──◄── user_roles ──► User
         │
         └──◄── role_permissions ──► Permission (platform catalog)
```

### Definition of Done (met)

- Domain model + migration + repositories only (no controllers / FE / authz logic)

---

## Phase 3.2 — Authorization infrastructure

### Objective

Centralize RBAC evaluation and enforcement: evaluators, resolvers, CurrentUser abstraction,
exception hierarchy, and security utilities. No controllers or business modules.

### What was done

- `CurrentUser` interface + immutable `AuthenticatedUser` implementation
- `PermissionResolver` / `DefaultPermissionResolver` (principal snapshot + User entity)
- `PermissionEvaluator` / `DefaultPermissionEvaluator` (default-deny boolean checks)
- `AuthorizationService` / `DefaultAuthorizationService` (`@Service("authz")` for SpEL)
- `CurrentUserAccessor` + `SecurityContextCurrentUserAccessor`
- `MethodSecurityPermissionEvaluator` + method-security expression handler
- AuthZ exception hierarchy (`AuthorizationException`, `PermissionDeniedException`, `RoleDeniedException`)
- `AuthorityUtils` + expanded `SecurityUtils`
- `JwtPrincipalValidator` uses `PermissionResolver`; interceptor unchanged API surface

### Key files

| Path | Purpose |
| ---- | ------- |
| `security/principal/CurrentUser.java` | Principal abstraction |
| `security/authorization/PermissionEvaluator.java` | Decision API |
| `security/authorization/PermissionResolver.java` | Effective codes |
| `security/authorization/AuthorizationService.java` | Enforcement facade |
| `security/authorization/CurrentUserAccessor.java` | Injectable current user |
| `security/authorization/MethodSecurityPermissionEvaluator.java` | Spring `hasPermission` bridge |
| `common/exception/authorization/*` | AuthZ exception hierarchy |
| `security/util/AuthorityUtils.java` | Role/permission authority helpers |

### Architecture flow

```
SecurityContext (CurrentUser snapshot)
        ↓
CurrentUserAccessor / SecurityUtils
        ↓
PermissionResolver → codes
        ↓
PermissionEvaluator → boolean
        ↓
AuthorizationService.require* → PermissionDenied / RoleDenied
        ↓
GlobalExceptionHandler → HTTP 403
```

### Definition of Done (met)

- Stateless, thread-safe infrastructure; no controllers / business modules

---

## Phase 3.3 — Permission-based authorization

### Objective

Wire declarative permission checks into reusable infrastructure so controllers stay free of
authorization logic. Enforce via interceptor (HTTP) and aspect (service methods), with a
shared `PermissionGuard` and consistent AccessDenied responses.

### What was done

- Canonical `@RequirePermission` (+ legacy `@RequiresPermission` still honored)
- `PermissionGuard` — shared enforcement API
- `PermissionAuthorizationInterceptor` — controller handlers under `/api/**`
- `PermissionAuthorizationAspect` — service/other beans; skips `@RestController`
- `PermissionAnnotationSupport` — method-over-type resolution
- Unified AccessDenied payloads (`AUTHZ_ACCESS_DENIED`) in filter handler + MVC advice
- `spring-boot-starter-aop` + `AuthorizationInfrastructureConfig`

### Package structure

```
security/annotation/RequirePermission
security/authorization/
  PermissionGuard
  PermissionAuthorizationInterceptor
  PermissionAuthorizationAspect
  PermissionAnnotationSupport
  AccessDeniedResponses
security/handler/RestAccessDeniedHandler
security/config/AuthorizationInfrastructureConfig
```

### Usage examples

```java
// Controller — declarative only
@GetMapping("/{id}")
@RequirePermission(PermissionConstants.PATIENT_READ)
ApiResponse<PatientResponse> get(@PathVariable UUID id) { ... }

// Service method
@RequirePermission(PermissionConstants.PRESCRIPTION_CREATE)
public Prescription create(CreatePrescriptionCommand cmd) { ... }

// Programmatic (rare)
permissionGuard.requireAny(PermissionConstants.VISIT_DELETE);
```

### Definition of Done (met)

- Controllers contain no imperative authZ; reusable interceptor/aspect/guard enforce permissions

---

## Phase 3.4 — Secure all existing APIs

### Objective

Apply permission-based authorization across every existing endpoint. Public auth/onboarding
paths stay anonymous; everything else verifies JWT, role, permission, and tenant. Document
authorization in Swagger and return safe 401/403 responses.

### What was done

- `@PublicEndpoint` on anonymous handlers (login, register, refresh, password/email recovery, health)
- `@RequireAuthenticated` for self-service (profile, change-password, logout, authz context)
- `@RequirePermission` retained on hospital settings + hospital-access probe
- `AuthorizationOpenApiCustomizer` — per-operation JWT/role/permission/tenant docs, `x-hms-authorization`, 401/403
- OpenAPI info section documents the authorization model
- AuthZ denials return generic `AUTHZ_ACCESS_DENIED` (permission codes logged server-side only)
- Integration coverage for 401 on protected routes and 403 on tenant mismatch

### Endpoint classification

| Endpoint | Access |
| -------- | ------ |
| `POST /auth/login` | Public |
| `POST /auth/register/hospital`, `POST /hospitals/register` | Public |
| `POST /auth/refresh-token`, forgot/reset password, verify/resend email | Public |
| `GET/PUT /auth/profile`, `POST /auth/change-password`, `POST /auth/logout` | Authenticated |
| `GET /auth/authorization` | Authenticated |
| `GET /auth/authorization/hospital-access` | `HOSPITAL_READ` |
| `GET /hospitals/settings` | `HOSPITAL_READ` |
| `PUT /hospitals/settings` | `HOSPITAL_UPDATE` |
| `GET /system/health` | Public |

### Key files

| Path | Purpose |
| ---- | ------- |
| `security/annotation/RequireAuthenticated.java` | Self-service auth marker |
| `security/annotation/PublicEndpoint.java` | Anonymous OpenAPI marker |
| `config/AuthorizationOpenApiCustomizer.java` | Swagger auth enrichment |
| `config/OpenApiConfig.java` | Authorization model in API info |
| `auth/controller/AuthController.java` | Public vs protected annotations |
| `common/exception/GlobalExceptionHandler.java` | Sanitized 403 bodies |

### Definition of Done (met)

- Existing APIs secured; only public allow-list is anonymous; Swagger documents authZ; 401/403 correct

---

## Phase 3.5 — Default system roles

### Objective

Implement and seed default system roles with a consistent `{GROUP}_{ACTION}` permission
catalog (`READ` / `CREATE` / `UPDATE` / `DELETE`), including Accountant and Billing.

### What was done

- Standardized actions: legacy `WRITE` → `UPDATE`; explicit `CREATE` for CRUD groups
- Added `BILLING_*` permissions and `ACCOUNTANT` role type
- `PermissionCatalog` + `SystemRolePermissionMatrix` as single source of truth
- `DefaultTenantRoleCatalog` provisions Accountant with matrix grants
- Flyway `V10__default_system_roles.sql` + startup `PlatformRbacBootstrap`
- Documented matrix in `docs/PERMISSION_MATRIX.md`

### Default roles

Super Admin · Hospital Admin · Doctor · Nurse · Receptionist · Pharmacist · Lab Technician · Accountant

### Key files

| Path | Purpose |
| ---- | ------- |
| `users/rbac/PermissionCatalog.java` | Full permission catalog |
| `users/rbac/SystemRolePermissionMatrix.java` | Role → permission grants |
| `users/bootstrap/PlatformRbacBootstrap.java` | Startup platform seed/sync |
| `db/migration/V10__default_system_roles.sql` | DB rename/seed/grants |
| `docs/PERMISSION_MATRIX.md` | Human-readable matrix |

### Definition of Done (met)

- Default roles seeded; naming consistent; matrix documented

---

## Phase 3.6 — Frontend authorization

### Objective

UX-only frontend authorization: mirror backend-issued roles/permissions to hide
unauthorized menus, buttons, and pages, and to block unauthorized client navigation.
Backend remains the source of truth for enforcement.

### What was done

- `PermissionProvider` + `RoleProvider` (+ composing `AuthorizationProvider`)
- Hooks: `usePermission`, `useRole`, `useAuthorization` (+ TanStack `useAuthorizationQuery`)
- Redux `authorization` slice hydrated from session profile and `GET /auth/authorization`
- `Can` / `Protected` for permission-based rendering
- `RouteProtection` + `ProtectedLayout` for page/route UX guards
- Navigation filter hides unauthorized items; route catalog prevents unauthorized navigation
- Placeholder pages under `/app/*` for hospital, users, patients, appointments, billing

### Key files

| Path | Purpose |
| ---- | ------- |
| `frontend/src/features/authorization/**` | Providers, hooks, components, route/nav catalog |
| `frontend/src/store/index.ts` | RTK store with authorization reducer |
| `frontend/src/providers/app-providers.tsx` | Wires AuthorizationProvider |
| `frontend/src/app/(protected)/layout.tsx` | Uses ProtectedLayout |

### Definition of Done (met)

- Unauthorized UI hidden; unauthorized client navigation redirected; no duplicated backend authZ logic

---

## Phase 3.7 — Dynamic navigation

### Objective

Build permission-aware application chrome so users only see pages, menus, cards, and
actions they are authorized to access. Visibility is driven by backend-issued
permissions — not hardcoded role checks.

### What was done

- `features/navigation` catalogs: sidebar/top nav, dashboard cards, quick actions
- `AppShell` composes sidebar, top navigation, breadcrumbs, and mobile drawer
- Dashboard renders permission-filtered `QuickActions` + `DashboardCards`
- Route catalog and nav items use permissions only (removed role gates from hospital UX)
- Compat re-exports kept for Phase 3.6 `APP_NAVIGATION` / `ProtectedShell` imports

### Surfaces

| Surface | Behavior |
| ------- | -------- |
| Sidebar | Sectioned nav filtered by permission |
| Top navigation | Breadcrumbs, user summary, primary quick action, mobile shortcuts |
| Breadcrumbs | Built from nav catalog for the current path |
| Dashboard cards | Module cards hidden without matching `*_READ` (or open to all authenticated) |
| Quick actions | Create/update actions hidden without matching `*_CREATE` / `*_UPDATE` |

### Key files

| Path | Purpose |
| ---- | ------- |
| `frontend/src/features/navigation/config/*` | Permission-driven catalogs |
| `frontend/src/features/navigation/components/app-shell.tsx` | App chrome |
| `frontend/src/features/navigation/components/app-sidebar.tsx` | Sidebar |
| `frontend/src/features/navigation/components/top-navigation.tsx` | Top bar |
| `frontend/src/features/navigation/components/app-breadcrumbs.tsx` | Breadcrumbs |
| `frontend/src/features/navigation/components/dashboard-cards.tsx` | Module cards |
| `frontend/src/features/navigation/components/quick-actions.tsx` | Quick actions |
| `frontend/src/features/authorization/components/protected-layout.tsx` | Wires `AppShell` |

### Definition of Done (met)

- Unauthorized menus/cards/actions hidden; no hardcoded role checks in navigation UX

---

## Phase 3.8 — Complete RBAC review

### Objective

Full review of RBAC architecture and security across backend and frontend; detect and fix
missing authorization, fail-open gaps, hardcoded roles, privilege-escalation paths, and
dependency-direction issues until no Critical findings remain.

### What was done

- Fail-closed `PermissionAuthorizationInterceptor` (unclassified `/api` handlers denied)
- Startup `ControllerAuthorizationCoverageGuard`
- `PlatformPrincipalSupport` consolidates Super Admin tenant-bypass trust bar
- `executeWithoutTenantFilter` requires platform Super Admin
- `@RequiresPermission` composed onto `@RequirePermission` via `@AliasFor`
- Service-layer `@RequirePermission` on hospital settings
- Frontend independent `PROTECTED_ROUTES` + fail-closed unknown `/app/*` paths
- Full report: [RBAC_REVIEW_PHASE_3_8.md](./RBAC_REVIEW_PHASE_3_8.md)

### Key files

| Path | Purpose |
| ---- | ------- |
| `security/authorization/PermissionAuthorizationInterceptor.java` | Fail-closed handler classification |
| `security/authorization/ControllerAuthorizationCoverageGuard.java` | Boot-time coverage check |
| `security/authorization/PlatformPrincipalSupport.java` | Shared Super Admin trust bar |
| `docs/RBAC_REVIEW_PHASE_3_8.md` | Architecture + security + recommendations |

### Definition of Done (met)

- No Critical RBAC issues remain; architecture and security reports published

---

## EQB validation (post–Phase 3.8)

Engineering Quality Board re-validated RBAC end-to-end. Critical Super Admin JWT issuance
gap and Flyway/`HOSPITAL_DELETE` drift were fixed. Full checklist:

[EQB_RBAC_VALIDATION_REPORT.md](./EQB_RBAC_VALIDATION_REPORT.md)

---

## Database migrations map (V1–V11)

| Migration | Phase | Purpose |
| --------- | ----- | ------- |
| `V1__platform_baseline.sql` | 1 | Platform metadata |
| `V2__identity_domain.sql` | 1 / 2 | Users, roles, permissions, refresh tokens, seeds |
| `V3__tenants_and_audit.sql` | 2 | Tenants, audit logs, admin grants |
| `V4__password_reset_tokens.sql` | 2 | Password reset tokens |
| `V5__email_verification.sql` | 2 | Email verification |
| `V6__tenant_foundation.sql` | 2.1 | Tenant type + FKs |
| `V7__hospitals.sql` | 2.5 | Hospitals table |
| `V8__hospital_settings.sql` | 2.6 | Settings columns |
| `V9__rbac_domain.sql` | 3.1 | Permission group/action, role hierarchy, system grants |
| `V10__default_system_roles.sql` | 3.5 | WRITE→UPDATE, CREATE/BILLING catalog, Accountant, matrix grants |
| `V11__revoke_hospital_admin_hospital_delete.sql` | EQB | Align upgraded Hospital Admin grants with matrix (revoke HOSPITAL_DELETE) |

---

## Module map (backend packages)

```
com.healthcare.hms
├── auth/          # Login, tokens, password reset, email verify
├── users/         # User / Role / Permission entities + RBAC domain (3.1)
├── tenant/        # Multi-tenant foundation (2.1–2.4, 2.7–2.8)
├── hospitals/     # Registration (2.5) + settings (2.6)
├── security/      # JWT, RBAC annotations, SecurityConfig
├── common/        # API envelope, exceptions, persistence bases, email, health
├── audit/         # Audit log write path
├── config/        # OpenAPI
├── patients/      # (empty — Phase 5+)
├── appointments/  # (empty — Phase 6)
├── visits/        # (empty — Phase 7)
├── prescriptions/ # (empty — later)
├── laboratory/    # (empty — later)
├── reports/       # (empty — later)
└── notifications/ # (empty — later)
```

---

## End-to-end lifecycle (as of Phase 2.8)

```
Hospital Registration (2.5)
        ↓
Tenant + Hospital PENDING + Admin + Roles
        ↓
Admin verifies email → Tenant/Hospital ACTIVE
        ↓
Login → JWT (tenant_id) + refresh token
        ↓
Protected API + optional confirming X-Tenant-ID
        ↓
JwtAuthenticationFilter (DB principal)
        ↓
TenantFilter (resolve → validate → bind)
        ↓
Controller → Service (@Transactional)
        ↓
Hibernate tenantFilter on Session
        ↓
Repository (tenant-isolated)
        ↓
Response → TenantContextHolder.clear()
```

---

## Not done yet (later ROADMAP phases)

| Phase | Scope |
| ----- | ----- |
| 3.x | Departments, staff assignment, multi-hospital ops beyond default hospital |
| 4 | Doctor/nurse/staff management APIs |
| 5 | Patient management |
| 6 | Appointments |
| 7 | Clinical visits |
| 8+ | Prescriptions, lab, imaging, billing, notifications UI, analytics, … |
| 2.x remaining | Super Admin JWT/platform console, cookie-based sessions |

---

## How to use this document

1. Start here for **what exists and why** by phase.  
2. Use [MULTI_TENANCY.md](./MULTI_TENANCY.md) for tenant isolation design depth.  
3. Use [API.md](./API.md) for endpoint contracts.  
4. Use [PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md) for default role permission grants.  
5. Use [ROADMAP.md](./ROADMAP.md) for upcoming work.  
6. Use [TEST_REPORT_PHASE_1_9.md](./TEST_REPORT_PHASE_1_9.md) for auth QA evidence.

---

*Last updated: EQB RBAC validation complete (2026-07).*
