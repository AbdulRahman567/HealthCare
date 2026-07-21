-- Tenants (hospitals), audit logs, user token version, and Hospital Admin RBAC grants.

CREATE TABLE tenants (
    id                  CHAR(36)     NOT NULL,
    name                VARCHAR(200) NOT NULL,
    slug                VARCHAR(120) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    phone               VARCHAR(30)  NULL,
    address             VARCHAR(500) NULL,
    logo_url            VARCHAR(500) NULL,
    subscription_plan   VARCHAR(50)  NOT NULL,
    status              VARCHAR(30)  NOT NULL,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)     NULL,
    updated_by          CHAR(36)     NULL,
    deleted             BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6) NULL,
    deleted_by          CHAR(36)     NULL,
    version             BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uk_tenants_slug UNIQUE (slug),
    CONSTRAINT uk_tenants_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tenants_status ON tenants (status);
CREATE INDEX idx_tenants_deleted ON tenants (deleted);

CREATE TABLE audit_logs (
    id              CHAR(36)     NOT NULL,
    tenant_id       CHAR(36)     NULL,
    user_id         CHAR(36)     NULL,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       VARCHAR(64)  NULL,
    action          VARCHAR(50)  NOT NULL,
    old_value       TEXT         NULL,
    new_value       TEXT         NULL,
    ip_address      VARCHAR(45)  NULL,
    user_agent      VARCHAR(512) NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by      CHAR(36)     NULL,
    updated_by      CHAR(36)     NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP(6) NULL,
    deleted_by      CHAR(36)     NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_logs_tenant_id ON audit_logs (tenant_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_deleted ON audit_logs (deleted);

ALTER TABLE users
    ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0 AFTER last_login_at;

-- Grant operational permissions to Hospital Admin system role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000002', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'USER_READ', 'USER_WRITE', 'USER_DELETE',
      'ROLE_READ',
      'HOSPITAL_READ', 'HOSPITAL_WRITE',
      'DEPARTMENT_READ', 'DEPARTMENT_WRITE', 'DEPARTMENT_DELETE',
      'DOCTOR_READ', 'DOCTOR_WRITE', 'DOCTOR_DELETE',
      'PATIENT_READ', 'PATIENT_WRITE', 'PATIENT_DELETE',
      'APPOINTMENT_READ', 'APPOINTMENT_WRITE', 'APPOINTMENT_DELETE',
      'VISIT_READ', 'VISIT_WRITE', 'VISIT_DELETE',
      'PRESCRIPTION_READ', 'PRESCRIPTION_CREATE', 'PRESCRIPTION_WRITE', 'PRESCRIPTION_DELETE',
      'LAB_READ', 'LAB_WRITE', 'LAB_DELETE',
      'RADIOLOGY_READ', 'RADIOLOGY_WRITE', 'RADIOLOGY_DELETE',
      'DOCUMENT_READ', 'DOCUMENT_WRITE', 'DOCUMENT_DELETE',
      'NOTIFICATION_READ', 'NOTIFICATION_WRITE',
      'AUDIT_READ', 'DASHBOARD_READ', 'REPORT_READ'
  );
