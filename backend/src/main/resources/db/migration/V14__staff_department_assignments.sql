-- Phase 4.4: Staff ↔ department assignment history + department head as Staff reference.

-- ---------------------------------------------------------------------------
-- 1. Assignment history (append-only; open row = ended_at IS NULL)
-- ---------------------------------------------------------------------------

CREATE TABLE staff_department_assignments (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    hospital_id         CHAR(36)      NOT NULL,
    staff_type          VARCHAR(30)   NOT NULL,
    staff_id            CHAR(36)      NOT NULL,
    department_id       CHAR(36)      NOT NULL,
    from_department_id  CHAR(36)      NULL,
    action              VARCHAR(30)   NOT NULL,
    reason              VARCHAR(500)  NULL,
    assigned_at         TIMESTAMP(6)  NOT NULL,
    ended_at            TIMESTAMP(6)  NULL,
    assigned_by         CHAR(36)      NULL,
    ended_by            CHAR(36)      NULL,
    -- MySQL UNIQUE treats NULL as distinct; only one open row per staff.
    open_slot           CHAR(1)       GENERATED ALWAYS AS (
                            CASE WHEN ended_at IS NULL THEN 'Y' ELSE NULL END
                        ) STORED,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_staff_department_assignments PRIMARY KEY (id),
    CONSTRAINT uk_staff_open_assignment UNIQUE (tenant_id, staff_type, staff_id, open_slot),
    CONSTRAINT fk_sda_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sda_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_sda_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_sda_from_department FOREIGN KEY (from_department_id) REFERENCES departments (id),
    CONSTRAINT fk_sda_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id),
    CONSTRAINT fk_sda_ended_by FOREIGN KEY (ended_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sda_tenant_id ON staff_department_assignments (tenant_id);
CREATE INDEX idx_sda_staff ON staff_department_assignments (tenant_id, staff_type, staff_id);
CREATE INDEX idx_sda_department ON staff_department_assignments (tenant_id, department_id);
CREATE INDEX idx_sda_assigned_at ON staff_department_assignments (tenant_id, assigned_at);
CREATE INDEX idx_sda_deleted ON staff_department_assignments (deleted);

-- ---------------------------------------------------------------------------
-- 2. Department head → Staff specialization (keep head_user_id in sync)
-- ---------------------------------------------------------------------------

ALTER TABLE departments
    ADD COLUMN head_staff_id   CHAR(36)    NULL AFTER head_user_id,
    ADD COLUMN head_staff_type VARCHAR(30) NULL AFTER head_staff_id;

CREATE INDEX idx_departments_head_staff_id ON departments (head_staff_id);
