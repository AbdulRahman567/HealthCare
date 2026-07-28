-- Phase 7.7: Follow-up management enhancements — clinical recommendations + reminder readiness.

ALTER TABLE consultation_follow_ups
    ADD COLUMN clinical_recommendations VARCHAR(2000) NULL AFTER instructions,
    ADD COLUMN reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER clinical_recommendations,
    ADD COLUMN reminder_lead_days INT NOT NULL DEFAULT 1 AFTER reminder_enabled,
    ADD COLUMN next_reminder_at TIMESTAMP(6) NULL AFTER reminder_lead_days,
    ADD COLUMN last_reminder_at TIMESTAMP(6) NULL AFTER next_reminder_at,
    ADD COLUMN reminder_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER last_reminder_at;

ALTER TABLE consultation_follow_ups
    ADD CONSTRAINT chk_consultation_follow_ups_reminder_lead
        CHECK (reminder_lead_days >= 0 AND reminder_lead_days <= 90);

CREATE INDEX idx_consultation_follow_ups_reminder_dispatch
    ON consultation_follow_ups (tenant_id, reminder_status, next_reminder_at);

CREATE INDEX idx_consultation_follow_ups_doctor_due
    ON consultation_follow_ups (tenant_id, doctor_id, status, scheduled_date);
