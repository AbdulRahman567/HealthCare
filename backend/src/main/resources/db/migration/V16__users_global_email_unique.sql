-- Phase 4.8: enforce global email uniqueness to match email-only login/password-reset.
-- Prevents concurrent invite-accept / registration races from creating duplicate emails across tenants.
CREATE UNIQUE INDEX uk_users_email_global_lower ON users ((LOWER(email)));
