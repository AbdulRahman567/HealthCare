-- Phase 7.2: Consultation clinical documentation fields + pause support.
-- Use TEXT (not VARCHAR(4000)): utf8mb4 + existing VARCHAR columns would exceed InnoDB row size (65535).

ALTER TABLE consultations
    ADD COLUMN history_of_present_illness TEXT NULL AFTER summary,
    ADD COLUMN physical_examination TEXT NULL AFTER history_of_present_illness,
    ADD COLUMN doctor_notes TEXT NULL AFTER physical_examination,
    ADD COLUMN paused_at TIMESTAMP(6) NULL AFTER completed_at;
