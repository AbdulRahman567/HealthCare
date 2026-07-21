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
- Tenant Isolation

Never expose:

- Passwords
- Secrets
- Internal errors
- Sensitive medical information

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
├── hospitals
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
