-- Phase 4.9: Soft-delete-aware uniqueness for staff ↔ user links.
-- Live rows keep (tenant_id, user_id) unique; soft-deleted rows free the slot for rehire.

-- doctors
ALTER TABLE doctors DROP INDEX uk_doctors_tenant_user;
ALTER TABLE doctors
    ADD COLUMN active_user_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED;
CREATE UNIQUE INDEX uk_doctors_tenant_active_user
    ON doctors (tenant_id, user_id, active_user_slot);

-- nurses
ALTER TABLE nurses DROP INDEX uk_nurses_tenant_user;
ALTER TABLE nurses
    ADD COLUMN active_user_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED;
CREATE UNIQUE INDEX uk_nurses_tenant_active_user
    ON nurses (tenant_id, user_id, active_user_slot);

-- receptionists
ALTER TABLE receptionists DROP INDEX uk_receptionists_tenant_user;
ALTER TABLE receptionists
    ADD COLUMN active_user_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED;
CREATE UNIQUE INDEX uk_receptionists_tenant_active_user
    ON receptionists (tenant_id, user_id, active_user_slot);

-- laboratory_staff
ALTER TABLE laboratory_staff DROP INDEX uk_laboratory_staff_tenant_user;
ALTER TABLE laboratory_staff
    ADD COLUMN active_user_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED;
CREATE UNIQUE INDEX uk_laboratory_staff_tenant_active_user
    ON laboratory_staff (tenant_id, user_id, active_user_slot);

-- pharmacists
ALTER TABLE pharmacists DROP INDEX uk_pharmacists_tenant_user;
ALTER TABLE pharmacists
    ADD COLUMN active_user_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED;
CREATE UNIQUE INDEX uk_pharmacists_tenant_active_user
    ON pharmacists (tenant_id, user_id, active_user_slot);
