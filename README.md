# Healthcare Management System (HMS)

Enterprise-grade multi-tenant Healthcare Management System monorepo.

## Tech Stack

- Frontend: Next.js 15, React 19, TypeScript, Tailwind CSS, shadcn/ui, Redux Toolkit, TanStack Query
- Backend: Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Redis, OpenAPI
- Infrastructure: Docker, Docker Compose, MySQL 8, Redis 7, MinIO, Nginx, Prometheus, Grafana

## Monorepo Structure

```text
frontend/                # Next.js App Router frontend
backend/                 # Spring Boot backend
docker/                  # Nginx and monitoring configuration
docs/                    # Project source-of-truth documents
.github/workflows/       # CI (frontend lint/tsc + backend unit tests)
docker-compose.yml       # Local multi-service orchestration
```

## Prerequisites

- Node.js 22+
- npm 10+
- Java 21
- Docker and Docker Compose (recommended for MySQL/Redis)

Maven Wrapper is included under `backend/` (`mvnw` / `mvnw.cmd`) — system Maven is optional.

## Local setup checklist

1. **Copy env templates** (never commit real `.env` files):

   ```bash
   cp .env.example .env
   cp backend/.env.example backend/.env
   cp frontend/.env.example frontend/.env.local
   ```

2. **Start infrastructure** (MySQL + Redis at minimum):

   ```bash
   npm run docker:up
   ```

   Or run only data services and start apps with `npm run dev`.

3. **Backend env loading** — `backend/.env` is loaded automatically by
   `DotenvEnvironmentPostProcessor` when you run Spring Boot from the repo root or
   `backend/`. OS / Docker / IDE environment variables always win over dotenv.
   Optional override path: `HMS_DOTENV_LOCATION=/absolute/path/to/.env`.

4. **JWT (local vs shared)**
   - Local: placeholder secrets + `JWT_ALLOW_INSECURE_SECRETS=true` (defaults in examples).
   - Staging/production: strong random `JWT_SECRET` / `JWT_REFRESH_SECRET` (≥32 chars) and
     `JWT_ALLOW_INSECURE_SECRETS=false` (also default under Spring profile `prod`).

5. **Email (optional)** — with `MAIL_ENABLED=false` (default), reset/verify/invite links are
   logged only. For real SMTP set `MAIL_ENABLED=true` plus `SMTP_*` / `MAIL_FROM`.

6. **Object storage** — default `HMS_STORAGE_TYPE=local`. For MinIO in Compose set
   `HMS_STORAGE_TYPE=s3` and the `HMS_STORAGE_S3_*` values from `.env.example`.

7. **Reminders** — `REMINDERS_SCHEDULER_ENABLED=false` locally by default. SMS/PUSH channels
   are logging stubs until providers are integrated.

## Development

```bash
npm run dev
```

Docker Compose (full stack):

```bash
npm run docker:up
npm run docker:down
```

## Quality Scripts

- `npm run lint` — frontend lint
- `npm run format` / `npm run format:write` — Prettier
- `npm run build` — frontend + backend production build
- Backend tests: `backend/mvnw.cmd -f backend/pom.xml test` (Windows) or `./backend/mvnw test`

## Endpoints

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/v1/system/health
- Swagger UI: http://localhost:8080/swagger-ui
- Actuator: http://localhost:8080/actuator/health
- Nginx: http://localhost
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001 (change default admin password for shared hosts)
- MinIO console: http://localhost:9001

## Current Scope

**Done (ROADMAP Phases 1–9):** auth/RBAC, multi-tenant hospitals, users/staff, patients + medical records (incl. family history), appointments/queue, clinical consultations (vitals, diagnosis, notes, follow-ups), digital prescriptions + printable Rx + patient Rx history.

**Not yet (Phase 10+):** laboratory & imaging, full notification productization, dashboards/analytics, deeper audit/security hardening phases, performance pass, full QA matrix, production cloud deployment.

Authoritative docs: `docs/ROADMAP.md`, `docs/PROJECT_CONTEXT.md`, `docs/CLINICAL_WORKFLOW.md`, `docs/PATIENT_MANAGEMENT.md`, `docs/DEPLOYMENT.md`.
