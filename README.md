# Healthcare Management System (HMS)

Enterprise-grade Healthcare Management System monorepo (auth/foundation phase complete; clinical modules phased per ROADMAP).

## Tech Stack

- Frontend: Next.js 15, React 19, TypeScript, Tailwind CSS, shadcn/ui, Redux Toolkit, TanStack Query
- Backend: Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Redis, OpenAPI
- Infrastructure: Docker, Docker Compose, MySQL 8, Redis 7, Nginx, Prometheus, Grafana

## Monorepo Structure

```text
frontend/                # Next.js App Router frontend
backend/                 # Spring Boot backend
docker/                  # Nginx and monitoring configuration
docs/                    # Project source-of-truth documents
.github/workflows/       # CI workflows (skeleton — add pipelines before production)
docker-compose.yml       # Local multi-service orchestration
```

## Prerequisites

- Node.js 22+
- npm 10+
- Java 21
- Maven 3.9+
- Docker and Docker Compose

## Environment Variables

Copy and configure environment files:

- Root: `.env.example`
- Frontend: `frontend/.env.example`
- Backend: `backend/.env.example`

For Docker builds, `NEXT_PUBLIC_*` values are passed as **build args** (Next.js inlines them at build time). JWT secrets must be overridden for any shared/staging environment; set `JWT_ALLOW_INSECURE_SECRETS=false` in production.

## Development

Run frontend and backend locally:

```bash
npm run dev
```

Run services with Docker Compose:

```bash
npm run docker:up
```

Stop Docker services:

```bash
npm run docker:down
```

## Quality Scripts

- `npm run lint` - frontend lint checks
- `npm run format` - prettier check
- `npm run format:write` - prettier auto-fix
- `npm run build` - frontend + backend production build

## Endpoints

- Frontend: [http://localhost:3000](http://localhost:3000)
- Backend API: [http://localhost:8080/api/v1/system/health](http://localhost:8080/api/v1/system/health)
- Swagger UI: [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)
- Actuator Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Nginx: [http://localhost](http://localhost)
- Prometheus: [http://localhost:9090](http://localhost:9090)
- Grafana: [http://localhost:3001](http://localhost:3001)

## Current Scope

Implemented (Phases 1–2 foundation + authentication):

- Monorepo, Docker, Flyway, Actuator, OpenAPI
- JWT access tokens + opaque refresh tokens (rotation + reuse detection)
- Hospital registration + initial admin onboarding (tenant stays `PENDING` until admin email verification)
- Login / logout / refresh / profile
- Password reset + email verification flows
- RBAC scaffolding (roles/permissions seeded; clinical modules not yet exposed)
- Protected frontend routes (`/app`)

Not yet implemented (later ROADMAP phases): patient, appointment, visit, prescription, laboratory, billing, and other clinical modules.
