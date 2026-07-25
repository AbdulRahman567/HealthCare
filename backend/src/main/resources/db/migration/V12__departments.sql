-- Phase 4.2: Department management (tenant-owned organizational units).

CREATE TABLE departments (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    hospital_id         CHAR(36)      NOT NULL,
    name                VARCHAR(200)  NOT NULL,
    code                VARCHAR(50)   NOT NULL,
    description         VARCHAR(1000) NULL,
    department_type     VARCHAR(30)   NOT NULL,
    status              VARCHAR(30)   NOT NULL,
    location            VARCHAR(255)  NULL,
    head_user_id        CHAR(36)      NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_departments PRIMARY KEY (id),
    CONSTRAINT uk_departments_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT uk_departments_tenant_name UNIQUE (tenant_id, name),
    CONSTRAINT fk_departments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_departments_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_departments_head_user FOREIGN KEY (head_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_departments_tenant_id ON departments (tenant_id);
CREATE INDEX idx_departments_hospital_id ON departments (hospital_id);
CREATE INDEX idx_departments_status ON departments (tenant_id, status);
CREATE INDEX idx_departments_type ON departments (tenant_id, department_type);
CREATE INDEX idx_departments_head_user_id ON departments (head_user_id);
CREATE INDEX idx_departments_deleted ON departments (deleted);
CREATE INDEX idx_departments_name ON departments (tenant_id, name);
