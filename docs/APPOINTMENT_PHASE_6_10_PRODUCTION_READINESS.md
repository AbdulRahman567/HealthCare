# Phase 6.10 — Appointment Module Production Readiness

**Date:** 2026-07-27  
**Scope:** Phases 6.1–6.9 (domain, availability, booking, queue, calendar, search, UI, reminders, security)  
**Constraint:** Review + remediation only — no new clinical feature modules

---

## Executive verdict

Appointment Management is **production-ready for the documented Hospital Admin → OPD lifecycle** after Critical/High remediation in this phase.

Build gates (backend compile/package, frontend build, Flyway V25–V32, OpenAPI tags, dual-layer RBAC, tenant isolation, doctor scope from 6.9) pass. This phase closed lifecycle integrity gaps between appointments and queue, concurrent double-booking races, missing availability UI for the booking prerequisite, queue destructive UX, and PHI fan-out on list/queue labels.

**Critical / High remaining: none.**

---

## Verification checklist

| Check | Result | Notes |
| ----- | ------ | ----- |
| Compilation (`mvnw -DskipTests compile`) | Pass | |
| Package (`mvnw -DskipTests package`) | Pass | |
| Unit tests (appointment conflict guard) | Pass | Booking lock + conflict regression |
| Next.js `build` | Pass (pre-remediation gate) | Re-validate after FE availability/queue UX |
| Flyway migrations V1–V32 | Pass | `V32` soft-delete-aware queue↔appointment unique |
| Docker Compose config | Pass | Reminder env wired; scheduler default **false** |
| Docker daemon / full stack | Blocked locally if engine down | Use Compose MySQL/`hms_user` |
| Spring Boot startup | Blocked without DB | Flyway + `ddl-auto=validate` on Compose MySQL |
| Swagger / springdoc | Pass | Appointments, Schedules, Unavailability, Queue, Calendars |
| Booking / reschedule / cancel | Pass | Pessimistic doctor+patient locks; cancel clears queue |
| Doctor availability | Pass | API + FE `/app/appointments/availability` |
| Queue management | Pass | Lock on start-consult; soft-delete cancel frees re-check-in |
| Calendar / search / pagination | Pass | Batch enrichers; sort allowlist; page caps |
| Validation | Pass | Bean Validation + slot validators |
| Audit logging | Pass | CUD + appointment VIEW |
| Tenant isolation / RBAC | Pass | Post-6.9 controls retained |
| Clean Architecture / SOLID / no circular deps | Pass | One-way appointments → org/patients/hospitals |
| No Critical/High remaining | Pass | After remediation below |

---

## Lifecycle simulation (Hospital Admin)

```
Hospital Admin
  → Create Department / Doctor     (Phase 4 APIs + UI)
  → Configure Availability         (GET/POST /doctors/{id}/schedules + FE Availability)
  → Register Patient               (Phase 5)
  → Book Appointment               (POST /appointments; requires ACTIVE schedule)
  → Check In Patient               (POST /queues/check-in)
  → Patient Enters Queue           (WAITING)
  → Doctor Starts Consultation     (pessimistic queue lock; single IN_CONSULTATION)
  → Appointment Completed          (queue complete → appointment COMPLETED)
  → View Calendar / Search         (labels enriched server-side)
  → Logout
```

Cancel paths:
- **Cancel appointment** → terminates live queue entry (cancel + soft-delete) + cancels reminders.
- **Cancel queue entry** → soft-deletes entry (frees unique slot for re-check-in); appointment stays bookable.

---

## Critical / High findings remediated

| Sev | Issue | Fix |
| --- | ----- | --- |
| Critical | Appointment cancel left live queue entries (staff could continue OPD) | Cancel terminates non-terminal queue entries (cancel + soft-delete) |
| Critical | Queue cancel kept unique `appointment_id` → stuck re-check-in; appointment desync | Flyway `V32` soft-delete-aware unique; queue cancel soft-deletes entry |
| High | Concurrent double-booking TOCTOU | Pessimistic locks on patient+doctor (UUID-ordered) before conflict checks |
| High | Concurrent double `IN_CONSULTATION` | `findForUpdate` on doctor day queue before start |
| High | No FE to configure availability (booking hard-requires schedule) | Availability page + API client under appointments |
| High | Queue Missed/Cancel one-click | Confirm dialog (aligned with appointment cancel UX) |
| High | FE patient label N+1 GETs | Server enrich `patientName`/`patientMrn` on appointment + queue responses; FE prefers enriched fields |
| High | Reminder env undocumented / not in Compose | `.env.example` + Compose `REMINDERS_*` (scheduler still default off) |
| High | Zero appointment automated tests | `AppointmentConflictGuardTest` |

---

## Architecture Review

### Module boundaries

```
appointments              → booking, search, lifecycle
appointments.availability → schedules / unavailability
appointments.queue        → daily OPD board
appointments.calendar     → daily/weekly/monthly read models
appointments.reminder     → schedule/dispatch (optional scheduler)
```

- Controllers thin; rules in `*ServiceImpl` + support guards (`AppointmentActorScopeSupport`, `AppointmentConflictGuard`, `DoctorAvailabilityBookingGuard`).
- Cross-module reads use repositories / query services (same pragmatism as patient timeline).
- **No circular package deps** among appointments ↔ organization ↔ patients ↔ hospitals.

### SOLID / DRY

- Shared actor scope and conflict locking.
- Label enrichment reused for list/detail (appointments) and queue boards.
- Intentional Medium debt: large service classes; reminder stubs for SMS/push.

---

## Security Review

### Controls validated (includes Phase 6.9)

- Tenant filter + explicit `tenantId` predicates
- Nested schedule IDOR; doctor self-scope; patient portal staff deny (`V31`)
- Dual `@RequirePermission`; schedule ACTIVE→INACTIVE requires `APPOINTMENT_DELETE`
- Search hardening; email recipient masking
- Audit CUD + appointment VIEW
- Queue mutations reject non-bookable appointments (except terminal cancel path)

### Residual (Medium / accepted)

- Nurse/Accountant hospital-wide `APPOINTMENT_READ`
- Dedicated patient portal self-scoped APIs not shipped
- Reminder scheduler off by default (ops must enable explicitly)

---

## Performance Review

| Area | Verdict |
| ---- | ------- |
| Calendar enricher | Pass — fixed batch queries |
| Appointment list labels | Pass after server enrich |
| Queue board labels | Pass — batch patient names in `QueueMapper` |
| Search | Specs + indexes V25/V29; page ≤ 100 |
| Booking concurrency | Serialized per doctor/patient via row locks |
| Reminder dispatch | Batch size 50; scheduler optional |

---

## Database Review

| Migration | Role |
| --------- | ---- |
| V25–V27 | Appointments + booking metadata |
| V26 | Availability schema |
| V28 | Queues (original unique appointment) |
| V29–V30 | Search indexes + reminders |
| V31 | Revoke PATIENT `APPOINTMENT_READ` |
| **V32** | Soft-delete-aware `uk_queue_entries_active_appointment` |

Optimistic locking (`@Version`) retained. Soft-delete appointment number uniqueness unchanged (V25).

---

## Documentation mismatches (closed / noted)

| Item | Resolution |
| ---- | ---------- |
| ROADMAP/phasesreadme ended at 6.9 | Updated for Phase 6.10 Done |
| Availability UI claimed done but missing | FE Availability page added |
| Reminder Compose/env silence | Documented + wired |
| API.md may still mention DELETE appointment / complete on appointment | Medium — API remains queue-complete; no appointment DELETE |

---

## Production Readiness Assessment

| Dimension | Rating |
| --------- | ------ |
| Functional lifecycle | Ready |
| Security (post-6.9 + 6.10 integrity) | Ready |
| Data integrity (booking + queue) | Ready |
| Operability (reminders default off) | Ready with explicit enable |
| Test coverage | Minimal but targeted; expand in later QA phase |
| Full Docker e2e on this host | Depends on Docker Desktop |

**Go / No-Go:** **GO** for Phase 6 Appointment Management behind Docker Compose (or equivalent env with Flyway through **V32**). Defer Phase 7+ clinical visits and patient portal self-scoped appointments.

Companion: [APPOINTMENT_PHASE_6_9_SECURITY_REVIEW.md](./APPOINTMENT_PHASE_6_9_SECURITY_REVIEW.md), [PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md), [PROJECT_CONTEXT.md](./PROJECT_CONTEXT.md)
