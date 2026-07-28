-- Phase 7.2: Consultation clinical documentation fields + pause support.

ALTER TABLE consultations
    ADD COLUMN history_of_present_illness VARCHAR(4000) NULL AFTER summary,
    ADD COLUMN physical_examination VARCHAR(4000) NULL AFTER history_of_present_illness,
    ADD COLUMN doctor_notes VARCHAR(4000) NULL AFTER physical_examination,
    ADD COLUMN paused_at TIMESTAMP(6) NULL AFTER completed_at;
