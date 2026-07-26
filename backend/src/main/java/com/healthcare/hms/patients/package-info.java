/**
 * Patient Management — registration, history, allergies, immunizations, timeline, search (Phases 5.1–5.7).
 *
 * <p>Owns the patient chart root used by later clinical modules (appointments,
 * visits, prescriptions). Phase 5.4 adds safety-critical allergies with banner
 * and critical-alert surfaces. Phase 5.5 adds immunization / vaccination records.
 * Phase 5.6 adds a chronological timeline aggregator (provider SPI for future modules).
 * Phase 5.7 adds directory search via JPA Specifications (pagination, sorting, filtering).
 *
 * <h2>Scope</h2>
 * <ul>
 *   <li>Phase 5.1 — {@link com.healthcare.hms.patients.entity.Patient}, enums, repository</li>
 *   <li>Phase 5.2 — registration APIs</li>
 *   <li>Phase 5.3 — structured medical history ({@link com.healthcare.hms.patients.history})</li>
 *   <li>Phase 5.4 — allergies ({@link com.healthcare.hms.patients.allergy})</li>
 *   <li>Phase 5.5 — immunizations ({@link com.healthcare.hms.patients.immunization})</li>
 *   <li>Phase 5.6 — timeline ({@link com.healthcare.hms.patients.timeline})</li>
 *   <li>Phase 5.7 — search ({@link com.healthcare.hms.patients.repository.PatientSpecifications})</li>
 *   <li>Phase 5.8+ — documents, dashboard, profile UI (roadmap)</li>
 * </ul>
 *
 * <h2>Isolation</h2>
 * Patient, history, allergy, and immunization entities extend
 * {@link com.healthcare.hms.common.persistence.TenantOwnedEntity} and inherit
 * Hibernate {@code tenantFilter} enforcement from {@link com.healthcare.hms.tenant}.
 *
 * <h2>Package layout</h2>
 * <pre>
 * patients/
 * ├── controller/      PatientController (register + search + lifecycle)
 * ├── service/         PatientService + PatientQueryService + impl
 * ├── history/         Medical history (5.3)
 * ├── allergy/         Allergies + banner/critical alerts (5.4)
 * ├── immunization/    Vaccination records + due tracking (5.5)
 * ├── timeline/        Chronological chart feed (5.6)
 * ├── mapper/          PatientMapper
 * ├── dto/request|response
 * ├── validation/
 * ├── entity/          Patient, EmergencyContact
 * ├── enums/
 * └── repository/      PatientRepository + PatientSpecifications
 * </pre>
 */
package com.healthcare.hms.patients;
