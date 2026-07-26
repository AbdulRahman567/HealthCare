# DATABASE.md

# Healthcare Management System (HMS)

## Enterprise Database Design

Version: 1.0
Status: Draft
Author: Engineering Team

---

# 1. Purpose

This document defines the complete database architecture for the Healthcare Management System (HMS).

Goals:

- Multi-Tenant Ready
- Highly Scalable
- ACID Compliant
- Audit Friendly
- Secure
- Healthcare Oriented
- Normalized Database Design
- Production Ready

---

# 2. Database Technology

| Component | Technology                              |
| --------- | --------------------------------------- |
| Database  | MySQL 8.x                               |
| ORM       | Spring Data JPA + Hibernate             |
| Migration | Flyway                                  |
| Cache     | Redis                                   |
| Search    | MySQL Full Text (Future: Elasticsearch) |

---

# 3. Database Principles

Every table must contain:

- UUID Primary Key
- Tenant ID
- Created At
- Updated At
- Created By
- Updated By
- Soft Delete
- Version (Optimistic Locking)

Example

id
tenant_id
created_at
updated_at
created_by
updated_by
deleted
version

---

# 4. Multi-Tenant Strategy

**Chosen approach: Shared Database + Shared Schema + Tenant ID.**

Full design: [MULTI_TENANCY.md](./MULTI_TENANCY.md).

Every business table includes

tenant_id

Example

Hospital A

tenant_id = UUID-A

Hospital B

tenant_id = UUID-B

Application never exposes records across tenants.

Referential integrity: `tenant_id` FKs point to `tenants(id)` (nullable for platform rows).

---

# 5. Naming Convention

Tables

snake_case

patient

doctor

appointment

Columns

snake_case

first_name

last_name

created_at

Foreign Keys

<entity>_id

patient_id

doctor_id

department_id

Indexes

idx_patient_name

idx_patient_phone

idx_visit_date

Unique Keys

uk_email

uk_license_number

---

# 6. Core Tables

## Tenant

Stores hospitals / clinics (tenant aggregate root). Table name: `tenants`.

Phase 2.5 also creates a tenant-owned `hospitals` row as the default hospital profile
(`is_default = true`, code `DEFAULT`). Tenant remains the isolation boundary.

Phase 2.6 extends `hospitals` with operational settings columns (logo, timezone,
currency, language, contact extras, structured address fields, working_hours JSON)
exposed via `GET/PUT /api/v1/hospitals/settings`.

Fields

id (UUID)
name
slug
tenant_type
email
phone
address
logo_url
subscription_plan
status
created_at
updated_at
created_by
updated_by
deleted
deleted_at
deleted_by
version

---

## Hospitals

Tenant-owned operational hospital profile / settings. Table name: `hospitals`.

Fields

id (UUID)
tenant_id
name
code
email
phone
address
description
logo_url
timezone (default UTC)
currency (ISO 4217, default USD)
language (BCP 47, default en)
website
secondary_phone
city
state_province
country
postal_code
working_hours (JSON weekly schedule)
is_default
status
created_at
updated_at
created_by
updated_by
deleted
deleted_at
deleted_by
version

---

## Users

users

Fields

id
tenant_id
role_id
first_name
last_name
email
password
phone
status
last_login
created_at

---

## Roles

roles

Tenant-aware (`tenant_id` nullable for platform system templates).

Fields

id (UUID)
tenant_id (nullable — platform system roles)
name
type (RoleType enum)
description
system_role
hierarchy_level (lower = higher privilege)
assignable
parent_role_id (self-FK; tenant HOSPITAL_ADMIN is root)
created_at / updated_at / created_by / updated_by
deleted / deleted_at / deleted_by
version

Default system roles

Super Admin (platform only, level 0)
Hospital Admin (level 10)
Doctor (level 20)
Nurse / Receptionist / Lab Technician / Pharmacist (level 30)
Patient (level 40, not assignable in MVP)

Hierarchy (structural — effective access is still explicit `role_permissions`):

```
SUPER_ADMIN
  └── HOSPITAL_ADMIN
        ├── DOCTOR
        ├── NURSE
        ├── RECEPTIONIST
        ├── LAB_TECHNICIAN
        ├── PHARMACIST
        └── PATIENT
```

---

## Permissions

permissions

Platform-global catalog (not tenant-owned). Grants are tenant-isolated via roles.

Fields

id (UUID)
code (`{GROUP}_{ACTION}`, e.g. PATIENT_READ)
name
description
permission_group (PermissionGroup enum)
action (PermissionAction: READ | CREATE | WRITE | DELETE)
system_permission
created_at / updated_at / created_by / updated_by
deleted / deleted_at / deleted_by
version

Unique: code; (permission_group, action)

Example

PATIENT_READ

PATIENT_CREATE

PATIENT_UPDATE

PATIENT_DELETE

PRESCRIPTION_CREATE

---

## Role Permissions

role_permissions

role_id

permission_id

---

# 7. Hospital Structure

## Phase 4.1 — Staff foundation (no tables yet)

Domain contract: [HOSPITAL_ORGANIZATION.md](./HOSPITAL_ORGANIZATION.md).

`Staff` is a `@MappedSuperclass` (not a concrete table). When concrete subtypes
are introduced, each subtype table inherits:

| Column                                            | Notes                                                              |
| ------------------------------------------------- | ------------------------------------------------------------------ |
| id                                                | UUID PK                                                            |
| tenant_id                                         | FK → tenants                                                       |
| hospital_id                                       | FK → hospitals                                                     |
| user_id                                           | FK → users                                                         |
| department_id                                     | FK → departments (nullable until departments exist)                |
| reports_to_staff_id                               | Self-FK for reporting hierarchy                                    |
| employee_code                                     | Unique per tenant (enforced on concrete tables)                    |
| job_title                                         | Optional                                                           |
| employment_status                                 | PENDING / ACTIVE / ON_LEAVE / SUSPENDED / TERMINATED               |
| employment_type                                   | FULL_TIME / PART_TIME / CONTRACT / TEMPORARY / INTERN / CONSULTANT |
| hired_at / terminated_at                          | Employment dates                                                   |
| created_at / updated_at / created_by / updated_by | Audit                                                              |
| deleted / deleted_at / deleted_by / version       | Soft delete + optimistic lock                                      |

`DepartmentType` enum is ready for the future `departments` table
(CLINICAL, DIAGNOSTIC, EMERGENCY, ADMINISTRATIVE, SUPPORT, RESEARCH, OTHER).

---

## Target structure (later phases) — departments implemented in Phase 4.2

### departments

Table: `departments` (Flyway `V12__departments.sql`)

| Column                                            | Notes                                                                           |
| ------------------------------------------------- | ------------------------------------------------------------------------------- |
| id                                                | UUID PK                                                                         |
| tenant_id                                         | FK → tenants                                                                    |
| hospital_id                                       | FK → hospitals                                                                  |
| name                                              | Unique per tenant                                                               |
| code                                              | Unique per tenant (stored uppercase)                                            |
| description                                       | Optional                                                                        |
| department_type                                   | CLINICAL / DIAGNOSTIC / EMERGENCY / ADMINISTRATIVE / SUPPORT / RESEARCH / OTHER |
| status                                            | ACTIVE / INACTIVE / SUSPENDED                                                   |
| location                                          | Optional                                                                        |
| head_user_id                                      | Optional FK → users (head of department)                                        |
| created_at / updated_at / created_by / updated_by | Audit                                                                           |
| deleted / deleted_at / deleted_by / version       | Soft delete + optimistic lock                                                   |

Indexes: tenant, hospital, status, type, head_user, name, deleted.

Soft delete suffixes `code`/`name` with `__DEL__{id8}` so unique keys can be reused.

Examples

Cardiology

Neurology

Orthopedics

Dermatology

Radiology

Laboratory

Emergency

---

doctors

doctor_profile (Phase 4.3 table: `doctors`)

Fields

user_id

department_id

specialization

license_number

experience_years

qualification

consultation_fee

(+ inherited Staff employment columns)

Soft-delete uniqueness (Phase 4.9 / Flyway `V17`): live rows keep
`(tenant_id, user_id)` unique via generated `active_user_slot`; soft-deleted rows free
the slot for rehire.

---

nurses (`nurses`)

department_id, shift, qualification, license_number (+ Staff columns)

---

receptionists (`receptionists`)

desk_location, languages (+ Staff columns)

---

laboratory_staff

specialty_area, license_number, certification (+ Staff columns)

---

pharmacists

license_number, pharmacy_location, qualification (+ Staff columns)

---

# 8. Patient Module

## patients (Phase 5.1 — Flyway `V18__patients.sql`)

Tenant-owned patient registration (demographics + identifiers). Full design:
[PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md).

Fields

id (UUID)
tenant_id
mrn
first_name
last_name
date_of_birth
gender (enum: MALE, FEMALE, OTHER, UNKNOWN)
blood_group (enum: A_POSITIVE … O_NEGATIVE, UNKNOWN)
national_id (CNIC or passport — optional)
phone
email
address
emergency_contact_name
emergency_contact_phone
emergency_contact_relation
marital_status (enum: SINGLE, MARRIED, DIVORCED, WIDOWED, SEPARATED, UNKNOWN)
status (enum: ACTIVE, INACTIVE, DECEASED, ARCHIVED)
created_at / updated_at / created_by / updated_by
deleted / deleted_at / deleted_by
version
active_mrn_slot (generated; soft-delete-aware uniqueness helper)
active_national_id_slot (generated; Phase 5.10 — soft-delete-aware uniqueness when national_id present)

MRN

Medical Record Number — unique per tenant among live (non-deleted) rows via
`uk_patients_tenant_active_mrn (tenant_id, mrn, active_mrn_slot)`.

National ID

Optional CNIC / passport — unique per tenant among live rows when present via
`uk_patients_tenant_active_national_id (tenant_id, national_id, active_national_id_slot)`
(Flyway `V24__patient_national_id_unique.sql`). Soft-deleted rows free the slot.

Indexes: tenant, mrn, status, name `(last_name, first_name)`, phone, email,
national_id (unique slot), date_of_birth, deleted.

Deferred (later patient profile enrichment): `occupation`, `photo_url`.

---

## Medical history (Phase 5.3 — Flyway `V19__medical_history.sql`)

Structured longitudinal chart data. Design: [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md).
No free-text history blobs; clinical notes bounded to `VARCHAR(1000)`.
Visits are out of scope (Phase 7).

### medical_histories

1:1 with `patients` per tenant.

id, tenant_id, patient_id (unique with tenant), last_reviewed_at, last_reviewed_by,
audit + soft delete + version

### past_diseases

patient_id, medical_history_id,
disease_name, disease_category (enum), disease_code (optional ICD-like),
diagnosis_date, recovery_date, severity, condition_status, clinical_notes,
recorded_by_user_id, audit + soft delete + version

### surgery_histories

patient_id, medical_history_id,
procedure_name, procedure_category (enum), procedure_code, performing_facility,
diagnosis_date (procedure date), recovery_date, severity, condition_status, clinical_notes,
recorded_by_user_id, audit + soft delete + version

### chronic_conditions

patient_id, medical_history_id,
condition_name, disease_category (enum), condition_code,
diagnosis_date, recovery_date, severity, condition_status, clinical_notes,
recorded_by_user_id, audit + soft delete + version

Indexes on tenant + patient, diagnosis_date, condition_status, medical_history_id, deleted.

---

patient_allergies (Phase 5.4 — Flyway `V20__patient_allergies.sql`)

Safety-critical structured allergies. Design: [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md).

Fields

id, tenant_id, patient_id
allergen_name, allergen_code
allergy_type (DRUG, FOOD, ENVIRONMENTAL, OTHER)
severity (MILD, MODERATE, SEVERE, LIFE_THREATENING)
reaction (ANAPHYLAXIS, RASH, …)
status (ACTIVE, INACTIVE, ENTERED_IN_ERROR)
onset_date
clinical_notes (VARCHAR 1000)
verified, patient_reported, critical_alert, show_on_banner
recorded_by_user_id
audit + soft delete + version

Indexes: tenant+patient, type, severity, status, critical_alert, show_on_banner, deleted.

---

patient_family_history

patient_id

disease

relation

notes

---

patient_chronic_diseases

patient_id

disease

status

diagnosed_date

---

patient_immunizations

patient_id

vaccine_name

vaccine_code

dose_number

manufacturer

batch_number

administration_date

next_due_date

healthcare_provider

route

status

clinical_notes

---

patient_surgeries

patient_id

procedure

hospital

doctor

date

notes

---

# 9. Visit Module

patient_visits

Stores every consultation.

Fields

patient_id

doctor_id

department_id

visit_date

chief_complaint

diagnosis

notes

follow_up_date

status

---

visit_vitals

visit_id

height

weight

temperature

blood_pressure

pulse

oxygen

blood_sugar

BMI

---

visit_diagnosis

visit_id

icd_code

disease_name

severity

notes

---

# 10. Prescription Module

prescriptions

visit_id

doctor_id

patient_id

prescription_date

status

---

prescription_items

prescription_id

medicine_id

dosage

frequency

duration

instructions

before_food

after_food

morning

afternoon

night

---

medicine_master

Master Medicine Table

medicine_name

generic_name

brand

strength

form

manufacturer

---

# 11. Laboratory Module

lab_orders

visit_id

doctor_id

patient_id

status

ordered_date

---

lab_tests

lab_order_id

test_name

category

result

unit

normal_range

remarks

---

# 12. Radiology

radiology_orders

patient_id

doctor_id

scan_type

status

---

radiology_reports

order_id

finding

impression

file_url

---

# 13. Medical Documents

documents

patient_id

document_type

file_name

file_url

uploaded_by

uploaded_at

Supported

PDF

X-Ray

MRI

CT Scan

Prescription

Insurance

Reports

---

# 14. Appointment Module

appointments

patient_id

doctor_id

department_id

appointment_date

status

reason

check_in

check_out

---

# 15. Admission Module

admissions

patient_id

ward

bed

doctor

admission_date

discharge_date

reason

status

---

# 16. Billing Module

invoices

patient_id

amount

discount

tax

payment_status

invoice_date

---

invoice_items

invoice_id

service

quantity

price

---

payments

invoice_id

payment_method

transaction_id

amount

paid_at

---

# 17. Pharmacy

medicine_inventory

medicine_id

batch

expiry

stock

purchase_price

selling_price

---

medicine_issue

patient_id

prescription_id

medicine_id

quantity

issued_by

issued_at

---

# 18. Notifications

notifications

user_id

title

message

type

is_read

created_at

---

# 19. Audit Logs

audit_logs

Stores every critical action.

Fields

user_id

entity

entity_id

action

old_value

new_value

ip_address

device

created_at

Actions

CREATE

UPDATE

DELETE

LOGIN

LOGOUT

DOWNLOAD

UPLOAD

---

# 20. Authentication

refresh_tokens

user_id

token

expires_at

revoked

---

login_history

user_id

ip

browser

device

location

login_time

---

# 21. Relationships

Tenant

↓

Users

↓

Doctor

↓

Patient Visit

↓

Prescription

↓

Medicine

Patient

↓

Appointments

↓

Visits

↓

Diagnosis

↓

Prescription

↓

Lab

↓

Reports

↓

Admission

↓

Billing

---

# 22. Soft Delete Strategy

Every business table includes

deleted BOOLEAN

deleted_at TIMESTAMP

deleted_by UUID

No permanent delete from UI.

---

# 23. Indexing Strategy

Primary Indexes

UUID

Secondary Indexes

patient_name

phone

email

doctor

appointment_date

visit_date

invoice_date

MRN

Composite Indexes

tenant_id + patient_id

tenant_id + doctor_id

tenant_id + appointment_date

---

# 24. Constraints

Email Unique

MRN Unique per Tenant

Doctor License Unique

Phone Indexed

NOT NULL for mandatory fields

Foreign Keys enforced

---

# 25. Backup Strategy

Daily Incremental Backup

Weekly Full Backup

Monthly Archive

Point-in-Time Recovery Enabled

---

# 26. Performance Considerations

Use Pagination

Avoid N+1 Queries

Lazy Loading where applicable

Batch Inserts

Proper Indexes

Connection Pooling

Redis Caching

Read Optimization

---

# 27. Future Enhancements

- FHIR Compliance
- HL7 Integration
- Insurance Module
- Telemedicine
- AI Diagnosis Support
- Voice Prescription
- OCR Medical Reports
- Wearable Device Integration
- Data Warehouse
- Elasticsearch
- Multi-Region Replication

---

End of DATABASE.md
