-- Identity domain: users, roles, permissions, refresh tokens, and RBAC join tables.

CREATE TABLE permissions (
    id              CHAR(36)     NOT NULL,
    code            VARCHAR(100) NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(500) NULL,
    module          VARCHAR(80)  NOT NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by      CHAR(36)     NULL,
    updated_by      CHAR(36)     NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP(6) NULL,
    deleted_by      CHAR(36)     NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permissions_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_permissions_module ON permissions (module);
CREATE INDEX idx_permissions_deleted ON permissions (deleted);

CREATE TABLE roles (
    id              CHAR(36)     NOT NULL,
    tenant_id       CHAR(36)     NULL,
    name            VARCHAR(100) NOT NULL,
    type            VARCHAR(50)  NOT NULL,
    description     VARCHAR(500) NULL,
    system_role     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by      CHAR(36)     NULL,
    updated_by      CHAR(36)     NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP(6) NULL,
    deleted_by      CHAR(36)     NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_tenant_type UNIQUE (tenant_id, type),
    CONSTRAINT uk_roles_tenant_name UNIQUE (tenant_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_roles_tenant_id ON roles (tenant_id);
CREATE INDEX idx_roles_type ON roles (type);
CREATE INDEX idx_roles_deleted ON roles (deleted);

CREATE TABLE users (
    id              CHAR(36)     NOT NULL,
    tenant_id       CHAR(36)     NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    phone           VARCHAR(30)  NULL,
    status          VARCHAR(30)  NOT NULL,
    last_login_at   TIMESTAMP(6) NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by      CHAR(36)     NULL,
    updated_by      CHAR(36)     NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP(6) NULL,
    deleted_by      CHAR(36)     NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_tenant_email UNIQUE (tenant_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_tenant_id ON users (tenant_id);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_deleted ON users (deleted);

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

CREATE TABLE role_permissions (
    role_id       CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT uk_role_permissions_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_role_permissions_role_id ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions (permission_id);

CREATE TABLE refresh_tokens (
    id                      CHAR(36)     NOT NULL,
    tenant_id               CHAR(36)     NULL,
    user_id                 CHAR(36)     NOT NULL,
    token_hash              VARCHAR(128) NOT NULL,
    expires_at              TIMESTAMP(6) NOT NULL,
    revoked                 BOOLEAN      NOT NULL DEFAULT FALSE,
    revoked_at              TIMESTAMP(6) NULL,
    replaced_by_token_hash  VARCHAR(128) NULL,
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
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_tenant_id ON refresh_tokens (tenant_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens (revoked);
CREATE INDEX idx_refresh_tokens_deleted ON refresh_tokens (deleted);

-- Seed global permission catalog
INSERT INTO permissions (id, code, name, description, module) VALUES
('a1000000-0000-4000-8000-000000000001', 'USER_READ', 'Read users', 'View user profiles and listings', 'USER'),
('a1000000-0000-4000-8000-000000000002', 'USER_WRITE', 'Write users', 'Create and update users', 'USER'),
('a1000000-0000-4000-8000-000000000003', 'USER_DELETE', 'Delete users', 'Soft-delete users', 'USER'),
('a1000000-0000-4000-8000-000000000004', 'ROLE_READ', 'Read roles', 'View roles and assignments', 'ROLE'),
('a1000000-0000-4000-8000-000000000005', 'ROLE_WRITE', 'Write roles', 'Create and update roles', 'ROLE'),
('a1000000-0000-4000-8000-000000000006', 'ROLE_DELETE', 'Delete roles', 'Soft-delete roles', 'ROLE'),
('a1000000-0000-4000-8000-000000000007', 'HOSPITAL_READ', 'Read hospitals', 'View hospital profiles', 'HOSPITAL'),
('a1000000-0000-4000-8000-000000000008', 'HOSPITAL_WRITE', 'Write hospitals', 'Create and update hospitals', 'HOSPITAL'),
('a1000000-0000-4000-8000-000000000009', 'DEPARTMENT_READ', 'Read departments', 'View departments', 'DEPARTMENT'),
('a1000000-0000-4000-8000-000000000010', 'DEPARTMENT_WRITE', 'Write departments', 'Create and update departments', 'DEPARTMENT'),
('a1000000-0000-4000-8000-000000000011', 'DEPARTMENT_DELETE', 'Delete departments', 'Soft-delete departments', 'DEPARTMENT'),
('a1000000-0000-4000-8000-000000000012', 'DOCTOR_READ', 'Read doctors', 'View doctor profiles', 'DOCTOR'),
('a1000000-0000-4000-8000-000000000013', 'DOCTOR_WRITE', 'Write doctors', 'Create and update doctors', 'DOCTOR'),
('a1000000-0000-4000-8000-000000000014', 'DOCTOR_DELETE', 'Delete doctors', 'Soft-delete doctors', 'DOCTOR'),
('a1000000-0000-4000-8000-000000000015', 'PATIENT_READ', 'Read patients', 'View patient records', 'PATIENT'),
('a1000000-0000-4000-8000-000000000016', 'PATIENT_WRITE', 'Write patients', 'Create and update patients', 'PATIENT'),
('a1000000-0000-4000-8000-000000000017', 'PATIENT_DELETE', 'Delete patients', 'Soft-delete patients', 'PATIENT'),
('a1000000-0000-4000-8000-000000000018', 'APPOINTMENT_READ', 'Read appointments', 'View appointments', 'APPOINTMENT'),
('a1000000-0000-4000-8000-000000000019', 'APPOINTMENT_WRITE', 'Write appointments', 'Create and update appointments', 'APPOINTMENT'),
('a1000000-0000-4000-8000-000000000020', 'APPOINTMENT_DELETE', 'Delete appointments', 'Cancel or soft-delete appointments', 'APPOINTMENT'),
('a1000000-0000-4000-8000-000000000021', 'VISIT_READ', 'Read visits', 'View clinical visits', 'VISIT'),
('a1000000-0000-4000-8000-000000000022', 'VISIT_WRITE', 'Write visits', 'Create and update visits', 'VISIT'),
('a1000000-0000-4000-8000-000000000023', 'VISIT_DELETE', 'Delete visits', 'Soft-delete visits', 'VISIT'),
('a1000000-0000-4000-8000-000000000024', 'PRESCRIPTION_READ', 'Read prescriptions', 'View prescriptions', 'PRESCRIPTION'),
('a1000000-0000-4000-8000-000000000025', 'PRESCRIPTION_CREATE', 'Create prescriptions', 'Create prescriptions', 'PRESCRIPTION'),
('a1000000-0000-4000-8000-000000000026', 'PRESCRIPTION_WRITE', 'Write prescriptions', 'Update prescriptions', 'PRESCRIPTION'),
('a1000000-0000-4000-8000-000000000027', 'PRESCRIPTION_DELETE', 'Delete prescriptions', 'Soft-delete prescriptions', 'PRESCRIPTION'),
('a1000000-0000-4000-8000-000000000028', 'LAB_READ', 'Read laboratory', 'View lab orders and results', 'LAB'),
('a1000000-0000-4000-8000-000000000029', 'LAB_WRITE', 'Write laboratory', 'Create and update lab data', 'LAB'),
('a1000000-0000-4000-8000-000000000030', 'LAB_DELETE', 'Delete laboratory', 'Soft-delete lab data', 'LAB'),
('a1000000-0000-4000-8000-000000000031', 'RADIOLOGY_READ', 'Read radiology', 'View radiology orders and reports', 'RADIOLOGY'),
('a1000000-0000-4000-8000-000000000032', 'RADIOLOGY_WRITE', 'Write radiology', 'Create and update radiology data', 'RADIOLOGY'),
('a1000000-0000-4000-8000-000000000033', 'RADIOLOGY_DELETE', 'Delete radiology', 'Soft-delete radiology data', 'RADIOLOGY'),
('a1000000-0000-4000-8000-000000000034', 'DOCUMENT_READ', 'Read documents', 'View medical documents', 'DOCUMENT'),
('a1000000-0000-4000-8000-000000000035', 'DOCUMENT_WRITE', 'Write documents', 'Upload and update documents', 'DOCUMENT'),
('a1000000-0000-4000-8000-000000000036', 'DOCUMENT_DELETE', 'Delete documents', 'Soft-delete documents', 'DOCUMENT'),
('a1000000-0000-4000-8000-000000000037', 'NOTIFICATION_READ', 'Read notifications', 'View notifications', 'NOTIFICATION'),
('a1000000-0000-4000-8000-000000000038', 'NOTIFICATION_WRITE', 'Write notifications', 'Create notifications', 'NOTIFICATION'),
('a1000000-0000-4000-8000-000000000039', 'AUDIT_READ', 'Read audit logs', 'View audit trail entries', 'AUDIT'),
('a1000000-0000-4000-8000-000000000040', 'DASHBOARD_READ', 'Read dashboards', 'View operational dashboards', 'DASHBOARD'),
('a1000000-0000-4000-8000-000000000041', 'REPORT_READ', 'Read reports', 'View analytics reports', 'REPORT');

-- Seed platform system roles (tenant_id NULL)
INSERT INTO roles (id, tenant_id, name, type, description, system_role) VALUES
('b1000000-0000-4000-8000-000000000001', NULL, 'Super Admin', 'SUPER_ADMIN', 'Platform-wide administrator', TRUE),
('b1000000-0000-4000-8000-000000000002', NULL, 'Hospital Admin', 'HOSPITAL_ADMIN', 'Hospital tenant administrator', TRUE),
('b1000000-0000-4000-8000-000000000003', NULL, 'Doctor', 'DOCTOR', 'Clinical doctor role', TRUE),
('b1000000-0000-4000-8000-000000000004', NULL, 'Nurse', 'NURSE', 'Nursing staff role', TRUE),
('b1000000-0000-4000-8000-000000000005', NULL, 'Receptionist', 'RECEPTIONIST', 'Front-desk receptionist role', TRUE),
('b1000000-0000-4000-8000-000000000006', NULL, 'Lab Technician', 'LAB_TECHNICIAN', 'Laboratory technician role', TRUE),
('b1000000-0000-4000-8000-000000000007', NULL, 'Pharmacist', 'PHARMACIST', 'Pharmacy staff role', TRUE),
('b1000000-0000-4000-8000-000000000008', NULL, 'Patient', 'PATIENT', 'Patient portal role', TRUE);

-- Grant every permission to Super Admin
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000001', id FROM permissions WHERE deleted = FALSE;
