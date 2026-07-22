# ENGINEERING_RULES.md

# Healthcare Management System (HMS) SaaS

## Engineering Standards & Development Guidelines

**Version:** 1.0  
**Status:** Draft

---

# 1. Purpose

This document defines the engineering standards, coding conventions, architectural rules, and development practices that every contributor must follow while working on the Healthcare Management System.

These rules ensure:

- Consistent codebase
- High maintainability
- Scalability
- Security
- Readability
- Production-ready quality

These standards apply to every module, service, component, API, and database object.

---

# 2. General Engineering Principles

- Follow SOLID principles.
- Follow DRY (Don't Repeat Yourself).
- Follow KISS (Keep It Simple).
- Follow Clean Code principles.
- Follow Clean Architecture.
- Follow REST API best practices.
- Write reusable and modular code.
- Avoid premature optimization.
- Prefer composition over inheritance.
- Never hardcode configuration values.

---

# 3. Naming Conventions

## Classes

Use PascalCase.

Examples:

- PatientService
- AppointmentController
- PrescriptionRepository

---

## Interfaces

Use descriptive names.

Examples:

- PatientRepository
- UserService
- EmailSender

---

## Variables

Use camelCase.

Examples:

- patientId
- doctorName
- createdAt
- currentVisit

---

## Constants

Use UPPER_SNAKE_CASE.

Examples:

- MAX_FILE_SIZE
- JWT_EXPIRATION
- DEFAULT_PAGE_SIZE

---

## Database Tables

Use snake_case.

Examples:

- patients
- appointments
- prescriptions
- audit_logs

---

## API Endpoints

Use lowercase plural nouns.

Examples:

```
/api/patients
/api/doctors
/api/appointments
/api/prescriptions
```

---

# 4. Folder Structure Rules

## Backend

```
src/

├── auth
├── users
├── tenant          # Multi-tenant foundation (Phase 2.1)
├── hospitals       # Hospital profile / departments (Phase 3+)
├── patients
├── appointments
├── visits
├── prescriptions
├── laboratory
├── reports
├── notifications
├── common
└── config
```

Each module should contain:

```
controller
service
repository
entity
dto
mapper
validator
exception
```

---

## Frontend

```
src/

├── app
├── components
├── features
├── hooks
├── services
├── store
├── lib
├── types
├── utils
└── styles
```

Each feature should contain:

```
components
api
hooks
validation
types
pages
```

---

# 5. API Standards

- Follow REST principles.
- Use nouns instead of verbs.
- Version APIs.
- Return proper HTTP status codes.
- Validate every request.
- Never expose internal exceptions.
- Use pagination for list endpoints.
- Support filtering and searching where applicable.

Example:

```
GET    /api/patients
POST   /api/patients
GET    /api/patients/{id}
PUT    /api/patients/{id}
DELETE /api/patients/{id}
```

---

# 6. Request Validation

Every incoming request must be validated.

Validation includes:

- Required fields
- Length
- Format
- Email validation
- Phone validation
- Date validation
- Enum validation
- Custom business rules

Invalid requests must return descriptive validation errors.

---

# 7. Error Handling

Use centralized exception handling.

Error responses should contain:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": []
}
```

Never expose:

- Stack traces
- SQL errors
- Internal server details

---

# 8. Logging Standards

Log important events only.

Examples:

- User login
- Authentication failure
- Record creation
- Record update
- File upload
- Security events
- System errors

Do not log:

- Passwords
- Tokens
- Sensitive medical information

---

# 9. Authentication Rules

- Use JWT Access Tokens.
- Use Refresh Tokens.
- Hash passwords.
- Support token expiration.
- Invalidate compromised tokens.
- Protect all private endpoints.

---

# 10. Authorization Rules

Use Role-Based Access Control (RBAC).

Every protected endpoint must verify:

- Authentication
- Role
- Permission
- Tenant ownership

Never allow users to access another tenant's data.

---

# 11. Database Rules

- Use foreign keys.
- Normalize data.
- Add indexes where necessary.
- Use soft delete.
- Include audit fields.

Standard audit fields:

- created_at
- updated_at
- deleted_at
- created_by
- updated_by

---

# 12. Security Rules

Always:

- Use HTTPS.
- Validate all inputs.
- Escape outputs.
- Prevent SQL Injection.
- Prevent XSS.
- Prevent CSRF where applicable.
- Limit upload size.
- Scan uploaded files if required.
- Enforce strong password policies.

Never:

- Store plaintext passwords.
- Store secrets in source code.
- Commit environment files.
- Expose sensitive configuration.

---

# 13. File Upload Rules

Allowed formats:

- PDF
- JPG
- JPEG
- PNG

Store files in object storage.

Only store metadata in the database.

Validate:

- MIME type
- Extension
- Size

---

# 14. Code Quality Rules

Every pull request should:

- Compile successfully.
- Pass linting.
- Pass tests.
- Follow formatting rules.
- Include meaningful commit messages.
- Avoid duplicated logic.

---

# 15. Git Workflow

Use feature branches.

Branch examples:

```
feature/patient-module
feature/authentication
feature/appointment-management

bugfix/login-error

hotfix/security-patch
```

---

# 16. Commit Message Convention

Format:

```
type(scope): message
```

Examples:

```
feat(patient): add patient registration

fix(auth): resolve refresh token issue

refactor(appointment): simplify scheduling logic

docs(prd): update functional requirements
```

---

# 17. Code Review Checklist

Before merging:

- Code follows architecture.
- Naming conventions are correct.
- Validation exists.
- Security is verified.
- Error handling is complete.
- Logging is appropriate.
- Tests pass.
- Documentation is updated.

---

# 18. Testing Standards

Minimum testing includes:

- Unit Tests
- Integration Tests
- API Tests
- End-to-End Tests

Critical modules should achieve high test coverage.

---

# 19. Documentation Rules

Every module should include:

- Purpose
- Responsibilities
- Public APIs
- Dependencies
- Usage examples (where applicable)

Public APIs must be documented using Swagger/OpenAPI.

---

# 20. Performance Guidelines

- Avoid N+1 queries.
- Use pagination.
- Cache frequently accessed data.
- Optimize database indexes.
- Compress API responses when appropriate.
- Load only required data.

---

# 21. Engineering Principles

Every contribution to the project must satisfy the following principles:

- Security First
- Patient Safety First
- Clean Architecture
- Scalable Design
- Modular Development
- Maintainable Code
- Reusable Components
- Consistent Standards
- Comprehensive Documentation
- Production-Ready Quality
