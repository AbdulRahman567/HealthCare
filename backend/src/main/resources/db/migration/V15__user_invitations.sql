-- Phase 4.5: User invitation (invite-by-email, hashed token, accept/reject/resend/cancel).

CREATE TABLE user_invitations (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    hospital_id         CHAR(36)      NOT NULL,
    email               VARCHAR(255)  NOT NULL,
    first_name          VARCHAR(100)  NULL,
    last_name           VARCHAR(100)  NULL,
    role_type           VARCHAR(30)   NOT NULL,
    invited_by          CHAR(36)      NOT NULL,
    token_hash          VARCHAR(128)  NOT NULL,
    status              VARCHAR(30)   NOT NULL,
    expires_at          TIMESTAMP(6)  NOT NULL,
    accepted_at         TIMESTAMP(6)  NULL,
    rejected_at         TIMESTAMP(6)  NULL,
    cancelled_at        TIMESTAMP(6)  NULL,
    accepted_user_id    CHAR(36)      NULL,
    message             VARCHAR(500)  NULL,
    ip_address          VARCHAR(45)   NULL,
    user_agent          VARCHAR(512)  NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_user_invitations PRIMARY KEY (id),
    CONSTRAINT uk_user_invitations_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_user_invitations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_user_invitations_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_user_invitations_invited_by FOREIGN KEY (invited_by) REFERENCES users (id),
    CONSTRAINT fk_user_invitations_accepted_user FOREIGN KEY (accepted_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_invitations_tenant_id ON user_invitations (tenant_id);
CREATE INDEX idx_user_invitations_email ON user_invitations (tenant_id, email);
CREATE INDEX idx_user_invitations_status ON user_invitations (tenant_id, status);
CREATE INDEX idx_user_invitations_expires_at ON user_invitations (expires_at);
CREATE INDEX idx_user_invitations_invited_by ON user_invitations (invited_by);
CREATE INDEX idx_user_invitations_deleted ON user_invitations (deleted);

-- At most one PENDING invitation per tenant email (open_slot = 'Y' while PENDING).
ALTER TABLE user_invitations
    ADD COLUMN pending_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN status = 'PENDING' AND deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED;

CREATE UNIQUE INDEX uk_user_invitations_pending_email
    ON user_invitations (tenant_id, email, pending_slot);
