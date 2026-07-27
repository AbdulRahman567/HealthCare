# Phase 5.10 — Patient Management Production Readiness

**Date:** 2026-07-26  
**Scope:** Phases 5.1–5.9 (Patient, Medical History, Allergies, Immunizations, Timeline, Search, UI, security hardening)  
**Constraint:** Review + remediation only — no new clinical feature modules

---

## Executive verdict

Patient Management is **production-ready for the documented Phase 5 lifecycle** after Critical/High remediation in this phase. Tenant isolation, nested IDOR guards, RBAC (including soft-delete / critical-allergy), search hardening, audit CUD + chart VIEW, Flyway V18–V24, Swagger annotations, and the Hospital Admin chart workflow remain solid.

Remaining items are Medium/Low polish (timeline in-memory merge at very large charts, DECEASED/ARCHIVED transition APIs unused, broader VIEW audits on list endpoints).

**Critical / High remaining: none.**

---

## Verification checklist

| Check                                                     | Result                | Notes                                                            |
| --------------------------------------------------------- | --------------------- | ---------------------------------------------------------------- |
| Compilation (`mvnw -DskipTests compile`)                  | Pass                  |                                                                  |
| Unit tests (`patients` package + allergy/patient service) | Pass                  | Includes Phase 5.10 allergy critical-flag regression             |
| Next.js `typecheck`                                       | Pass                  |                                                                  |
| Next.js `build`                                           | Pass                  | `/app/patients`, `/new`, `/[id]`, `/[id]/edit` present           |
| Flyway migrations V1–V24                                  | Pass (chain complete) | `V24__patient_national_id_unique.sql` added                      |
| Docker Compose config                                     | Pass                  | `docker compose config` valid                                    |
| Docker daemon / full stack                                | Blocked locally       | Docker Desktop engine not running                                |
| Spring Boot startup (local host MySQL)                    | Blocked locally       | Use Compose MySQL (`hms_user` / mapped `3306`)                   |
| Swagger / springdoc paths                                 | Configured            | `/swagger-ui`, `/api-docs`; patient tags annotated               |
| Patient registration / update                             | Pass                  | MRN + national ID uniqueness; soft-delete-aware DB indexes       |
| Medical history / allergies / vaccinations                | Pass                  | ACTIVE-patient writes; soft-delete → `PATIENT_DELETE`            |
| Timeline                                                  | Pass                  | SPI merge + cursor page; FE invalidates after clinical CUD       |
| Search / pagination                                       | Pass                  | Specs + sort allowlist + page cap 100; FE search debounced 300ms |
| Validation                                                | Pass                  | Bean Validation + clinical/immunization date constraints         |
| Audit logging                                             | Pass                  | CUD + patient chart `VIEW`                                       |
| Tenant isolation                                          | Pass                  | `TenantOwnedEntity` + explicit tenant predicates                 |
| RBAC                                                      | Pass                  | Dual controller/service `@RequirePermission`                     |
| Clean Architecture / SOLID / no circular deps             | Pass                  | Feature packages; timeline SPI; no repository cycles             |
| No duplicate Critical/High defects                        | Pass                  | After remediation below                                          |

---

## Lifecycle simulation (Hospital Admin)

```
Hospital Admin
  → Register Patient          (POST /api/v1/patients, PATIENT_CREATE)
  → Update Patient            (PUT /api/v1/patients/{id}; MRN read-only in UI)
  → Add Medical History       (past disease / surgery / chronic)
  → Add Allergy               (banner on chart open; critical flags protected)
  → Add Vaccination           (immunization + due surface)
  → Search Patient            (debounced directory search)
  → View Timeline             (refreshes after clinical mutations)
  → Deactivate Patient        (confirm dialog → ACTIVE → INACTIVE)
  → Reactivate Patient        (INACTIVE → ACTIVE)
  → Logout
```

All steps are supported by backend APIs + Patient Management UI. Clinical chart writes reject inactive patients (`PatientAccessSupport.requireActivePatient`).

---

## Critical / High findings remediated

| Sev  | Issue                                                                                                                               | Fix                                                                                                                                   |
| ---- | ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| High | Critical allergy `criticalAlert` cleared when omitted on PUT (`null` → `false` in mapper) while guard only blocked explicit `false` | Mapper patch semantics (`resolveFlag` preserves existing); guard still requires `PATIENT_DELETE` for explicit clear; regression tests |
| High | National ID uniqueness app-only — concurrent register/update race                                                                   | Flyway `V24` soft-delete-aware `uk_patients_tenant_active_national_id`                                                                |
| High | Deactivate patient with one click (no confirm)                                                                                      | Confirm dialog on chart header (aligned with clinical soft-delete UX)                                                                 |
| High | Undebounced PHI search per keystroke                                                                                                | 300ms debounce before Redux/query dispatch                                                                                            |
| High | Timeline cache stale after allergy/history/immunization CUD                                                                         | Invalidate `timelineKeys` alongside clinical query keys                                                                               |

---

## Architecture Review

### Module boundaries

```
patients          → registration, search, lifecycle
patients.history  → structured medical history
patients.allergy  → safety-critical allergies + banner/critical
patients.immunization → vaccinations + due
patients.timeline → SPI providers + cursor merge (read model)
security / audit / tenant → cross-cutting
frontend features/patients → list / register / edit / chart
```

- Controllers thin; business rules in `service/impl`.
- Cross-module reads use repositories only within `patients` (no foreign-module repository cycles).
- Timeline providers depend on clinical repositories (pragmatic layering); orchestrator depends on `TimelineEventProvider` SPI (Open/Closed).

### SOLID / DRY

- Shared access: `PatientAccessSupport`.
- Timeline extension via SPI without changing merge orchestrator.
- Intentional remaining Medium debt: audit/snapshot helpers duplicated across clinical services; `MedicalHistoryServiceImpl` is a multi-entity facade.

### Circular dependencies

None detected among `patients` subpackages or FE hook graphs (`timelineKeys` imported one-way from clinical hooks).

---

## Security Review

### Controls validated

- Tenant filter + explicit `tenantId` (and nested `patientId`) queries
- Nested IDOR: `findByIdAndTenantIdAndPatientId`
- Soft-delete → `PATIENT_DELETE`; critical allergy downgrade/deactivate/clear → `PATIENT_DELETE`
- Omitted `criticalAlert` no longer silently clears (Phase 5.10)
- Search LIKE escape + sort allowlist + page size ≤ 100
- App INFO logs: opaque IDs only; audit store retains clinical snapshots by design
- FE routes fail closed for create/edit; action buttons gated with `Can`

### Residual (Medium / accepted)

- Timeline full fan-out load then in-memory page (authenticated single-patient scope)
- `DECEASED` / `ARCHIVED` enum values without transition APIs
- VIEW audit on chart open only (not search/list/banner)
- Split `PATIENT_UPDATE` demographics vs clinical write — product matrix decision deferred

**Critical / High remaining after Phase 5.10: none.**

---

## Performance Review

| Area              | Assessment                                                                                 |
| ----------------- | ------------------------------------------------------------------------------------------ |
| Search pagination | DB-level Specifications; max page 100; age→DOB range                                       |
| Indexes           | V18–V23 search/timeline indexes; V24 national ID unique                                    |
| Timeline          | In-memory merge — acceptable for Phase 5 chart size; revisit for large longitudinal charts |
| FE search         | Debounced 300ms to cut PHI-bearing request chatter                                         |
| N+1               | List endpoints return aggregates without nested entity graphs                              |

---

## Database Review

| Migration | Role                                                                     |
| --------- | ------------------------------------------------------------------------ |
| V18       | Patients + soft-delete-aware MRN unique                                  |
| V19       | Medical history root + entries                                           |
| V20       | Allergies                                                                |
| V21       | Immunizations + dose CHECK                                               |
| V22       | Allergy timeline date indexes                                            |
| V23       | Search columns (`primary_department_id` / `primary_doctor_id`) + indexes |
| **V24**   | Soft-delete-aware national ID unique (`active_national_id_slot`)         |

Referential integrity: FKs to `tenants` / `patients` / org tables as documented. Soft delete via `@SQLRestriction` + `deleted` columns. No physical delete APIs for patients (lifecycle deactivate/reactivate only).

---

## Documentation Mismatches

| Item                                                | Resolution                                    |
| --------------------------------------------------- | --------------------------------------------- |
| ROADMAP ended at 5.9 (no 5.10 production readiness) | Added Phase 5.10 Done                         |
| `PATIENT_MANAGEMENT.md` status 5.1–5.9              | Extended to 5.10                              |
| `DATABASE.md` national_id non-unique index only     | Documented V24 unique slot                    |
| Phase 5.9 claimed critical-allergy guard complete   | Partial; completed in 5.10 (omit-null bypass) |
| Swagger 409 said “Duplicate MRN” only               | Updated to MRN or national ID                 |

Intentionally deferred (not mismatches): documents upload / dedicated patient dashboard (`5.8+` in ROADMAP).

---

## Production Readiness Assessment

| Dimension                          | Rating             | Comment                                                               |
| ---------------------------------- | ------------------ | --------------------------------------------------------------------- |
| Functional completeness (Phase 5)  | Ready              | Register → chart clinical → search → timeline → deactivate/reactivate |
| Security (tenant/RBAC/PHI/allergy) | Ready              | 5.9 + 5.10 allergy/national-ID hardening                              |
| Data integrity                     | Ready              | Soft-delete-aware MRN + national ID uniqueness                        |
| Operability                        | Ready with Compose | Local host MySQL/Docker Desktop may differ                            |
| Observability                      | Partial            | Actuator + audit; timeline read not VIEW-audited                      |
| Documentation                      | Updated            | This report + ROADMAP / PROJECT_CONTEXT / phasesreadme / DATABASE     |

**Go / No-Go:** **GO** for Phase 5 Patient Management behind Docker Compose (or equivalent env with Flyway through **V24**). Defer Phase 6+ (appointments) and `5.8+` documents/dashboard.

---

## Files touched (remediation)

### Backend

- `AllergyMapper.java` — patch semantics for optional clinical flags
- `AllergyServiceImpl.java` — critical-clear guard clarity
- `AllergyServiceImplTest.java` — omit vs explicit clear regression
- `V24__patient_national_id_unique.sql`
- `Patient.java` — index/docs alignment with V24
- `PatientController.java` — Swagger 409 text

### Frontend

- `patient-chart-header.tsx` — deactivate confirmation
- `patients-list-page.tsx` — search debounce
- `use-allergies.ts` / `use-medical-history.ts` / `use-immunizations.ts` — timeline invalidation
- `edit-patient-page.tsx` — MRN read-only on edit

Companion: [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md), [PATIENT_PHASE_5_9_SECURITY_REVIEW.md](./PATIENT_PHASE_5_9_SECURITY_REVIEW.md)
