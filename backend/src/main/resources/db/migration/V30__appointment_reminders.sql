-- Phase 6.8: Appointment reminder infrastructure (status tracking + dispatch indexes).
-- Channel delivery (email/SMS/push) is abstracted; external providers are not wired yet.

CREATE TABLE appointment_reminders (
    id                    CHAR(36)      NOT NULL,
    tenant_id             CHAR(36)      NOT NULL,
    appointment_id        CHAR(36)      NOT NULL,
    hospital_id           CHAR(36)      NOT NULL,
    patient_id            CHAR(36)      NOT NULL,
    reminder_type         VARCHAR(40)   NOT NULL,
    channel               VARCHAR(20)   NOT NULL,
    status                VARCHAR(30)   NOT NULL,
    lead_offset_minutes   INT           NOT NULL,
    scheduled_at          TIMESTAMP(6)  NOT NULL,
    sent_at               TIMESTAMP(6)  NULL,
    last_attempt_at       TIMESTAMP(6)  NULL,
    attempt_count         INT           NOT NULL DEFAULT 0,
    max_attempts          INT           NOT NULL DEFAULT 3,
    recipient             VARCHAR(255)  NULL,
    provider_message_id   VARCHAR(100)  NULL,
    failure_reason        VARCHAR(500)  NULL,
    created_at            TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by            CHAR(36)      NULL,
    updated_by            CHAR(36)      NULL,
    deleted               BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMP(6)  NULL,
    deleted_by            CHAR(36)      NULL,
    version               BIGINT        NOT NULL DEFAULT 0,
    active_pending_slot   CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE AND status = 'PENDING' THEN 'Y' ELSE NULL END
    ) STORED,
    CONSTRAINT pk_appointment_reminders PRIMARY KEY (id),
    CONSTRAINT fk_appointment_reminders_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_appointment_reminders_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_appointment_reminders_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_appointment_reminders_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT chk_appointment_reminders_lead_offset CHECK (lead_offset_minutes >= 0),
    CONSTRAINT chk_appointment_reminders_attempt_count CHECK (attempt_count >= 0),
    CONSTRAINT chk_appointment_reminders_max_attempts CHECK (max_attempts >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- At most one PENDING reminder per appointment + channel + type + lead offset.
CREATE UNIQUE INDEX uk_appointment_reminders_pending_slot
    ON appointment_reminders (appointment_id, channel, reminder_type, lead_offset_minutes, active_pending_slot);

CREATE INDEX idx_appointment_reminders_tenant_id
    ON appointment_reminders (tenant_id);
CREATE INDEX idx_appointment_reminders_appointment
    ON appointment_reminders (tenant_id, appointment_id);
CREATE INDEX idx_appointment_reminders_dispatch
    ON appointment_reminders (status, scheduled_at);
CREATE INDEX idx_appointment_reminders_tenant_status_scheduled
    ON appointment_reminders (tenant_id, status, scheduled_at);
CREATE INDEX idx_appointment_reminders_channel_status
    ON appointment_reminders (tenant_id, channel, status);
CREATE INDEX idx_appointment_reminders_deleted
    ON appointment_reminders (deleted);
