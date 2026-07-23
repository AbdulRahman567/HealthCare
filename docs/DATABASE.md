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

departments

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

doctor_profile

Fields

user_id

department_id

specialization

license_number

experience

qualification

consultation_fee

availability

---

nurses

nurse_profile

department_id

shift

qualification

---

# 8. Patient Module

patients

Fields

id
tenant_id
mrn
first_name
last_name
dob
gender
blood_group
phone
email
address
emergency_contact
marital_status
occupation
photo_url

MRN

Medical Record Number

Unique per hospital.

---

patient_allergies

Fields

patient_id

allergy_name

severity

notes

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

patient_vaccinations

patient_id

vaccine

date

notes

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
