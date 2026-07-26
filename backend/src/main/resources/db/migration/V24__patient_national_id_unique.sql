-- Phase 5.10: Soft-delete-aware uniqueness for national ID (CNIC / passport).
-- Live rows: at most one national_id per tenant when present.
-- Soft-deleted rows free the slot (NULL active_national_id_slot).
-- Rows with NULL national_id are unconstrained (MySQL UNIQUE allows multiple NULLs).

ALTER TABLE patients
    ADD COLUMN active_national_id_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE AND national_id IS NOT NULL THEN 'Y' ELSE NULL END
    ) STORED;

-- Replace non-unique lookup index; unique index covers (tenant_id, national_id) prefix.
DROP INDEX idx_patients_tenant_national_id ON patients;

CREATE UNIQUE INDEX uk_patients_tenant_active_national_id
    ON patients (tenant_id, national_id, active_national_id_slot);
