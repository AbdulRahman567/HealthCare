# API.md

# Healthcare Management System (HMS)

## REST API Documentation

Version: 1.0
Status: Draft

---

# 1. Purpose

This document defines the REST APIs for the Healthcare Management System (HMS).

Goals

- RESTful Design
- Versioned APIs
- Secure Endpoints
- Consistent Response Format
- Multi-Tenant Ready
- Production Ready
- Swagger Compatible

Base URL

/api/v1

Example

https://api.hms.com/api/v1

---

# 2. API Standards

## Request Headers

Authorization: Bearer <JWT_TOKEN>

Content-Type: application/json

Accept: application/json

X-Tenant-ID: tenant_uuid

---

## Response Format

Success

{
"success": true,
"message": "Patient created successfully",
"data": {},
"timestamp": "2026-07-21T12:30:00Z"
}

Error

{
"success": false,
"message": "Validation Failed",
"errors": [
{
"field": "email",
"message": "Email already exists"
}
],
"timestamp": "2026-07-21T12:30:00Z"
}

---

# 3. Authentication APIs

## Hospital Registration (Phase 2.5)

`POST /api/v1/hospitals/register` (public)

Atomically creates:

1. Tenant (`PENDING`)
2. Default hospital profile (`code=DEFAULT`, `is_default=true`)
3. Default tenant roles with permission grants
4. Initial hospital administrator (email verification required)

Compatibility alias: `POST /api/v1/auth/register/hospital` (same payload).

Hospital department management is Phase 3+ and not exposed here.

## Hospital Settings (Phase 2.6)

Tenant-scoped settings for the current tenant's default hospital. Requires
`Authorization: Bearer <token>` and `X-Tenant-ID`. Client-supplied hospital ids
are not accepted — isolation comes from `TenantContextHolder` + Hibernate
`tenantFilter`.

`GET /api/v1/hospitals/settings` — permission `HOSPITAL_READ`

`PUT /api/v1/hospitals/settings` — permission `HOSPITAL_UPDATE`

Supports:

- Hospital profile (name, description, code/status read-only)
- Logo URL
- Timezone (IANA)
- Currency (ISO 4217)
- Language (BCP 47)
- Contact information (email, phone, secondary phone, website)
- Address (line, city, state/province, country, postal code)
- Working hours (per weekday open/close `HH:mm`, or closed)

Updates are audited (`AuditAction.UPDATE` on entity `HOSPITAL`).

## Tenant Security Notes (Phase 2.7)

- Public auth paths ignore `X-Tenant-ID` (no unauthenticated tenant enumeration).
- Conflicting duplicate `X-Tenant-ID` values are rejected.
- JWT principal tenant identity, roles, and permissions are loaded from the database; claim/DB drift fails closed.
- Platform tenant bypass (and header binding with null principal tenant) requires `SUPER_ADMIN`.
- `POST /api/v1/auth/register/admin` returns **410 Gone** (legacy onboarding disabled).

---

# 3.1 Session & Profile APIs

Public (anonymous):

- `POST /auth/login`
- `POST /auth/refresh-token`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`
- `POST /auth/verify-email`
- `POST /auth/resend-verification`

Authenticated (JWT + tenant; self-service `@RequireAuthenticated`):

- `POST /auth/logout`
- `POST /auth/change-password`
- `GET /auth/profile`
- `PUT /auth/profile`
- `GET /auth/authorization`

Permission-gated:

- `GET /auth/authorization/hospital-access` — `HOSPITAL_READ`

Unauthorized → **401**. Missing permission / tenant mismatch → **403** (generic body).

---

# 4. User Management

Base path: `/api/v1`

## 4.1 User invitations (Phase 4.5)

Requires JWT + `X-Tenant-ID` for admin operations. Accept/reject/token preview are public.

| Method | Path | Permission | Notes |
| ------ | ---- | ---------- | ----- |
| POST | `/invitations` | USER_CREATE | Invite by email + role; emails hashed single-use token |
| GET | `/invitations` | USER_READ | Filter `status`, `email`; pageable |
| GET | `/invitations/{id}` | USER_READ | Tenant-scoped get |
| POST | `/invitations/{id}/resend` | USER_CREATE | New token + extended expiry |
| POST | `/invitations/{id}/cancel` | USER_UPDATE | Cancel pending invite |
| POST | `/invitations/preview` | Public | Preview pending invitation (token in body) |
| POST | `/invitations/accept` | Public | Create account, assign role, join hospital tenant |
| POST | `/invitations/reject` | Public | Reject pending invitation |

Workflow: Hospital Admin invites → email with `#token=` fragment link →
`/accept-invitation` (public) → preview (token in body) → accept (create user + role)
or reject → optional resend/cancel while pending.

Token TTL: `hms.security.user-invitation.token-expiration` (default 72h).
Expired pending invitations are persisted as `EXPIRED` so the email may be re-invited.

## 4.2 Users (Phase 4.6)

Requires JWT + `X-Tenant-ID` + `USER_*` permissions.

No physical delete — lifecycle uses status changes only.

| Method | Path | Permission | Notes |
| ------ | ---- | ---------- | ----- |
| GET | `/users` | USER_READ | Search `q`; filter `status` / `roleType` / `emailVerified`; pageable sort |
| GET | `/users/{id}` | USER_READ | Tenant-scoped get with role types |
| PUT | `/users/{id}` | USER_UPDATE | Admin profile update (name, phone) |
| POST | `/users/{id}/activate` | USER_UPDATE | PENDING or INACTIVE → ACTIVE |
| POST | `/users/{id}/deactivate` | USER_UPDATE | ACTIVE → INACTIVE; revokes sessions; clears dept heads; suspends staff employment |
| POST | `/users/{id}/suspend` | USER_UPDATE | ACTIVE → SUSPENDED; revokes sessions; clears dept heads; suspends staff employment |
| POST | `/users/{id}/restore` | USER_UPDATE | INACTIVE / SUSPENDED → ACTIVE (LOCKED excluded) |

Status transitions forbid self-target for activate/deactivate/suspend/restore.

User creation remains via invitations (Phase 4.5) or hospital registration.

---

# 5. Roles

GET

/roles

POST

/roles

PUT

/roles/{id}

DELETE

/roles/{id}

GET

/roles/{id}/permissions

PUT

/roles/{id}/permissions

---

# 6. Departments

Base path: `/api/v1/departments`

Requires JWT + `X-Tenant-ID` + department permissions.

| Method | Path | Permission | Notes |
| ------ | ---- | ---------- | ----- |
| GET | `/departments` | DEPARTMENT_READ | Search `q`, filter `status`/`type`/`hospitalId`, pageable sort |
| GET | `/departments/{id}` | DEPARTMENT_READ | Tenant-scoped get |
| POST | `/departments` | DEPARTMENT_CREATE | Bound to default hospital; unique code/name per tenant |
| PUT | `/departments/{id}` | DEPARTMENT_UPDATE | Full replace; unique code/name |
| DELETE | `/departments/{id}` | DEPARTMENT_DELETE | Soft delete (204); frees code/name uniqueness |

---

# 7. Doctors / Staff

Staff administration (Phase 4.3). No scheduling/appointment endpoints here.

| Method | Path | Permission |
| ------ | ---- | ---------- |
| CRUD | `/api/v1/doctors` | `DOCTOR_*` |
| CRUD | `/api/v1/nurses` | `STAFF_*` |
| CRUD | `/api/v1/receptionists` | `STAFF_*` |
| CRUD | `/api/v1/laboratory-staff` | `STAFF_*` |
| CRUD | `/api/v1/pharmacists` | `STAFF_*` |

List endpoints support `q`, `employmentStatus`, `departmentId`, and pageable sort.
Create requires existing tenant `userId` with matching role + `departmentId`.
Soft delete returns `204`.

---

# 7.1 Staff assignments (Phase 4.4)

Base path: `/api/v1`

Requires JWT + `X-Tenant-ID`.

| Method | Path | Permission | Notes |
| ------ | ---- | ---------- | ----- |
| POST | `/staff-assignments` | STAFF_UPDATE | Assign staff with no current department; rejects duplicates |
| POST | `/staff-assignments/transfers` | STAFF_UPDATE | Transfer between departments; closes open history; clears head if leaving |
| GET | `/staff-assignments/current` | STAFF_READ | Open assignment for `staffType` + `staffId` |
| GET | `/staff-assignments` | STAFF_READ | History by `staffType`+`staffId` or `departmentId` |
| PUT | `/departments/{departmentId}/head` | DEPARTMENT_UPDATE | Staff must currently belong to the department |
| DELETE | `/departments/{departmentId}/head` | DEPARTMENT_UPDATE | Clear head (staff + user refs) |

Assignment history is also written when staff profiles are created/updated/soft-deleted.

---

# 8. Patients

GET

/patients

GET

/patients/{id}

POST

/patients

PUT

/patients/{id}

DELETE

/patients/{id}

GET

/patients/search

GET

/patients/{id}/timeline

GET

/patients/{id}/history

GET

/patients/{id}/dashboard

---

# 9. Patient Allergies

GET

/patients/{id}/allergies

POST

/patients/{id}/allergies

PUT

/patients/{id}/allergies/{allergyId}

DELETE

/patients/{id}/allergies/{allergyId}

---

# 10. Patient Diseases

GET

/patients/{id}/diseases

POST

/patients/{id}/diseases

PUT

/patients/{id}/diseases/{diseaseId}

DELETE

/patients/{id}/diseases/{diseaseId}

---

# 11. Patient Visits

GET

/visits

GET

/visits/{id}

POST

/visits

PUT

/visits/{id}

DELETE

/visits/{id}

GET

/patients/{id}/visits

GET

/doctors/{id}/visits

---

# 12. Vital Signs

GET

/visits/{id}/vitals

POST

/visits/{id}/vitals

PUT

/visits/{id}/vitals

---

# 13. Diagnosis

GET

/diagnosis

POST

/diagnosis

PUT

/diagnosis/{id}

DELETE

/diagnosis/{id}

GET

/patients/{id}/diagnosis

---

# 14. Prescriptions

GET

/prescriptions

GET

/prescriptions/{id}

POST

/prescriptions

PUT

/prescriptions/{id}

DELETE

/prescriptions/{id}

GET

/patients/{id}/prescriptions

POST

/prescriptions/{id}/print

POST

/prescriptions/{id}/email

---

# 15. Medicines

GET

/medicines

GET

/medicines/search

POST

/medicines

PUT

/medicines/{id}

DELETE

/medicines/{id}

---

# 16. Laboratory

GET

/lab/orders

POST

/lab/orders

GET

/lab/orders/{id}

PUT

/lab/orders/{id}

DELETE

/lab/orders/{id}

GET

/lab/tests

POST

/lab/tests

PUT

/lab/tests/{id}

---

# 17. Radiology

GET

/radiology

POST

/radiology

PUT

/radiology/{id}

DELETE

/radiology/{id}

POST

/radiology/upload

---

# 18. Documents

GET

/documents

POST

/documents/upload

DELETE

/documents/{id}

GET

/patients/{id}/documents

---

# 19. Appointments

GET

/appointments

GET

/appointments/{id}

POST

/appointments

PUT

/appointments/{id}

DELETE

/appointments/{id}

PATCH

/appointments/{id}/cancel

PATCH

/appointments/{id}/complete

---

# 20. Admissions

GET

/admissions

POST

/admissions

PUT

/admissions/{id}

PATCH

/admissions/{id}/discharge

GET

/patients/{id}/admissions

---

# 21. Billing

GET

/invoices

GET

/invoices/{id}

POST

/invoices

PUT

/invoices/{id}

DELETE

/invoices/{id}

POST

/invoices/{id}/pay

GET

/payments

---

# 22. Pharmacy

GET

/pharmacy/inventory

POST

/pharmacy/inventory

PUT

/pharmacy/inventory/{id}

DELETE

/pharmacy/inventory/{id}

POST

/pharmacy/issue

GET

/pharmacy/history

---

# 23. Notifications

GET

/notifications

PATCH

/notifications/{id}/read

PATCH

/notifications/read-all

DELETE

/notifications/{id}

---

# 24. Audit Logs

GET

/audit-logs

GET

/audit-logs/{id}

GET

/audit-logs/user/{userId}

GET

/audit-logs/entity/{entity}

---

# 25. Dashboard APIs

GET

/dashboard/admin

GET

/dashboard/doctor

GET

/dashboard/receptionist

GET

/dashboard/pharmacy

GET

/dashboard/laboratory

GET

/dashboard/patient

---

# 26. Reports

GET

/reports/patients

GET

/reports/appointments

GET

/reports/revenue

GET

/reports/prescriptions

GET

/reports/laboratory

GET

/reports/doctors

---

# 27. Pagination

Request

GET /patients?page=1&size=20

Response

{
"page": 1,
"size": 20,
"totalPages": 25,
"totalItems": 500,
"data": []
}

---

# 28. Filtering

Examples

GET /patients?gender=Male

GET /appointments?status=Scheduled

GET /visits?doctorId=UUID

GET /patients?bloodGroup=O+

---

# 29. Searching

Examples

GET /patients/search?q=Ali

GET /doctors/search?q=Cardiology

GET /medicines/search?q=Paracetamol

---

# 30. Sorting

Examples

?sort=createdAt

?sort=lastName

?direction=asc

?direction=desc

---

# 31. HTTP Status Codes

200 OK

201 Created

204 No Content

400 Bad Request

401 Unauthorized

403 Forbidden

404 Not Found

409 Conflict

422 Validation Error

500 Internal Server Error

---

# 32. API Versioning

/api/v1

Future

/api/v2

Backward compatibility must be maintained.

---

# 33. Rate Limiting

Authentication APIs

5 requests/minute

General APIs

100 requests/minute

File Upload APIs

20 requests/minute

Report APIs

30 requests/minute

---

# 34. File Upload Rules

Allowed Types

PDF

PNG

JPEG

DOCX

Maximum Size

20 MB

Virus scan required before storage.

---

# 35. Security

JWT Authentication

Refresh Tokens

RBAC Authorization

HTTPS Only

Input Validation

Output Encoding

Tenant Isolation

Audit Logging

CSRF Protection (if applicable)

Rate Limiting

---

# 36. Swagger

All APIs must be documented using

OpenAPI 3.0

Swagger UI available at

/swagger-ui

---

# 37. Future APIs

FHIR Integration

HL7 Integration

Insurance Claims

Telemedicine

AI Diagnosis Support

Wearable Device Sync

SMS Gateway

WhatsApp Notifications

Third-Party Pharmacy Integration

---

End of API.md
