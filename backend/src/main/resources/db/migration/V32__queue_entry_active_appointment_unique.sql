-- Phase 6.10: Soft-delete-aware uniqueness for queue entry ↔ appointment.
-- Cancelled / soft-deleted entries free the appointment slot so patients can re-check-in.

ALTER TABLE queue_entries
    DROP INDEX uk_queue_entries_appointment;

ALTER TABLE queue_entries
    ADD COLUMN active_appointment_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED;

CREATE UNIQUE INDEX uk_queue_entries_active_appointment
    ON queue_entries (appointment_id, active_appointment_slot);
