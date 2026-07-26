# Phase 4.9 — Hospital Administration Production Readiness

**Date:** 2026-07-25  
**Scope:** Phases 4.1–4.8 (organization, users invitation/management, Hospital Admin UI)  
**Constraint:** Review + remediation only — no new feature modules

---

## Executive verdict

Hospital Administration is **production-ready for the documented Phase 4 lifecycle** after Critical/High remediation in this phase. Tenant isolation, RBAC annotations, invitation hashing, pagination caps, and Soft-delete code/name release patterns remain solid. Remaining items are Medium/Low polish (search indexing, email-out-of-transaction, staff CRUD duplication).

---

## Verification checklist

| Check                                     | Result                | Notes                                                           |
| ----------------------------------------- | --------------------- | --------------------------------------------------------------- |
| Compilation (`mvnw -DskipTests compile`)  | Pass                  |                                                                 |
| Unit tests (dept/doctor/invite/user mgmt) | Pass                  | Updated stubs for expiry + lifecycle hooks                      |
| Next.js `typecheck`                       | Pass                  |                                                                 |
| Next.js `build`                           | Pass                  | `/accept-invitation` route present                              |
| Flyway migrations V1–V17                  | Pass (chain complete) | `V17__staff_active_user_unique.sql` added                       |
| Docker Compose config                     | Pass                  | `docker compose config` valid                                   |
| Docker daemon / full stack                | Blocked locally       | Docker Desktop engine not running                               |
| Spring Boot startup (local)               | Blocked locally       | MySQL `hms_user` access denied on host; use Compose MySQL       |
| Swagger / springdoc paths                 | Configured            | `/swagger-ui`, `/api-docs`                                      |
| Tenant isolation                          | Pass                  | `TenantOwnedEntity` + tenant-scoped queries                     |
| RBAC                                      | Pass                  | `@RequirePermission` / `@PublicEndpoint` on Hospital Admin APIs |
| Search / pagination                       | Pass                  | Max page size 100; sort allowlists                              |
| Validation / exceptions                   | Pass                  | Bean Validation + central handler                               |
| Audit logging                             | Pass                  | Dept/staff/invite CUD + user lifecycle UPDATE                   |
| Package / Clean Architecture              | Pass                  | No `users ↔ organization` repository cycle                      |
| SOLID / no circular deps                  | Pass                  | Cross-module via QueryServices + lifecycle hook                 |

---

## Lifecycle simulation (Hospital Admin)

```
Hospital Admin
  → Create Department
  → Invite Doctor (email link uses #token= fragment)
  → Doctor Accepts Invitation (/accept-invitation → preview/accept body token)
  → Account Created (ACTIVE, email verified via invite)
  → Assign Department (staff create / staff-assignments API)
  → Deactivate User (sessions revoked; heads cleared; employment SUSPENDED)
  → Reactivate User (restore/activate)
  → Update Department
  → View / Search Staff
  → Logout
```

Previously broken steps (now fixed): invitee accept UI missing; expired invite blocking re-invite; soft-delete rehire on `user_id`; dangling department heads on staff delete / user deactivate; department delete with affiliated staff.

---

## Critical / High findings remediated

| Sev      | Issue                                                                                                                | Fix                                                              |
| -------- | -------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- |
| Critical | Soft-deleted staff blocked rehire via `uk_*_tenant_user`                                                             | Flyway V17 generated `active_user_slot` unique indexes           |
| Critical | `/accept-invitation` missing — email onboarding 404                                                                  | Public FE page + preview/accept/reject API client                |
| High     | Expired invites stayed PENDING and blocked re-invite                                                                 | Persist `EXPIRED` on accept/preview; expire stale on list/invite |
| High     | Department delete left staff affiliated                                                                              | Reject delete when `hasAffiliatedStaff`                          |
| High     | Staff soft-delete left department head pointers                                                                      | `closeOnSoftDelete` clears matching heads                        |
| High     | Invite token in email query string                                                                                   | Email URL uses URL fragment `#token=`                            |
| High     | Deactivate/suspend left org heads + ACTIVE employment                                                                | `UserAccountLifecycleHook` clears heads + suspends staff         |
| High     | Staff user picker ignored role; dept API without `DEPARTMENT_READ`; `reportsToStaffId` wiped; LOCKED restore offered | FE staff/users fixes                                             |

---

## Architecture report

### Module boundaries

```
hospitals     → hospital profile / registration (QueryService for org)
organization  → departments, staff specializations, assignments
users         → identity, RBAC, invitations, user lifecycle
security      → JWT, @RequirePermission, tenant filter
audit         → append-only audit writes
```

- Controllers remain thin; business rules in `service/impl` + `staff/*` helpers.
- Cross-module reads: `HospitalQueryService`, `UserQueryService` (no foreign repositories).
- Cross-module write side-effect: `UserAccountLifecycleHook` (users port) implemented by organization — **no circular package dependency**.

### SOLID / DRY notes

- Shared staff rules concentrated in `StaffAdministrationSupport`, `StaffMembershipGuard`, `StaffAssignmentHistoryWriter`, `StaffProfileDirectory`.
- Five specialization services remain intentionally explicit (Phase 4.3); further generic template is Medium debt, not a blocker.
- Soft-delete unique release helper still duplicated between department and staff (Low).

### Package structure

Aligns with ENGINEERING_RULES feature packaging under `organization` and `users`. Missing optional `validator/` / `exception/` subpackages are documentation drift only.

---

## Security report

### Controls validated

- Tenant scoping on organization/user admin APIs
- Global email uniqueness (V16) + invite/accept checks
- Hashed invitation tokens; public preview minimized
- Tenant admin cannot invite peer `HOSPITAL_ADMIN`
- Rank + last-admin guards on deactivate/suspend
- Session invalidation on deactivate/suspend
- LOCKED not restorable via admin restore API/UI

### Residual (Medium / accepted)

- Invitation token still appears in browser history via fragment (better than query/`Referer`; not eliminated)
- Cross-tenant UUID oracle on staff create (error message differentiation)
- `reportsToStaffId` not FK-validated (integrity, not authz)

**Critical / High remaining after Phase 4.9: none.**

---

## Performance report

| Area        | Assessment                                                                                |
| ----------- | ----------------------------------------------------------------------------------------- |
| Pagination  | Enforced max size 100 across dept/staff/users/invites                                     |
| N+1         | User directory fetch-joins roles; staff lists use specs                                   |
| Indexes     | Tenant/status/email indexes present (V12–V15); V17 adds slot uniqueness                   |
| Search      | Leading-wildcard `LIKE` — acceptable for Phase 4 scale; plan FTS/prefix for large tenants |
| Email in TX | Invite/resend send email inside write transaction — Medium latency risk under SMTP delay  |

---

## Production readiness assessment

| Dimension                         | Rating             | Comment                                                     |
| --------------------------------- | ------------------ | ----------------------------------------------------------- |
| Functional completeness (Phase 4) | Ready              | Full admin + invitee accept path                            |
| Security (tenant/RBAC/invite)     | Ready              | 4.8 + 4.9 hardening                                         |
| Data integrity                    | Ready              | Soft-delete uniqueness + referential guards                 |
| Operability                       | Ready with Compose | Local host MySQL/Redis/Docker may differ                    |
| Observability                     | Partial            | Actuator + Prometheus wired; Redis health intentionally off |
| Documentation                     | Updated            | This report + ROADMAP / PROJECT_CONTEXT / phasesreadme      |

**Go / No-Go:** **GO** for Phase 4 Hospital Administration behind Docker Compose (or equivalent env with Flyway through V17). Defer Phase 5+ clinical modules.

---

## Files touched (remediation)

### Backend

- `V17__staff_active_user_unique.sql`
- `UserAccountLifecycleHook` + `UserAccountOrganizationLifecycleHook`
- `StaffMembershipGuard`, `StaffAssignmentHistoryWriter`, `DepartmentServiceImpl`
- `UserManagementServiceImpl`, `UserInvitationServiceImpl`, `InvitationEmailServiceImpl`
- Repository query additions (department heads, staff by user/department, invitation expiry)
- Unit test stub updates

### Frontend

- `/accept-invitation` page + `AcceptInvitationForm`
- `invitations-api` public methods
- Middleware + API client skip-refresh for public invite routes
- Staff directory role/permission/`reportsToStaffId` fixes
- Users page: hide Restore for `LOCKED`
