-- Phase 7.10: Soft-delete-aware uniqueness for clinical invariants.
-- 1) At most one live PRIMARY diagnosis per consultation.
-- 2) At most one live follow-up linked to a given appointment.

ALTER TABLE consultation_diagnoses
    ADD COLUMN active_primary_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE AND diagnosis_type = 'PRIMARY' THEN 'Y' ELSE NULL END
    ) STORED;

CREATE UNIQUE INDEX uk_consultation_diagnoses_active_primary
    ON consultation_diagnoses (tenant_id, consultation_id, active_primary_slot);

ALTER TABLE consultation_follow_ups
    ADD COLUMN active_follow_up_appointment_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE AND follow_up_appointment_id IS NOT NULL THEN 'Y' ELSE NULL END
    ) STORED;

-- Drop non-unique lookup index; unique index covers the same (tenant, appointment) prefix.
DROP INDEX idx_consultation_follow_ups_appointment ON consultation_follow_ups;

CREATE UNIQUE INDEX uk_consultation_follow_ups_active_appointment
    ON consultation_follow_ups (tenant_id, follow_up_appointment_id, active_follow_up_appointment_slot);
