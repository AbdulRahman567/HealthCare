# ARCHITECTURE.md

# Healthcare Management System (HMS) SaaS

## Software Architecture Document

**Version:** 1.0  
**Status:** Draft

---

# 1. Overview

This document defines the technical architecture of the Healthcare Management System (HMS). The system is designed as a modern, cloud-native, enterprise-grade, multi-tenant SaaS application following clean architecture principles, domain-driven design concepts, and industry best practices.

The architecture prioritizes:

- Scalability
- Security
- Maintainability
- Performance
- Multi-tenancy
- Fault Tolerance
- Extensibility

---

# 2. High-Level Architecture

```
                    Internet
                        │
                Load Balancer / Nginx
                        │
      ┌─────────────────┴─────────────────┐
      │                                   │
 Next.js Frontend                  Spring Boot API
      │                                   │
      └──────────────REST API─────────────┘
                        │
                Authentication Layer
                        │
                 Business Services
                        │
                Repository Layer
                        │
               MySQL Database Cluster
                        │
     ┌──────────────┬──────────────┐
     │              │              │
   Redis         Object Storage   Monitoring
                (AWS S3 / MinIO)
```

---

# 3. System Components

## Frontend

Technology:

- Next.js
- React
- TypeScript
- Tailwind CSS
- shadcn/ui
- Redux Toolkit
- TanStack Query

Responsibilities:

- Authentication
- Dashboard
- Patient Management
- Appointment Management
- Doctor Portal
- Admin Portal
- Reports
- Notifications

---

## Backend

Technology:

- Spring Boot
- Java
- Spring Security
- Spring Data JPA
- Hibernate

Responsibilities:

- Business Logic
- Authentication
- Authorization
- Validation
- API Layer
- File Management
- Audit Logging
- Notifications

---

## Database

Technology:

- MySQL

Responsibilities:

- Multi-tenant data
- Patient records
- Medical history
- Audit logs
- Users
- Appointments
- Prescriptions

---

## Cache Layer

Technology:

- Redis

Used for:

- Session caching
- Frequently accessed data
- Authentication tokens
- Rate limiting
- Performance optimization

---

## File Storage

Technology:

- AWS S3
- MinIO (Local Development)

Stores:

- Medical Reports
- MRI
- CT Scan
- X-Ray
- PDFs
- Images
- Documents

---

# 4. Multi-Tenant Architecture

Every hospital acts as an independent tenant.

**Strategy:** Shared Database + Shared Schema + Tenant ID discriminator.

See [MULTI_TENANCY.md](./MULTI_TENANCY.md) for the Phase 2.1 foundation: tenant aggregate,
lifecycle, identification, resolution contracts, isolation rules, and request lifecycle.

```
Tenant A
 ├── Users
 ├── Patients
 ├── Doctors
 └── Medical Records

Tenant B
 ├── Users
 ├── Patients
 ├── Doctors
 └── Medical Records

Tenant C
 ├── Users
 ├── Patients
 ├── Doctors
 └── Medical Records
```

Each tenant's data remains logically isolated via `tenant_id` on every business table.
Platform Super Admin rows may use `tenant_id = NULL`.

Persistence enforcement (Phase 2.4): tenant-owned entities extend `TenantOwnedEntity`;
Hibernate `tenantFilter` is enabled on JPA transaction begin from `TenantContextHolder`.
See [MULTI_TENANCY.md](./MULTI_TENANCY.md) §6.1.

### Request lifecycle (Phase 2.2–2.4)

```
Request
  ↓
Tenant Resolution (X-Tenant-ID)
  ↓
Tenant Validation (exists + ACTIVE)
  ↓
TenantContextHolder
  ↓
Business Layer
```

HTTP filter: `TenantFilter` (after JWT). See [MULTI_TENANCY.md](./MULTI_TENANCY.md).

---

# 5. Backend Architecture

The backend follows a layered architecture.

```
Controller

↓

Service

↓

Repository

↓

Database
```

Each module contains:

```
patient

├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── validator
└── exception
```

### Hospital Administration (Phases 4.1–4.9)

Organizational employment and departments live in `com.healthcare.hms.organization`,
separate from the hospital profile module (`hospitals`) and identity/RBAC (`users`).
Phase 4.9 production readiness: accept-invitation UI, soft-delete-aware staff↔user
uniqueness (Flyway V17), invitation expiry persistence, and user-lifecycle org hooks.
See [HOSPITAL_ADMIN_PHASE_4_9_REVIEW.md](./HOSPITAL_ADMIN_PHASE_4_9_REVIEW.md).

```
organization
├── controller/DepartmentController
├── service(+impl)/DepartmentService
├── repository/DepartmentRepository + Specifications
├── mapper/DepartmentMapper
├── dto/request|response
├── entity/Staff          # @MappedSuperclass — shared employment fields
├── entity/Department     # Phase 4.2 aggregate
├── enums                 # Employment*, DepartmentType, DepartmentStatus
├── department/           # documentation anchor
└── staff/                # reserved — Doctor/Nurse/… later
```

- `Department` and `Staff` extend `TenantOwnedEntity` (UUID, `tenant_id`, audit, soft delete, version).
- Cross-module reads use `HospitalQueryService` / `UserQueryService` (no foreign repositories).
- Full package layout, ER relationships, and module interaction diagrams:
  [HOSPITAL_ORGANIZATION.md](./HOSPITAL_ORGANIZATION.md).

---

# 6. Frontend Architecture

Feature-based organization.

```
src

├── app
├── components
├── features
├── hooks
├── layouts
├── lib
├── services
├── store
├── types
├── utils
└── styles
```

Each feature contains:

```
patients

├── components
├── hooks
├── api
├── pages
├── types
└── validation
```

---

# 7. Authentication Flow

Supported authentication:

- JWT
- Refresh Token

Flow:

```
Login

↓

Validate Credentials

↓

Generate JWT

↓

Generate Refresh Token

↓

Return Tokens

↓

Access Protected APIs
```

---

# 8. Authorization

Role-Based Access Control (RBAC)

Roles:

- Super Admin
- Hospital Admin
- Doctor
- Nurse
- Receptionist
- Laboratory Technician
- Pharmacist

Permissions are evaluated before every protected request.

---

# 9. API Architecture

RESTful APIs

```
Client

↓

API Controller

↓

Service Layer

↓

Repository

↓

Database
```

Standard response format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

---

# 10. Database Strategy

Primary Database:

- MySQL

ORM:

- Hibernate
- Spring Data JPA

Migration Tool:

- Flyway

Design Principles:

- Normalized schema
- Foreign key constraints
- Soft delete
- Audit fields
- Optimized indexing

---

# 11. Security Architecture

Authentication:

- JWT
- Refresh Tokens

Authorization:

- RBAC

Security Features:

- HTTPS
- Password Hashing
- Input Validation
- SQL Injection Prevention
- XSS Protection
- CSRF Protection
- Rate Limiting
- Secure Headers

---

# 12. Audit Logging

Every important action is logged.

Examples:

- Login
- Logout
- Patient Created
- Patient Updated
- Prescription Added
- Report Uploaded
- Record Deleted

Each log stores:

- User
- Timestamp
- Action
- Resource
- IP Address

---

# 13. Notification Architecture

Notification channels:

- Email
- In-App Notifications

Supported events:

- Appointment Reminder
- Follow-up Reminder
- System Alerts
- Password Reset

---

# 14. File Management

Supported file types:

- PDF
- JPG
- PNG
- DICOM (Future)

Maximum upload size is configurable.

Files are stored separately from the database.

Database stores only metadata.

---

# 15. Monitoring & Logging

Application Monitoring:

- Spring Boot Actuator

Metrics:

- Prometheus

Visualization:

- Grafana

Application Logs:

- Structured Logging
- Error Logs
- Audit Logs

---

# 16. Deployment Architecture

```
                Internet
                    │
                 Nginx
                    │
        ┌───────────┴───────────┐
        │                       │
   Next.js Frontend       Spring Boot API
                                │
                        ┌──────────────┐
                        │              │
                     MySQL          Redis
                        │
                     AWS S3
```

Deployment Targets:

- Frontend → Vercel
- Backend → AWS EC2
- Database → Managed MySQL
- Object Storage → AWS S3

---

# 17. Scalability Strategy

The architecture supports:

- Horizontal Scaling
- Stateless Backend
- Load Balancing
- Database Optimization
- Caching
- CDN for Static Assets

---

# 18. Disaster Recovery

The system shall support:

- Automated Database Backups
- File Backups
- Restore Procedures
- High Availability
- Health Checks

---

# 19. Architecture Principles

- Separation of Concerns
- Single Responsibility Principle
- Clean Architecture
- Modular Design
- Loose Coupling
- High Cohesion
- Security by Design
- API First
- Cloud Native
- Scalable Infrastructure
- Maintainable Codebase

```

```
