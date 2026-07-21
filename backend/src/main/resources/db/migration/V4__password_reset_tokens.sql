-- Password recovery tokens (hashed at rest, single-use, time-limited)

CREATE TABLE password_reset_tokens (
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
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_tokens_tenant_id ON password_reset_tokens (tenant_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens (expires_at);
CREATE INDEX idx_password_reset_tokens_used_at ON password_reset_tokens (used_at);
CREATE INDEX idx_password_reset_tokens_deleted ON password_reset_tokens (deleted);
