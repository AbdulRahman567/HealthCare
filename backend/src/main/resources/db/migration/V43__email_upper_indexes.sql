-- Phase 8: case-insensitive email lookups.
--
-- Spring Data derived `...EmailIgnoreCase` queries emit `UPPER(email) = UPPER(?)`,
-- which cannot use the existing `LOWER(email)` functional unique index (users) or the
-- plain `email` indexes (tenants). EXPLAIN confirmed a full table scan
-- (`type=ALL`) on those lookups; these UPPER(email) functional indexes turn them
-- into indexed lookups (`type=ref`).
--
-- Scoped to users and tenants only: EXPLAIN confirmed the optimizer uses these indexes
-- there. `user_invitations` and `pending_registrations` are currently tiny and the
-- optimizer prefers the `deleted` index, so those were deliberately left out to avoid
-- speculative indexes.

CREATE INDEX idx_users_email_upper ON users ((UPPER(email)));
CREATE INDEX idx_tenants_email_upper ON tenants ((UPPER(email)));
