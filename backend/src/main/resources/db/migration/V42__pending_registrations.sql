-- Pending hospital registrations (Phase 7).
--
-- Stores the FULL signup payload (admin account + hospital details + chosen plan)
-- from the single-page registration form WITHOUT creating any real tenant, hospital,
-- admin user, or user row. Real records are created only when the emailed
-- verification link is clicked.
--
-- Rows are transient: a scheduled job deletes any row whose verification token has
-- expired and has not been verified. trial_ends_at is computed at verification time,
-- never here, so users do not lose trial days while sitting in their inbox.

CREATE TABLE pending_registrations (
    id                  CHAR(36)     NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    phone               VARCHAR(30)  NULL,
    hospital_name       VARCHAR(200) NOT NULL,
    hospital_email      VARCHAR(255) NOT NULL,
    hospital_phone      VARCHAR(30)  NULL,
    hospital_address    VARCHAR(500) NULL,
    subscription_plan   VARCHAR(20)  NOT NULL,
    token_hash          VARCHAR(128) NOT NULL,
    token_expires_at    TIMESTAMP(6) NOT NULL,
    submitted_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    verified_at         TIMESTAMP(6) NULL,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)     NULL,
    updated_by          CHAR(36)     NULL,
    deleted             BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6) NULL,
    deleted_by          CHAR(36)     NULL,
    version             BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_pending_registrations PRIMARY KEY (id),
    CONSTRAINT uk_pending_registrations_token_hash UNIQUE (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pending_registrations_email ON pending_registrations (email);
CREATE INDEX idx_pending_registrations_token_expires_at ON pending_registrations (token_expires_at);
CREATE INDEX idx_pending_registrations_verified_at ON pending_registrations (verified_at);
CREATE INDEX idx_pending_registrations_deleted ON pending_registrations (deleted);
