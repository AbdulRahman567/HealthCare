# Healthcare Management System (HMS)

Enterprise-grade, production-ready Healthcare Management System monorepo.

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
.github/workflows/       # CI workflow folder skeleton
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

This repository currently contains only project foundation (Phase 0 / Foundation stage):

- Monorepo setup
- Frontend baseline setup
- Backend baseline setup
- Infrastructure and observability setup
- Engineering quality tooling setup

No authentication, patient, appointment, or business modules are implemented in this phase.
