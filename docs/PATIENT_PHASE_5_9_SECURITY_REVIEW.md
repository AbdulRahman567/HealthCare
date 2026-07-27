# Phase 5.9 — Patient Module Security Review

**Date:** 2026-07-26  
**Scope:** Patient APIs, Medical History, Allergies, Immunizations, Timeline, Search (Phases 5.1–5.8)  
**Constraint:** Security review + remediation — no new clinical feature modules

---

## Executive verdict

Patient Management has **no Critical security issues remaining** after Phase 5.9 remediation.

Tenant isolation, nested-resource IDOR guards, search hardening, and mutation audit logging were already solid. This phase closed High/Medium gaps around soft-delete RBAC alignment, critical-allergy downgrade abuse, PHI in application logs/timeline summaries, missing patient-view audit, national-ID uniqueness, inactive-patient clinical writes, FE route fail-closed UX, and immunization date validation.

---

## Verification checklist

| Check                      | Result | Notes                                                           |
| -------------------------- | ------ | --------------------------------------------------------------- |
| Tenant isolation           | Pass   | `TenantOwnedEntity` + `findBy…AndTenantId[AndPatientId]`        |
| Nested IDOR                | Pass   | Allergy / history / immunization triple-key lookups             |
| RBAC annotations           | Pass   | Controllers + services; soft-delete → `PATIENT_DELETE`          |
| Critical allergy downgrade | Pass   | Requires `PATIENT_DELETE` to deactivate/downgrade               |
| Search injection           | Pass   | LIKE escape + sort allowlist + page cap 100                     |
| Audit CUD                  | Pass   | Create / update / soft-delete audited                           |
| Audit VIEW                 | Pass   | `AuditAction.VIEW` on patient chart open (`getById`)            |
| PHI app logs               | Pass   | App INFO logs use opaque IDs only (no MRN / vaccine / severity) |
| Timeline PHI               | Pass   | Free-text clinical notes removed from timeline summaries        |
| FE route guards            | Pass   | `/new` → `PATIENT_CREATE`; `/*/edit` → `PATIENT_UPDATE`         |
| Input validation           | Pass   | Immunization `@PastOrPresent`; national ID uniqueness           |

---

## Findings remediated

| Severity | Issue                                                                                                                           | Fix                                                                                                          |
| -------- | ------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| High     | Clinical soft-delete used `PATIENT_UPDATE` (receptionist could remove allergies/history/immunizations); `PATIENT_DELETE` unused | Soft-delete endpoints/services require `PATIENT_DELETE` (Hospital Admin+); FE delete actions gated similarly |
| High     | Critical / life-threatening allergies could be deactivated or downgraded via UPDATE with only `PATIENT_UPDATE`                  | `AllergyServiceImpl` requires `PATIENT_DELETE` for deactivate/downgrade/clear-critical paths                 |
| Medium   | SECURITY.md “Patient View” not audited                                                                                          | Added `AuditAction.VIEW`; `getById` records view with IP/UA                                                  |
| Medium   | App logs contained PHI (MRN, vaccine name, allergy type/severity)                                                               | Redacted to entity/patient/tenant/actor IDs                                                                  |
| Medium   | Timeline summaries embedded clinical notes for any `PATIENT_READ` role                                                          | Summaries use structured category/severity/status (+ facility for surgery) only                              |
| Medium   | Clinical mutations allowed on inactive patients                                                                                 | `PatientAccessSupport.requireActivePatient` on chart writes/deletes                                          |
| Medium   | National ID uniqueness helpers unused — duplicate CNIC allowed                                                                  | Enforce uniqueness on register/update when national ID present                                               |
| Medium   | FE `/patients/new` and `/*/edit` inherited `PATIENT_READ` only                                                                  | Exact `/new` + edit-path regex guards                                                                        |
| Low      | Immunization `administrationDate` lacked `@PastOrPresent`                                                                       | Added Bean Validation constraint                                                                             |

---

## Controls confirmed (no defect)

| Area                     | Evidence                                                      |
| ------------------------ | ------------------------------------------------------------- |
| Cross-tenant isolation   | Hibernate `tenantFilter` + explicit tenant predicates         |
| Cross-patient IDOR       | Path `patientId` bound with resource id in repository queries |
| Allergy banner auth      | `PATIENT_READ` + tenant patient existence                     |
| Mass assignment          | Mappers ignore `tenantId`, soft-delete fields, patient status |
| Soft-delete query bypass | `@SQLRestriction("deleted = false")`                          |
| Sort injection           | Allowlist in `PatientServiceImpl.sanitizePageable`            |

---

## Intentionally deferred (not Critical)

| Item                                                       | Rationale                                                                                |
| ---------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| Split `PATIENT_UPDATE` into demographics vs clinical write | Product/permission-matrix redesign; Doctor/Receptionist both hold UPDATE today by design |
| Doctor “own patients” scoping                              | `primaryDoctorId` is filter-ready; assignment workflow is later                          |
| Slim list DTO vs full PHI on search                        | Authorized `PATIENT_READ` holders; clinical HMS expectation                              |
| Timeline in-memory merge DoS                               | Authenticated single-patient scope; optimize later if charts grow large                  |

---

## Definition of Done

- **No Critical** Patient module security issues remain
- Soft-delete and critical-allergy safety aligned with `PATIENT_DELETE`
- PHI reduced in app logs and timeline read models
- Patient chart open audited as `VIEW`
- FE mutation routes fail closed for create/update UX

Companion: [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md), [PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md), [SECURITY.md](./SECURITY.md)
