# Changelog

This project uses [Keep a Changelog](https://keepachangelog.com/) conventions.
For authoritative roadmap/feature context, see `docs/ROADMAP.md` and `docs/PROJECT_CONTEXT.md`.

All notable changes are tracked here by phase. Dates are when the phase work was
committed; "Setup" rows are the initial monorepo scaffolding commits, and the
"Refactor & polish" rows are the dedicated cleanup series on top of it.

## Unreleased

### Changed

- **Phase 8** — Database performance: added `UPPER(email)` functional indexes on `users`
  and `tenants` (Spring Data `IgnoreCase` email lookups were full-table scans; now indexed,
  ~106× faster at scale), batched the tenant role-provisioning existence check (9 queries →
  1), and removed the now-unused `RoleRepository.existsByTenantIdAndType`. Confirmed HikariCP
  is already tuned (max 30 / min 5) and list endpoints already paginate. No caching added —
  pricing is static frontend data (no DB query) and the permission/role catalog is small and
  only loaded during one-time registration.
- **Phase 7** — Registration redesign: no real tenant/hospital/admin account is created
  until email verification. The full signup is captured as a minimal, transient
  `pending_registrations` row (Option B); a scheduled job purges expired, unverified
  rows; verification creates the real records with `trialEndsAt` starting from the
  verification moment. The multi-step wizard was replaced by a single-page form
  (`/pricing` → `/register/hospital?plan=X`) that keeps the "Your Account" /
  "Hospital Details" visual separation.
- Repository hygiene (Phase 6 cleanup): removed committed screenshot/log artifacts,
  added `.gitignore` rules so generated captures are never tracked again, deleted
  leftover boilerplate docs (`backend/HELP.md`, `frontend/README.md`), consolidated
  phase history into this file, and removed dead/unused code.

## 2026-08-01

### Refactor & polish series

- **Phase 5** — Fixed registration regressions found in the full-stack walkthrough.
- **Phase 4** — Assembled the marketing landing page and shared site chrome.
- **Phase 3** — Built the dedicated pricing/plans page with free-trial support.
- **Phase 2** — Separated hospital registration from admin account creation.
- **Phase 1** — Fixed the hospital registration lock timeout and transaction boundary.
- Fixed Java language-server diagnostics (270 problems → 0 real).
- Aligned `HospitalRegistrationServiceImplTest` with the Phase 1 refactor.

## 2026-07 (initial build)

### Setup

- **Phase 9 setup** — completed project scaffolding (roles/permissions, workflows).
- **Phase 8 setup** — completed.
- **Phase 7 setup** — completed.
- **Phase 6 setup** — completed.
- **Phase 5 setup** — completed.
- **Phase 4 setup** — completed.
- **Phase 3 setup** — completed.
- **Phase 2 setup** — completed.
- **Phase 1 setup** — initial project scaffolding.
- Initial project setup (monorepo, tooling, CI).
