# DEPLOYMENT.md

# Healthcare Management System (HMS)

## Deployment, Infrastructure & DevOps Guide

Version: 1.1  
Status: Active (local/staging checklist); cloud HA sections remain target architecture

---

# 0. Local & staging go-live checklist

Use this before any shared or production-like environment. Local laptop Compose may keep
documented defaults; **never** ship them to a shared host.

## 0.1 Secrets & config

| Item                                        | Required action                                   |
| ------------------------------------------- | ------------------------------------------------- |
| `JWT_SECRET` / `JWT_REFRESH_SECRET`         | Random ≥32 characters; no `change-me`             |
| `JWT_ALLOW_INSECURE_SECRETS`                | `false` (Spring profile `prod` defaults this)     |
| `DATABASE_PASSWORD` / `MYSQL_ROOT_PASSWORD` | Strong unique passwords                           |
| `GF_SECURITY_ADMIN_PASSWORD`                | Change Grafana admin password                     |
| `MINIO_ROOT_*` / `HMS_STORAGE_S3_*`         | Non-default if MinIO/S3 is exposed                |
| `CORS_ALLOWED_ORIGINS` / `FRONTEND_URL`     | Exact production origins only                     |
| `MAIL_ENABLED` + `SMTP_*`                   | Enable if auth/invite/reminder email must deliver |
| `REMINDERS_SCHEDULER_ENABLED`               | `true` only when email/SMS routing is ready       |
| `SPRING_PROFILES_ACTIVE`                    | `prod` (or staging profile) for non-local         |

Copy from `.env.example` → `.env` / `backend/.env` / `frontend/.env.local`. Backend loads
`backend/.env` via `DotenvEnvironmentPostProcessor` for local runs; Compose injects env into
containers from the root `.env`.

## 0.2 Runtime expectations

- MySQL 8 + Flyway migrations applied on startup (`ddl-auto=validate`).
- Redis container may run; session/rate-limit use is not fully wired yet — do not assume Redis HA is required for Phase 9 scope.
- Clinical attachments: `HMS_STORAGE_TYPE=local` or `s3` (MinIO/AWS). Persist volumes in Compose.
- SMS/PUSH appointment reminders are **stubs** (log only) until providers are integrated.
- CI: `.github/workflows/ci.yml` runs frontend lint/tsc and backend unit tests on PR/push.

## 0.3 Smoke checks

1. `GET /actuator/health/liveness` → UP
2. Login + refresh token rotation
3. Create consultation → issue prescription → open `/app/prescriptions/{id}/print`
4. Patient chart **Prescriptions** tab lists history
5. Soft-delete / RBAC denial returns 403 with stable `errorCode`

## 0.4 Out of scope until Phase 10+ / Phase 16

Laboratory, imaging, billing, full notification product, multi-region HA, automated DR drills,
and zero-downtime blue/green — described in later sections as **target** architecture.

---

# 1. Purpose

This document defines the deployment architecture, infrastructure, DevOps practices, disaster recovery strategy, monitoring, and production environment for the Healthcare Management System (HMS).

Goals

- Enterprise-grade deployment
- High availability
- Scalability
- Security
- Observability
- Disaster recovery
- Zero-downtime deployments

---

# 2. Production Architecture

Client

↓

Cloudflare CDN

↓

Nginx Reverse Proxy

↓

Frontend (Next.js)

↓

Spring Boot API

↓

Redis

↓

MySQL

↓

AWS S3

↓

Monitoring Stack

↓

Backup Storage

---

# 3. Technology Stack

Frontend

Next.js 15

Backend

Spring Boot 3

Database

MySQL 8

Cache

Redis

Storage

AWS S3

Containerization

Docker

Orchestration

Docker Compose

Reverse Proxy

Nginx

Monitoring

Prometheus

Grafana

Logging

Loki

Deployment

AWS EC2

CI/CD

GitHub Actions

SSL

Let's Encrypt

---

# 4. Environments

Development

Purpose

Local Development

---

Testing

Purpose

QA Testing

---

Staging

Purpose

Pre-Production Verification

---

Production

Purpose

Live Hospital Environment

---

# 5. Environment Variables

Backend

DATABASE_URL

DATABASE_USERNAME

DATABASE_PASSWORD

JWT_SECRET

JWT_REFRESH_SECRET

REDIS_HOST

REDIS_PORT

AWS_ACCESS_KEY

AWS_SECRET_KEY

AWS_BUCKET

SMTP_HOST

SMTP_USERNAME

SMTP_PASSWORD

FRONTEND_URL

BACKEND_URL

---

Frontend

NEXT_PUBLIC_API_URL

NEXT_PUBLIC_APP_NAME

NEXT_PUBLIC_ENV

---

Never commit

.env

to Git.

---

# 6. Docker Containers

Frontend

nextjs-app

Backend

springboot-api

Database

mysql

Cache

redis

Reverse Proxy

nginx

Monitoring

prometheus

grafana

loki

---

# 7. Docker Compose Services

services

frontend

backend

mysql

redis

nginx

prometheus

grafana

loki

Volumes

Database

Redis

Logs

Uploads

---

# 8. Nginx Responsibilities

Serve Frontend

Reverse Proxy API

SSL Termination

Compression

Caching

Security Headers

Rate Limiting

Static Asset Delivery

---

# 9. SSL

HTTPS Required

TLS 1.3

Automatic Renewal

Let's Encrypt

Redirect HTTP → HTTPS

HSTS Enabled

---

# 10. Database Deployment

Engine

MySQL 8

Features

Daily Backups

Replication Ready

Connection Pooling

Automatic Recovery

Migration

Flyway

---

# 11. Redis Deployment

Purpose

Caching

JWT Blacklist

Session Data

Rate Limiting

Temporary Data

---

# 12. File Storage

AWS S3

Store

Patient Reports

Medical Images

PDF Prescriptions

Hospital Logos

Documents

Never store uploaded files inside application containers.

---

# 13. Logging

Application Logs

Spring Boot

Access Logs

Nginx

Audit Logs

Database

Container Logs

Docker

Centralized using

Loki

---

# 14. Monitoring

Prometheus

Collect

CPU

Memory

Disk

Network

Application Metrics

Database Metrics

Redis Metrics

---

Grafana

Dashboards

API Performance

Hospital Activity

Database Health

Server Health

Error Rate

Request Count

---

# 15. Health Checks

Backend

/actuator/health

Database

Connection Test

Redis

Ping

Frontend

Availability Check

Docker

Container Health

---

# 16. Backup Strategy

Database

Daily Incremental

Weekly Full

Monthly Archive

S3

Versioning Enabled

Retention

90 Days

Backups must be encrypted.

---

# 17. Disaster Recovery

Recovery Objectives

RPO

≤ 15 Minutes

RTO

≤ 60 Minutes

Recovery Process

Restore Database

Restore Files

Restart Containers

Validate Services

Resume Traffic

---

# 18. CI/CD Pipeline

Trigger

Push to Main

↓

Install Dependencies

↓

Run Lint

↓

Run Unit Tests

↓

Run Integration Tests

↓

Build Application

↓

Security Scan

↓

Docker Build

↓

Push Images

↓

Deploy

↓

Health Check

↓

Notify Team

---

# 19. GitHub Actions

Pipeline Stages

Checkout

Java Setup

Node Setup

Install Dependencies

Run Tests

Build

Create Docker Images

Push Registry

Deploy

Smoke Test

---

# 20. Deployment Strategy

Preferred

Rolling Deployment

Future

Blue-Green Deployment

Canary Releases

Zero Downtime

---

# 21. Security

Firewall Enabled

SSH Key Authentication

Fail2Ban

HTTPS Only

Secrets via Environment Variables

Database Private

Redis Private

Automatic Security Updates

---

# 22. Scaling Strategy

Frontend

Horizontal Scaling

Backend

Horizontal Scaling

Database

Read Replicas

Redis

Cluster Mode

Storage

Unlimited via S3

---

# 23. Performance Targets

API Response

< 300 ms

Frontend Load

< 2 Seconds

Dashboard

< 2 Seconds

File Upload

< 5 Seconds

Availability

99.9%

---

# 24. Domain Structure

Production

app.company.com

API

api.company.com

Monitoring

monitor.company.com

Documentation

docs.company.com

---

# 25. Required Ports

80

HTTP

443

HTTPS

8080

Spring Boot

3306

MySQL

6379

Redis

9090

Prometheus

3000

Grafana

3100

Loki

---

# 26. Infrastructure Checklist

✓ Docker Installed

✓ Docker Compose Installed

✓ Java 21

✓ Node.js LTS

✓ Nginx Configured

✓ SSL Installed

✓ Redis Running

✓ MySQL Running

✓ Monitoring Enabled

✓ Daily Backups Configured

✓ Firewall Enabled

✓ Health Checks Enabled

---

# 27. Production Readiness Checklist

✓ Environment Variables Configured

✓ Secrets Protected

✓ Database Migrated

✓ HTTPS Enabled

✓ CI/CD Working

✓ Monitoring Active

✓ Logging Centralized

✓ Backups Verified

✓ Security Headers Enabled

✓ Performance Tested

✓ Documentation Complete

---

# 28. Future Enhancements

- Kubernetes Deployment
- AWS ECS
- AWS EKS
- Auto Scaling Groups
- CloudFront CDN
- Multi-Region Deployment
- Database Failover
- Redis Cluster
- Service Mesh
- Infrastructure as Code (Terraform)
- GitOps Deployment
- AI-Based Infrastructure Monitoring

---

End of DEPLOYMENT.md
