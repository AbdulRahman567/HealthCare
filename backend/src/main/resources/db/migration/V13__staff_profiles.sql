-- Phase 4.3: Staff management profiles + STAFF RBAC permissions.

-- ---------------------------------------------------------------------------
-- 1. Staff profile tables
-- ---------------------------------------------------------------------------

CREATE TABLE doctors (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    hospital_id         CHAR(36)      NOT NULL,
    user_id             CHAR(36)      NOT NULL,
    department_id       CHAR(36)      NULL,
    reports_to_staff_id CHAR(36)      NULL,
    employee_code       VARCHAR(50)   NOT NULL,
    job_title           VARCHAR(150)  NULL,
    employment_status   VARCHAR(30)   NOT NULL,
    employment_type     VARCHAR(30)   NOT NULL,
    hired_at            DATE          NULL,
    terminated_at       DATE          NULL,
    specialization      VARCHAR(150)  NOT NULL,
    license_number      VARCHAR(100)  NOT NULL,
    qualification       VARCHAR(255)  NULL,
    experience_years    INT           NULL,
    consultation_fee    DECIMAL(12,2) NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_doctors PRIMARY KEY (id),
    CONSTRAINT uk_doctors_tenant_employee_code UNIQUE (tenant_id, employee_code),
    CONSTRAINT uk_doctors_tenant_user UNIQUE (tenant_id, user_id),
    CONSTRAINT uk_doctors_tenant_license UNIQUE (tenant_id, license_number),
    CONSTRAINT fk_doctors_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_doctors_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_doctors_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_doctors_tenant_id ON doctors (tenant_id);
CREATE INDEX idx_doctors_hospital_id ON doctors (hospital_id);
CREATE INDEX idx_doctors_department_id ON doctors (department_id);
CREATE INDEX idx_doctors_user_id ON doctors (user_id);
CREATE INDEX idx_doctors_employment_status ON doctors (tenant_id, employment_status);
CREATE INDEX idx_doctors_deleted ON doctors (deleted);

CREATE TABLE nurses (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    hospital_id         CHAR(36)      NOT NULL,
    user_id             CHAR(36)      NOT NULL,
    department_id       CHAR(36)      NULL,
    reports_to_staff_id CHAR(36)      NULL,
    employee_code       VARCHAR(50)   NOT NULL,
    job_title           VARCHAR(150)  NULL,
    employment_status   VARCHAR(30)   NOT NULL,
    employment_type     VARCHAR(30)   NOT NULL,
    hired_at            DATE          NULL,
    terminated_at       DATE          NULL,
    shift               VARCHAR(30)   NOT NULL,
    qualification       VARCHAR(255)  NULL,
    license_number      VARCHAR(100)  NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_nurses PRIMARY KEY (id),
    CONSTRAINT uk_nurses_tenant_employee_code UNIQUE (tenant_id, employee_code),
    CONSTRAINT uk_nurses_tenant_user UNIQUE (tenant_id, user_id),
    CONSTRAINT fk_nurses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_nurses_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_nurses_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_nurses_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_nurses_tenant_id ON nurses (tenant_id);
CREATE INDEX idx_nurses_hospital_id ON nurses (hospital_id);
CREATE INDEX idx_nurses_department_id ON nurses (department_id);
CREATE INDEX idx_nurses_user_id ON nurses (user_id);
CREATE INDEX idx_nurses_employment_status ON nurses (tenant_id, employment_status);
CREATE INDEX idx_nurses_deleted ON nurses (deleted);

CREATE TABLE receptionists (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    hospital_id         CHAR(36)      NOT NULL,
    user_id             CHAR(36)      NOT NULL,
    department_id       CHAR(36)      NULL,
    reports_to_staff_id CHAR(36)      NULL,
    employee_code       VARCHAR(50)   NOT NULL,
    job_title           VARCHAR(150)  NULL,
    employment_status   VARCHAR(30)   NOT NULL,
    employment_type     VARCHAR(30)   NOT NULL,
    hired_at            DATE          NULL,
    terminated_at       DATE          NULL,
    desk_location       VARCHAR(150)  NULL,
    languages           VARCHAR(255)  NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_receptionists PRIMARY KEY (id),
    CONSTRAINT uk_receptionists_tenant_employee_code UNIQUE (tenant_id, employee_code),
    CONSTRAINT uk_receptionists_tenant_user UNIQUE (tenant_id, user_id),
    CONSTRAINT fk_receptionists_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_receptionists_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_receptionists_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_receptionists_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_receptionists_tenant_id ON receptionists (tenant_id);
CREATE INDEX idx_receptionists_hospital_id ON receptionists (hospital_id);
CREATE INDEX idx_receptionists_department_id ON receptionists (department_id);
CREATE INDEX idx_receptionists_user_id ON receptionists (user_id);
CREATE INDEX idx_receptionists_employment_status ON receptionists (tenant_id, employment_status);
CREATE INDEX idx_receptionists_deleted ON receptionists (deleted);

CREATE TABLE laboratory_staff (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    hospital_id         CHAR(36)      NOT NULL,
    user_id             CHAR(36)      NOT NULL,
    department_id       CHAR(36)      NULL,
    reports_to_staff_id CHAR(36)      NULL,
    employee_code       VARCHAR(50)   NOT NULL,
    job_title           VARCHAR(150)  NULL,
    employment_status   VARCHAR(30)   NOT NULL,
    employment_type     VARCHAR(30)   NOT NULL,
    hired_at            DATE          NULL,
    terminated_at       DATE          NULL,
    specialty_area      VARCHAR(150)  NULL,
    license_number      VARCHAR(100)  NULL,
    certification       VARCHAR(255)  NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_laboratory_staff PRIMARY KEY (id),
    CONSTRAINT uk_laboratory_staff_tenant_employee_code UNIQUE (tenant_id, employee_code),
    CONSTRAINT uk_laboratory_staff_tenant_user UNIQUE (tenant_id, user_id),
    CONSTRAINT fk_laboratory_staff_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_laboratory_staff_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_laboratory_staff_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_laboratory_staff_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_laboratory_staff_tenant_id ON laboratory_staff (tenant_id);
CREATE INDEX idx_laboratory_staff_hospital_id ON laboratory_staff (hospital_id);
CREATE INDEX idx_laboratory_staff_department_id ON laboratory_staff (department_id);
CREATE INDEX idx_laboratory_staff_user_id ON laboratory_staff (user_id);
CREATE INDEX idx_laboratory_staff_employment_status ON laboratory_staff (tenant_id, employment_status);
CREATE INDEX idx_laboratory_staff_deleted ON laboratory_staff (deleted);

CREATE TABLE pharmacists (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    hospital_id         CHAR(36)      NOT NULL,
    user_id             CHAR(36)      NOT NULL,
    department_id       CHAR(36)      NULL,
    reports_to_staff_id CHAR(36)      NULL,
    employee_code       VARCHAR(50)   NOT NULL,
    job_title           VARCHAR(150)  NULL,
    employment_status   VARCHAR(30)   NOT NULL,
    employment_type     VARCHAR(30)   NOT NULL,
    hired_at            DATE          NULL,
    terminated_at       DATE          NULL,
    license_number      VARCHAR(100)  NOT NULL,
    pharmacy_location   VARCHAR(150)  NULL,
    qualification       VARCHAR(255)  NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pharmacists PRIMARY KEY (id),
    CONSTRAINT uk_pharmacists_tenant_employee_code UNIQUE (tenant_id, employee_code),
    CONSTRAINT uk_pharmacists_tenant_user UNIQUE (tenant_id, user_id),
    CONSTRAINT uk_pharmacists_tenant_license UNIQUE (tenant_id, license_number),
    CONSTRAINT fk_pharmacists_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_pharmacists_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_pharmacists_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_pharmacists_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pharmacists_tenant_id ON pharmacists (tenant_id);
CREATE INDEX idx_pharmacists_hospital_id ON pharmacists (hospital_id);
CREATE INDEX idx_pharmacists_department_id ON pharmacists (department_id);
CREATE INDEX idx_pharmacists_user_id ON pharmacists (user_id);
CREATE INDEX idx_pharmacists_employment_status ON pharmacists (tenant_id, employment_status);
CREATE INDEX idx_pharmacists_deleted ON pharmacists (deleted);

-- ---------------------------------------------------------------------------
-- 2. STAFF permission catalog + HOSPITAL_ADMIN grants
-- ---------------------------------------------------------------------------

INSERT INTO permissions (id, code, name, description, permission_group, action, system_permission)
SELECT * FROM (
    SELECT 'a1000000-0000-4000-8000-000000000070' AS id, 'STAFF_READ' AS code, 'Read staff' AS name,
           'View operational staff profiles' AS description, 'STAFF' AS permission_group, 'READ' AS action, TRUE AS system_permission
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000071', 'STAFF_CREATE', 'Create staff',
           'Create operational staff profiles', 'STAFF', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000072', 'STAFF_UPDATE', 'Update staff',
           'Update operational staff profiles', 'STAFF', 'UPDATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000073', 'STAFF_DELETE', 'Delete staff',
           'Soft-delete operational staff profiles', 'STAFF', 'DELETE', TRUE
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = seed.code AND p.deleted = FALSE
);

-- Platform HOSPITAL_ADMIN system role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000002', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN ('STAFF_READ', 'STAFF_CREATE', 'STAFF_UPDATE', 'STAFF_DELETE')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = 'b1000000-0000-4000-8000-000000000002'
        AND rp.permission_id = p.id
  );

-- Platform SUPER_ADMIN already gets all via bootstrap; also grant explicitly for DB consistency
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000001', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN ('STAFF_READ', 'STAFF_CREATE', 'STAFF_UPDATE', 'STAFF_DELETE')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = 'b1000000-0000-4000-8000-000000000001'
        AND rp.permission_id = p.id
  );

-- Existing tenant HOSPITAL_ADMIN roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.type = 'HOSPITAL_ADMIN'
  AND r.tenant_id IS NOT NULL
  AND r.deleted = FALSE
  AND p.deleted = FALSE
  AND p.code IN ('STAFF_READ', 'STAFF_CREATE', 'STAFF_UPDATE', 'STAFF_DELETE')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = p.id
  );
