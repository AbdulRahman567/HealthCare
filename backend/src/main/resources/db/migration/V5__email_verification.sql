-- Email verification support: user flags + hashed single-use tokens

ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER phone,
    ADD COLUMN email_verified_at TIMESTAMP(6) NULL AFTER email_verified;

-- Grandfather existing accounts so verification does not lock out prior users
UPDATE users
SET email_verified = TRUE,
    email_verified_at = COALESCE(created_at, CURRENT_TIMESTAMP(6))
WHERE email_verified = FALSE;

CREATE INDEX idx_users_email_verified ON users (email_verified);

CREATE TABLE email_verification_tokens (
    id                      CHAR(36)     NOT NULL,
    tenant_id               CHAR(36)     NULL,
    user_id                 CHAR(36)     NOT NULL,
    token_hash              VARCHAR(128) NOT NULL,
    expires_at              TIMESTAMP(6) NOT NULL,
    used_at                 TIMESTAMP(6) NULL,
    ip_address              VARCHAR(45)  NULL,
    user_agent              VARCHAR(512) NULL,
    created_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by              CHAR(36)     NULL,
    updated_by              CHAR(36)     NULL,
    deleted                 BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP(6) NULL,
    deleted_by              CHAR(36)     NULL,
    version                 BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_email_verification_tokens PRIMARY KEY (id),
    CONSTRAINT uk_email_verification_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens (user_id);
CREATE INDEX idx_email_verification_tokens_tenant_id ON email_verification_tokens (tenant_id);
CREATE INDEX idx_email_verification_tokens_expires_at ON email_verification_tokens (expires_at);
CREATE INDEX idx_email_verification_tokens_used_at ON email_verification_tokens (used_at);
CREATE INDEX idx_email_verification_tokens_deleted ON email_verification_tokens (deleted);
