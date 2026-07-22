-- Phase 2.5: Default hospital profile per tenant (initial registration).
-- Tenant remains the isolation boundary; hospitals are tenant-owned operational profiles.

CREATE TABLE hospitals (
    id              CHAR(36)     NOT NULL,
    tenant_id       CHAR(36)     NOT NULL,
    name            VARCHAR(200) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    email           VARCHAR(255) NOT NULL,
    phone           VARCHAR(30)  NULL,
    address         VARCHAR(500) NULL,
    is_default      BOOLEAN      NOT NULL DEFAULT TRUE,
    status          VARCHAR(30)  NOT NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by      CHAR(36)     NULL,
    updated_by      CHAR(36)     NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP(6) NULL,
    deleted_by      CHAR(36)     NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_hospitals PRIMARY KEY (id),
    CONSTRAINT uk_hospitals_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT uk_hospitals_tenant_name UNIQUE (tenant_id, name),
    CONSTRAINT fk_hospitals_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_hospitals_tenant_id ON hospitals (tenant_id);
CREATE INDEX idx_hospitals_status ON hospitals (status);
CREATE INDEX idx_hospitals_is_default ON hospitals (tenant_id, is_default);
CREATE INDEX idx_hospitals_deleted ON hospitals (deleted);
