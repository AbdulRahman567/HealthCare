# PROJECT_CONTEXT.md

# Healthcare Management System (HMS) SaaS

## Project Context for AI Assistants & Developers

**Version:** 1.0  
**Status:** Active

---

# 1. Project Overview

Healthcare Management System (HMS) is a production-ready, enterprise-grade, multi-tenant SaaS platform designed to manage hospitals, clinics, doctors, patients, appointments, medical records, prescriptions, laboratory reports, and healthcare workflows.

The project is intended to demonstrate modern software architecture, clean engineering practices, security, scalability, and real-world healthcare workflows.

This document provides long-term context for developers and AI coding assistants to ensure consistency across the entire codebase.

---

# 2. Project Vision

Build a secure, scalable, maintainable, and modern Healthcare Management System that enables hospitals to manage their operations through a centralized platform while giving doctors complete access to a patient's medical history for better clinical decision-making.

---

# 3. Primary Objectives

- Build an enterprise-grade SaaS platform.
- Support multiple hospitals using a single application.
- Maintain complete patient medical history.
- Improve healthcare workflows.
- Ensure data security and tenant isolation.
- Follow modern software engineering practices.

---

# 4. Technology Stack

## Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS
- shadcn/ui
- Redux Toolkit
- TanStack Query
- React Hook Form
- Zod
- Axios

---

## Backend

- Spring Boot
- Java
- Spring Security
- Spring Data JPA
- Hibernate
- Maven

---

## Database

- MySQL

---

## Cache

- Redis

---

## File Storage

- AWS S3
- MinIO (Local Development)

---

## API Documentation

- Swagger / OpenAPI

---

## Containerization

- Docker
- Docker Compose

---

## Deployment

- Frontend → Vercel
- Backend → AWS EC2
- Database → Managed MySQL
- Storage → AWS S3

---

# 5. Core Modules

- Authentication
- Authorization (RBAC)
- Multi-Tenant Management
- Hospital Management
- Department Management
- Staff Management
- Doctor Management
- Patient Management
- Appointment Management
- Visit Management
- Medical Records
- Prescription Management
- Laboratory Reports
- Imaging Reports
- Notifications
- Dashboard & Analytics
- Audit Logs

---

# 6. User Roles

- Super Admin
- Hospital Admin
- Doctor
- Nurse
- Receptionist
- Laboratory Technician
- Pharmacist

Future:

- Patient
- Insurance Provider

---

# 7. Architecture Principles

The project follows:

- Clean Architecture
- Modular Architecture
- Layered Architecture
- RESTful API Design
- SOLID Principles
- DRY
- KISS
- Separation of Concerns
- API First Development

---

# 8. Coding Standards

Developers and AI assistants must:

- Write clean, readable code.
- Keep modules independent.
- Follow naming conventions.
- Avoid duplicated logic.
- Create reusable components.
- Use dependency injection.
- Follow project folder structure.
- Keep business logic inside service layer.
- Keep controllers lightweight.

---

# 9. Security Principles

Always enforce:

- JWT Authentication
- Refresh Tokens
- Role-Based Access Control (RBAC)
- HTTPS
- Password Hashing
- Input Validation
- Secure File Uploads
- Audit Logging
- Tenant Isolation (Shared Schema + `tenant_id`; see MULTI_TENANCY.md)

Never expose:

- Passwords
- Secrets
- Internal errors
- Sensitive medical information

---

# 9.1 Architectural Decisions (active)

| Decision                            | Choice                                                                                   | Why                                                                                                        |
| ----------------------------------- | ---------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Multi-tenancy                       | Shared DB + Shared Schema + Tenant ID                                                    | Operational simplicity, matches DATABASE.md, scales horizontally; physical isolation deferred              |
| Tenant module                       | `com.healthcare.hms.tenant`                                                              | Cross-cutting foundation separate from Phase 3 hospital business APIs                                      |
| Tenant resolution                   | `X-Tenant-ID` via `HeaderTenantResolver`                                                 | Phase 2.2; subdomain resolver wired but disabled                                                           |
| Tenant middleware                   | `TenantFilter` after JWT (`OncePerRequestFilter`)                                        | Phase 2.3; public bypass + always clear context                                                            |
| Tenant persistence                  | `TenantOwnedEntity` + Hibernate `tenantFilter`                                           | Phase 2.4; enabled on JPA TX begin from `TenantContextHolder`                                              |
| Hospital registration               | Atomic `POST /api/v1/hospitals/register`                                                 | Phase 2.5; tenant + default hospital + admin + roles                                                       |
| Hospital settings                   | `GET/PUT /api/v1/hospitals/settings`                                                     | Phase 2.6; tenant-isolated profile/locale/contact/hours                                                    |
| Tenant security audit               | Hardened resolution, JWT, public bypass, legacy admin disabled                           | Phase 2.7                                                                                                  |
| Tenant production review            | Auth-first tenant filter, OpenAPI tenant header, FE/BE registration contract             | Phase 2.8                                                                                                  |
| RBAC domain                         | PermissionGroup/Action, naming `{GROUP}_{ACTION}`, role hierarchy, V9 migration          | Phase 3.1                                                                                                  |
| Authorization infra                 | PermissionEvaluator, AuthorizationService, CurrentUser, PermissionResolver               | Phase 3.2                                                                                                  |
| Permission-based authZ              | `@RequirePermission`, PermissionGuard, aspect + interceptor, AccessDenied JSON           | Phase 3.3                                                                                                  |
| Secure existing APIs                | Public vs protected annotations, JWT/role/permission/tenant, Swagger auth docs           | Phase 3.4                                                                                                  |
| Default system roles                | CREATE/UPDATE naming, BILLING, Accountant, matrix, Flyway V10 + bootstrap                | Phase 3.5                                                                                                  |
| Frontend authorization              | Permission/Role providers, hooks, Can/Protected, route UX guards (backend = SoT)         | Phase 3.6                                                                                                  |
| Dynamic navigation                  | Permission-aware shell: sidebar, top nav, breadcrumbs, cards, quick actions              | Phase 3.7                                                                                                  |
| RBAC review / hardening             | Fail-closed APIs & routes, platform trust bar, coverage guard, review reports            | Phase 3.8                                                                                                  |
| Hospital organization               | Top-level `organization` module; `Staff` MappedSuperclass; employment/department enums   | Phase 4.1; see [HOSPITAL_ORGANIZATION.md](./HOSPITAL_ORGANIZATION.md)                                      |
| Department management               | `Department` CRUD, unique code/name per tenant, search/page/sort/filter, Flyway V12      | Phase 4.2                                                                                                  |
| Staff management                    | Doctor/Nurse/Receptionist/Lab/Pharmacist profiles; User+Department; STAFF_* RBAC; V13    | Phase 4.3                                                                                                  |
| Staff assignment                    | Assign/transfer, department head as Staff FK, assignment history, V14                    | Phase 4.4                                                                                                  |
| User invitation                     | Invite-by-email, hashed token, accept/reject/resend/cancel; Flyway V15                   | Phase 4.5                                                                                                  |
| User management                     | Directory search/filter/page; profile update; activate/deactivate/suspend/restore        | Phase 4.6                                                                                                  |
| Hospital Administration UI          | FE dashboard, departments, staff directory, users, invitations; permission-gated         | Phase 4.7                                                                                                  |
| Hospital Admin security review      | Tenant isolation, RBAC, invite/user privilege hardening; no Critical remaining           | Phase 4.8                                                                                                  |
| Hospital Admin production readiness | Lifecycle verification, accept-invitation UI, soft-delete uniqueness, referential guards | Phase 4.9                                                                                                  |
| Patient domain                      | `patients` module; `Patient` + enums; soft-delete-aware unique MRN; Flyway V18           | Phase 5.1; see [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md)                                            |
| Patient registration                | Register/update/deactivate/reactivate APIs; MRN uniqueness; Swagger; no physical delete  | Phase 5.2; see [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md)                                            |
| Patient medical history             | Structured PastDisease/Surgery/ChronicCondition; MedicalHistory root; Flyway V19         | Phase 5.3; see [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md)                                            |
| Patient allergies                   | Drug/food/environmental; severity/reaction; banner + critical alerts; Flyway V20         | Phase 5.4; see [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md)                                            |
| Patient immunizations               | Vaccine/dose/manufacturer/batch/dates/provider; due API; Flyway V21                      | Phase 5.5; see [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md)                                            |
| Patient timeline                    | Provider SPI + cursor merge; V22 allergy date indexes; future VISIT/Rx/lab/billing hooks | Phase 5.6; see [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md)                                            |
| Patient search                      | Specifications + page/sort; age→DOB; V23 dept/doctor columns + indexes                   | Phase 5.7; see [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md)                                            |
| Patient Management UI               | Next.js list/register/edit/chart; allergy banner; history/allergies/vaccines/timeline    | Phase 5.8; see [PATIENT_MANAGEMENT.md](./PATIENT_MANAGEMENT.md)                                            |
| Patient security review             | Tenant/RBAC/PHI/audit hardening; soft-delete + critical allergy guards; no Critical left | Phase 5.9; see [PATIENT_PHASE_5_9_SECURITY_REVIEW.md](./PATIENT_PHASE_5_9_SECURITY_REVIEW.md)              |
| Patient production readiness        | Lifecycle verification, national-ID unique, allergy flag patch semantics, FE chart UX    | Phase 5.10; see [PATIENT_PHASE_5_10_PRODUCTION_READINESS.md](./PATIENT_PHASE_5_10_PRODUCTION_READINESS.md) |
| Appointment security review         | Tenant/RBAC/audit/doctor-scope/patient-portal hardening; no Critical remaining           | Phase 6.9; see [APPOINTMENT_PHASE_6_9_SECURITY_REVIEW.md](./APPOINTMENT_PHASE_6_9_SECURITY_REVIEW.md)     |
| Appointment production readiness    | Lifecycle/queue integrity, booking locks, availability UI, Flyway V32; no Critical/High  | Phase 6.10; see [APPOINTMENT_PHASE_6_10_PRODUCTION_READINESS.md](./APPOINTMENT_PHASE_6_10_PRODUCTION_READINESS.md) |

---

# 10. Database Principles

- Normalize data.
- Use foreign keys.
- Apply soft delete.
- Include audit fields.
- Optimize indexes.
- Maintain referential integrity.

---

# 11. API Design Rules

- Version APIs.
- Use REST conventions.
- Return consistent response formats.
- Validate all requests.
- Handle errors centrally.
- Support pagination, filtering, and searching.

---

# 12. Frontend Principles

- Feature-based architecture.
- Reusable UI components.
- Responsive design.
- Accessibility support.
- Consistent design system.
- Client-side state with Redux Toolkit.
- Server state with TanStack Query.

---

# 13. Backend Principles

Each module should contain:

- Controller
- Service
- Repository
- Entity
- DTO
- Mapper
- Validator
- Exception

Business logic belongs in the service layer.

---

# 14. Project Structure

```
frontend/
backend/
docs/

frontend/
├── src
├── components
├── features
├── hooks
├── services
├── store
└── utils

backend/
├── auth
├── users
├── tenant
├── hospitals
├── organization   # Hospital Administration foundation (Phase 4.1)
├── patients
├── appointments
├── visits
├── prescriptions
├── reports
├── notifications
└── common
```

---

# 15. Development Workflow

1. Plan feature.
2. Design database.
3. Create API.
4. Implement backend.
5. Implement frontend.
6. Test feature.
7. Review code.
8. Update documentation.
9. Merge to main branch.
10. Deploy.

---

# 16. Documentation Files

The project documentation includes:

- PRD.md
- ARCHITECTURE.md
- ENGINEERING_RULES.md
- ROADMAP.md
- PROJECT_CONTEXT.md
- DESIGN_SYSTEM.md
- DATABASE.md
- API.md
- SECURITY.md
- TESTING.md
- DEPLOYMENT.md
- MULTI_TENANCY.md
- HOSPITAL_ORGANIZATION.md
- HOSPITAL_ADMIN_PHASE_4_9_REVIEW.md
- PATIENT_MANAGEMENT.md
- PATIENT_PHASE_5_9_SECURITY_REVIEW.md
- PATIENT_PHASE_5_10_PRODUCTION_READINESS.md
- APPOINTMENT_PHASE_6_9_SECURITY_REVIEW.md
- APPOINTMENT_PHASE_6_10_PRODUCTION_READINESS.md
- phasesreadme.md

---

# 17. Long-Term Goals

Future expansion includes:

- Billing & Payments
- Pharmacy Management
- Inventory Management
- Insurance Claims
- Telemedicine
- Patient Portal
- Mobile Applications
- AI Clinical Decision Support
- Wearable Device Integration
- Government Health System Integration

---

# 18. AI Assistant Guidelines

AI assistants working on this project must:

- Follow the documented architecture.
- Respect module boundaries.
- Follow naming conventions.
- Generate production-ready code.
- Prioritize security.
- Maintain consistency across all modules.
- Avoid introducing unnecessary dependencies.
- Keep implementations scalable and maintainable.
- Update documentation when new modules or architectural changes are introduced.

---

# 19. Definition of Success

The project is considered successful when it:

- Supports multiple hospitals securely.
- Maintains complete patient medical records.
- Provides a clean and intuitive user experience.
- Demonstrates enterprise-level architecture.
- Is production-ready and scalable.
- Follows modern engineering standards.
- Serves as a real-world Healthcare Management System suitable for further expansion.
