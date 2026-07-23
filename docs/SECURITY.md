# SECURITY.md

# Healthcare Management System (HMS)

## Security Architecture & Compliance Guide

Version: 1.0
Status: Draft

---

# 1. Purpose

This document defines the security architecture, policies, standards, and best practices for the Healthcare Management System (HMS).

Goals

- Protect patient medical records
- Ensure tenant isolation
- Prevent unauthorized access
- Meet healthcare security standards
- Reduce attack surface
- Maintain auditability
- Support secure enterprise deployments

---

# 2. Security Principles

The system follows these principles:

- Zero Trust Architecture
- Least Privilege Access
- Defense in Depth
- Secure by Default
- Privacy by Design
- Principle of Separation of Duties
- Fail Securely

---

# 3. Authentication

Supported Authentication Methods

- Email + Password
- JWT Authentication
- Refresh Tokens
- Secure Session Management

Future

- MFA (Multi-Factor Authentication)
- Google Login
- Microsoft Login
- Hospital SSO
- Biometric Authentication

---

# 4. Password Policy

Minimum Length

12 Characters

Must Contain

- Uppercase
- Lowercase
- Number
- Special Character

Forbidden

- Username
- Email
- Dictionary Words
- Previous Passwords

Passwords must be hashed using

BCrypt

---

# 5. JWT Security

Access Token

Lifetime

15 Minutes

Refresh Token

Lifetime

7 Days

Stored

HTTP Only Secure Cookies

JWT Includes

User ID

Role

Tenant ID

Issued Time

Expiration

Token Version

---

# 6. Session Management

Automatic Logout

30 Minutes of inactivity

Maximum Concurrent Sessions

5

Refresh Token Rotation

Enabled

Logout

Invalidate Refresh Token

---

# 7. Authorization (RBAC)

Roles

Super Admin

Hospital Admin

Doctor

Receptionist

Nurse

Lab Technician

Pharmacist

Accountant

Patient

### Permission model (Phase 3.1 / 3.5)

- Canonical code format: `{PermissionGroup}_{PermissionAction}`
  (e.g. `PATIENT_READ`, `PATIENT_CREATE`, `PATIENT_UPDATE`, `PATIENT_DELETE`).
- Actions: `READ`, `CREATE`, `UPDATE`, `DELETE` (legacy `WRITE` renamed to `UPDATE` in Phase 3.5).
- Permissions are a **platform-global catalog**; tenant isolation is via tenant-scoped roles
  and `role_permissions` grants.
- Default grants for system roles are defined in `SystemRolePermissionMatrix`
  (see [PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md)).
- Role **hierarchy** (parent + `hierarchy_level`) is structural for ordering/assignment
  eligibility. Runtime access remains the **explicit** permission set on the role
  (no silent inheritance).

### Authorization infrastructure (Phase 3.2)

- Centralized evaluation via `PermissionEvaluator` + enforcement via `AuthorizationService`.
- `CurrentUser` abstraction (immutable principal snapshot); `PermissionResolver` derives codes.
- AuthZ failures use `AuthorizationException` hierarchy (distinct from authN) → HTTP 403.
- Default deny; method security `hasPermission` SpEL wired through Spring `PermissionEvaluator`.

### Permission-based authorization (Phase 3.3)

- Declarative `@RequirePermission` on controllers (interceptor) and services (aspect).
- Controllers must not contain imperative authorization logic — annotations + infrastructure only.
- `PermissionGuard` for rare programmatic checks; unified AccessDenied JSON (`AUTHZ_ACCESS_DENIED`).

### RBAC hardening (Phase 3.8)

- `/api/**` handlers must be classified (`@PublicEndpoint`, `@RequireAuthenticated`,
  `@RequirePermission`, or `@RequiresRole`); unclassified handlers are denied (fail-closed).
- Startup coverage guard fails boot if any `/api` REST handler is unclassified.
- Platform Super Admin tenant bypass is centralized in `PlatformPrincipalSupport`
  (`tenantId == null` **and** `SUPER_ADMIN`).
- Temporarily disabling Hibernate `tenantFilter` requires platform Super Admin.
- Frontend unknown `/app/*` routes fail closed; route catalog is independent of navigation UI.
- Full findings and recommendations: [RBAC_REVIEW_PHASE_3_8.md](./RBAC_REVIEW_PHASE_3_8.md).

Every request must verify

Authentication

↓

Tenant

↓

Role

↓

Permission

↓

Resource Ownership

---

# 8. Tenant Isolation

Every query must include

tenant_id

Example

WHERE tenant_id = ?

Never allow cross-tenant access.

Enforcement (Phase 2.4): Hibernate `tenantFilter` on `TenantOwnedEntity` subclasses,
enabled on JPA transaction begin from `TenantContextHolder`. See MULTI_TENANCY.md §6.1.

Native SQL against tenant-owned tables is forbidden unless it includes an explicit
`tenant_id` predicate and is security-reviewed.

All uploaded files

All reports

All prescriptions

All appointments

must remain isolated.

---

# 9. Data Encryption

Encryption In Transit

HTTPS

TLS 1.3

Encryption At Rest

AES-256

Encrypted Data

Medical Records

Passwords

Refresh Tokens

Sensitive Documents

Backup Archives

---

# 10. Secrets Management

Never store secrets inside

Source Code

Git Repository

Docker Images

Secrets stored using

Environment Variables

Secret Manager

AWS Secrets Manager (Production)

---

# 11. HTTPS Policy

Production

HTTPS Only

HTTP Redirect

Enabled

Secure Headers

Enabled

HSTS Enabled

TLS 1.3 Preferred

---

# 12. File Upload Security

Allowed

PDF

PNG

JPEG

DOCX

Maximum

20 MB

Every uploaded file must

Validate MIME Type

Validate Extension

Virus Scan

Rename File

Store Outside Public Directory

Generate Random Filename

---

# 13. Input Validation

Validate

Body

Query

Headers

Path Variables

Use DTO Validation

Reject

Invalid Types

Unknown Fields

Empty Required Fields

Malformed JSON

---

# 14. SQL Injection Protection

Never use

Raw SQL

Always use

Hibernate

Prepared Statements

Parameterized Queries

---

# 15. XSS Protection

Escape HTML Output

Sanitize Rich Text

Enable Content Security Policy

Avoid Unsafe HTML Rendering

---

# 16. CSRF Protection

For Cookie Authentication

Enable CSRF Protection

For JWT Authorization Header

CSRF Not Required

---

# 17. CORS Policy

Allow

Trusted Frontend Domains

Block

Unknown Origins

Allowed Methods

GET

POST

PUT

PATCH

DELETE

OPTIONS

---

# 18. Rate Limiting

Authentication

5 Requests / Minute

General APIs

100 Requests / Minute

File Upload

20 Requests / Minute

Report Generation

30 Requests / Minute

---

# 19. Brute Force Protection

Failed Login Attempts

5

Temporary Lock

15 Minutes

Permanent Lock

Requires Administrator Reset

---

# 20. Audit Logging

Log Every

Login

Logout

Patient View

Patient Update

Prescription Creation

Prescription Modification

Document Upload

Role Changes

Permission Changes

User Creation

Password Reset

Audit Log Includes

User

IP Address

Browser

Timestamp

Action

Previous Value

New Value

---

# 21. Data Privacy

Protected Data

Patient Name

Medical History

Phone Number

Email

Address

Lab Reports

Radiology Images

Diagnosis

Prescription

Only authorized users may access.

---

# 22. Medical Record Access

Doctor

Own Patients

Hospital Admin

Hospital Data

Receptionist

Limited Access

Patient

Own Records Only

Super Admin

System Management Only

---

# 23. Backup Security

Daily Backup

Encrypted

Weekly Backup

Encrypted

Monthly Archive

Encrypted

Backup Storage

Offsite

Restore Testing

Monthly

---

# 24. Logging

Never Log

Passwords

JWT Tokens

Refresh Tokens

Medical Reports

Personal Secrets

Log

Errors

Warnings

API Calls

Audit Events

Performance Metrics

---

# 25. Error Handling

Never expose

Stack Trace

Database Structure

Server Paths

Framework Information

Internal Exceptions

Client receives

Standard Error Response

---

# 26. OWASP Top 10 Protection

Protect Against

Broken Access Control

Cryptographic Failures

Injection

Insecure Design

Security Misconfiguration

Vulnerable Components

Authentication Failures

Software Integrity Failures

Logging Failures

SSRF

---

# 27. Dependency Security

Run

Dependency Scanning

SAST

DAST

Container Scanning

Regular Updates

Remove Unused Packages

---

# 28. Infrastructure Security

Docker Containers

Minimal Base Images

Non-Root User

Firewall Enabled

Reverse Proxy

Nginx

Automatic SSL Renewal

Security Updates

---

# 29. API Security

Require JWT

Validate Tenant

Validate Permissions

Validate Input

Rate Limit

Audit Log

HTTPS Only

---

# 30. Database Security

Least Privilege Database User

Separate Read/Write Accounts

Encrypted Connections

Foreign Key Constraints

Indexes

Backups

No Public Database Access

---

# 31. Monitoring

Monitor

Failed Logins

API Errors

Database Errors

CPU Usage

Memory Usage

Suspicious Requests

File Upload Activity

---

# 32. Incident Response

Detect

↓

Contain

↓

Investigate

↓

Recover

↓

Review

Maintain incident reports for every security event.

---

# 33. Compliance

Designed to support

HIPAA Principles

GDPR Principles

ISO 27001 Practices

OWASP ASVS

Healthcare Data Protection Standards

Compliance implementation depends on deployment jurisdiction.

---

# 34. Security Checklist

✓ HTTPS Enabled

✓ JWT Authentication

✓ Refresh Tokens

✓ RBAC

✓ Tenant Isolation

✓ Password Hashing

✓ DTO Validation

✓ Audit Logging

✓ File Validation

✓ Rate Limiting

✓ Secure Headers

✓ Encrypted Backups

✓ Secret Management

✓ Dependency Scanning

✓ Monitoring

---

# 35. Future Enhancements

- Multi-Factor Authentication
- Hardware Security Keys
- AI Threat Detection
- Device Trust Verification
- Risk-Based Authentication
- Security Information & Event Management (SIEM)
- Data Loss Prevention (DLP)
- End-to-End Encryption for Sensitive Documents

---

End of SECURITY.md
