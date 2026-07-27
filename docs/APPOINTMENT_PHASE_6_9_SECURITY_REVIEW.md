# Phase 6.9 — Appointment Module Security Review

**Date:** 2026-07-27  
**Scope:** Appointment APIs, Availability, Booking, Queue, Calendar, Search, Reminders (Phases 6.1–6.8)  
**Constraint:** Security review + remediation — no new clinical feature modules

---

## Executive verdict

Appointment Management has **no Critical security issues remaining** after Phase 6.9 remediation.

Tenant isolation, dual-layer RBAC, nested schedule IDOR guards, search hardening, and schedule soft-delete `APPOINTMENT_DELETE` alignment were already solid. This phase closed Critical/High gaps around patient-portal staff `APPOINTMENT_READ`, doctor horizontal privilege, missing VIEW audits, PHI in email logs, schedule deactivate-via-UPDATE bypass, and reminder queries without explicit `tenantId`.

---

## Verification checklist

| Check                         | Result | Notes                                                                 |
| ----------------------------- | ------ | --------------------------------------------------------------------- |
| Tenant isolation              | Pass   | `TenantOwnedEntity` + explicit `tenantId` predicates; no native SQL   |
| Nested schedule IDOR          | Pass   | `findByIdAndTenantIdAndDoctorId`                                      |
| RBAC controller + service     | Pass   | Dual `@RequirePermission` on all staff HTTP services                  |
| Patient portal staff APIs     | Pass   | `APPOINTMENT_READ` revoked; deny-role guard on appointment surfaces   |
| Doctor self-scoping           | Pass   | Doctor-only actors limited to own doctor profile                      |
| Soft-delete → `*_DELETE`      | Pass   | Schedules / unavailability; ACTIVE→INACTIVE also requires DELETE      |
| Search injection              | Pass   | LIKE escape + sort allowlist + page cap 100 + text length cap 100     |
| Audit CUD                     | Pass   | Booking / availability / queue / reminders                            |
| Audit VIEW                    | Pass   | `AuditAction.VIEW` on appointment `getById`                           |
| PHI email logs                | Pass   | Masked recipient; body never logged                                   |
| FE create/update fail-closed  | Pass   | `/new` → CREATE; edit regex → UPDATE                                  |

---

## Findings remediated

| Severity | Issue                                                                                                      | Fix                                                                                                          |
| -------- | ---------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| Critical | Patient portal role held staff `APPOINTMENT_READ` with no self-scoping (tenant-wide PHI via list/calendar) | Revoked from matrix + Flyway `V31`; deny PATIENT role on staff appointment surfaces                          |
| High     | Doctor (no admin/receptionist elevation) could read/mutate any colleague’s appointments/queue/calendar     | `AppointmentActorScopeSupport` forces own `doctorId` on booking, search, queue, calendar, availability       |
| Medium   | Appointment chart open not audited                                                                         | `AuditAction.VIEW` on `getById` with IP/UA                                                                   |
| Medium   | Email senders logged full recipient (+ body in logging sender)                                             | `EmailLogRedaction.maskRecipient`; body length only                                                          |
| Medium   | Schedule `status=INACTIVE` via UPDATE bypassed soft-delete DELETE privilege                                | ACTIVE→INACTIVE (and create-as-INACTIVE) requires `APPOINTMENT_DELETE`                                       |
| Medium   | Reminder cancel/exists queries omitted explicit `tenantId`                                                 | Tenant-scoped repository methods; cancel API takes `tenantId`                                                |
| Low      | Unbounded search free-text params                                                                          | `@Size(max=100)` + service truncate                                                                          |
| Low      | App logs included appointment numbers / patientId                                                          | Opaque appointment/doctor/tenant/actor IDs                                                                   |

---

## Controls confirmed (no defect)

| Area                       | Evidence                                                                |
| -------------------------- | ----------------------------------------------------------------------- |
| Cross-tenant isolation     | Hibernate `tenantFilter` + explicit tenant predicates                   |
| Nested schedule IDOR       | Path `doctorId` bound with resource id                                  |
| Soft-delete schedules      | `APPOINTMENT_DELETE` on DELETE endpoints                                |
| Mass assignment            | Explicit mappers; no client `tenantId` / soft-delete / appointment #    |
| Booking guards             | Active patient/doctor, conflicts, availability, past-slot rejection     |
| Search LIKE / sort         | Escaped LIKE; sort allowlist; page size caps                            |
| Reminder dispatch          | Cross-tenant due query intentional; loads by reminder’s own `tenantId`  |
| FE route catalog           | Fail-closed create/edit path permissions                                |

---

## Intentionally deferred (not Critical)

| Item                                                         | Rationale                                                                              |
| ------------------------------------------------------------ | -------------------------------------------------------------------------------------- |
| Nurse / Accountant hospital-wide `APPOINTMENT_READ`          | Operational OPD / billing workflow; not patient-portal privilege escalation            |
| Dedicated patient self-scoped portal appointment APIs        | Product module not in Phase 6.9; staff APIs correctly deny PATIENT until portal ships  |
| Slim calendar DTO (drop MRN for some roles)                  | Authorized staff with `APPOINTMENT_READ`; refine later if least-privilege product asks |
| Split cancel into dedicated `APPOINTMENT_CANCEL` permission  | Cancel remains a lifecycle UPDATE; soft-delete DELETE path already separated           |

---

## Definition of Done

- **No Critical** Appointment module security issues remain
- Patient portal cannot use staff appointment APIs
- Doctor-only actors are scoped to their own doctor profile
- Appointment VIEW audited; email PHI redacted from logs
- Schedule deactivation aligned with `APPOINTMENT_DELETE`
- Flyway chain through `V31`

Companion: [PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md), [SECURITY.md](./SECURITY.md), [PROJECT_CONTEXT.md](./PROJECT_CONTEXT.md)
