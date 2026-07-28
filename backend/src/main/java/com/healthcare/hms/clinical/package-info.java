/**
 * Clinical Workflow — consultation encounters, diagnoses, notes, vitals, follow-ups (Phase 7.1).
 *
 * <p>Digitizes doctor consultations as structured, tenant-isolated clinical records linked to
 * patients, doctors, and optionally appointments. Prescriptions remain a later phase.
 *
 * <h2>Phase 7.1 — Domain model</h2>
 * Persistence foundation: {@link com.healthcare.hms.clinical.entity.Consultation} aggregate,
 * child entities, enums, repositories, Flyway {@code V33__clinical_workflow.sql}.
 *
 * <h2>Phase 7.2 — Consultation management</h2>
 * REST APIs under {@code /api/v1/consultations}: create, search, lifecycle (start/pause/resume/complete),
 * and clinical documentation (chief complaint, HPI, physical exam, doctor notes, summary, advice).
 * Flyway {@code V34__consultation_clinical_documentation.sql}.
 *
 * <h2>Architecture</h2>
 * <ul>
 *   <li>{@link com.healthcare.hms.clinical.entity.Consultation} is the encounter aggregate root</li>
 *   <li>Child rows: {@link com.healthcare.hms.clinical.entity.Diagnosis},
 *       {@link com.healthcare.hms.clinical.entity.ClinicalNote},
 *       {@link com.healthcare.hms.clinical.entity.VitalSigns},
 *       {@link com.healthcare.hms.clinical.entity.FollowUp}</li>
 *   <li>All entities extend {@link com.healthcare.hms.common.persistence.TenantOwnedEntity}</li>
 *   <li>Cross-module links use UUID FKs (Patient, Doctor, Appointment) — no {@code @ManyToOne}</li>
 *   <li>Tenant isolation via Hibernate {@code tenantFilter}; soft-delete via {@code @SQLRestriction}</li>
 * </ul>
 *
 * <h2>Package layout</h2>
 * <pre>
 * clinical/
 * ├── controller/      ConsultationController (7.2)
 * ├── service/         ConsultationService (+ impl)
 * ├── dto/             request | response
 * ├── mapper/
 * ├── support/         access, actor scope, number generator, label enricher
 * ├── entity/          Consultation, Diagnosis, ClinicalNote, VitalSigns, FollowUp
 * ├── enums/           ConsultationStatus, DiagnosisType, DiagnosisStatus, …
 * └── repository/      *Repository interfaces + ConsultationSpecifications
 * </pre>
 *
 * <p>Design reference: {@code docs/CLINICAL_WORKFLOW.md}
 */
package com.healthcare.hms.clinical;
