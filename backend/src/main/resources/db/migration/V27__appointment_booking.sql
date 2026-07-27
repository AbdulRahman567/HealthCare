-- Phase 6.3: Appointment booking — confirmation and cancellation metadata.

ALTER TABLE appointments
    ADD COLUMN confirmed_at TIMESTAMP(6) NULL AFTER notes,
    ADD COLUMN cancelled_at TIMESTAMP(6) NULL AFTER confirmed_at,
    ADD COLUMN cancellation_reason VARCHAR(500) NULL AFTER cancelled_at;
