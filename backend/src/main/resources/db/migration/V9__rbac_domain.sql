-- Phase 3.1: RBAC domain refinement — permission group/action, role hierarchy, indexes.

-- ---------------------------------------------------------------------------
-- Permissions: rename module → permission_group, add action + system flag
-- ---------------------------------------------------------------------------

ALTER TABLE permissions
    CHANGE COLUMN module permission_group VARCHAR(80) NOT NULL;

ALTER TABLE permissions
    ADD COLUMN action VARCHAR(30) NULL AFTER permission_group,
    ADD COLUMN system_permission BOOLEAN NOT NULL DEFAULT TRUE AFTER action;

UPDATE permissions
SET action = SUBSTRING_INDEX(code, '_', -1)
WHERE action IS NULL;

ALTER TABLE permissions
    MODIFY COLUMN action VARCHAR(30) NOT NULL;

ALTER TABLE permissions
    ADD CONSTRAINT uk_permissions_group_action UNIQUE (permission_group, action);

DROP INDEX idx_permissions_module ON permissions;

CREATE INDEX idx_permissions_group ON permissions (permission_group);
CREATE INDEX idx_permissions_action ON permissions (action);
CREATE INDEX idx_permissions_group_action ON permissions (permission_group, action);
CREATE INDEX idx_permissions_system ON permissions (system_permission);

-- ---------------------------------------------------------------------------
-- Roles: hierarchy level, parent linkage, assignable flag
-- ---------------------------------------------------------------------------

ALTER TABLE roles
    ADD COLUMN hierarchy_level INT NOT NULL DEFAULT 100 AFTER system_role,
    ADD COLUMN assignable BOOLEAN NOT NULL DEFAULT TRUE AFTER hierarchy_level,
    ADD COLUMN parent_role_id CHAR(36) NULL AFTER assignable;

UPDATE roles
SET hierarchy_level = CASE type
    WHEN 'SUPER_ADMIN' THEN 0
    WHEN 'HOSPITAL_ADMIN' THEN 10
    WHEN 'DOCTOR' THEN 20
    WHEN 'NURSE' THEN 30
    WHEN 'RECEPTIONIST' THEN 30
    WHEN 'LAB_TECHNICIAN' THEN 30
    WHEN 'PHARMACIST' THEN 30
    WHEN 'PATIENT' THEN 40
    ELSE 100
END,
    assignable = CASE type
        WHEN 'SUPER_ADMIN' THEN FALSE
        WHEN 'PATIENT' THEN FALSE
        ELSE TRUE
    END;

-- Platform hierarchy: SUPER_ADMIN ← HOSPITAL_ADMIN ← operational / clinical / patient
UPDATE roles
SET parent_role_id = 'b1000000-0000-4000-8000-000000000001'
WHERE tenant_id IS NULL
  AND type = 'HOSPITAL_ADMIN'
  AND id = 'b1000000-0000-4000-8000-000000000002';

UPDATE roles
SET parent_role_id = 'b1000000-0000-4000-8000-000000000002'
WHERE tenant_id IS NULL
  AND type IN ('DOCTOR', 'NURSE', 'RECEPTIONIST', 'LAB_TECHNICIAN', 'PHARMACIST', 'PATIENT');

-- Tenant hierarchy: HOSPITAL_ADMIN is root; children point at that tenant's admin role
UPDATE roles child
    INNER JOIN roles parent
        ON parent.tenant_id = child.tenant_id
       AND parent.type = 'HOSPITAL_ADMIN'
       AND parent.deleted = FALSE
SET child.parent_role_id = parent.id
WHERE child.tenant_id IS NOT NULL
  AND child.type <> 'HOSPITAL_ADMIN'
  AND child.deleted = FALSE;

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_parent_role
        FOREIGN KEY (parent_role_id) REFERENCES roles (id);

CREATE INDEX idx_roles_parent_role_id ON roles (parent_role_id);
CREATE INDEX idx_roles_hierarchy_level ON roles (hierarchy_level);
CREATE INDEX idx_roles_tenant_hierarchy ON roles (tenant_id, hierarchy_level);
CREATE INDEX idx_roles_system_role ON roles (system_role);
CREATE INDEX idx_roles_assignable ON roles (assignable);

-- ---------------------------------------------------------------------------
-- Default system role permission grants (platform templates)
-- Hospital Admin grants already seeded in V3; Super Admin has all permissions.
-- ---------------------------------------------------------------------------

-- Doctor
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000003', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ', 'PATIENT_WRITE',
      'APPOINTMENT_READ', 'APPOINTMENT_WRITE',
      'VISIT_READ', 'VISIT_WRITE',
      'PRESCRIPTION_READ', 'PRESCRIPTION_CREATE', 'PRESCRIPTION_WRITE',
      'LAB_READ', 'RADIOLOGY_READ',
      'DOCUMENT_READ', 'DOCUMENT_WRITE',
      'DASHBOARD_READ'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = 'b1000000-0000-4000-8000-000000000003'
        AND rp.permission_id = p.id
  );

-- Nurse
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000004', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ',
      'APPOINTMENT_READ',
      'VISIT_READ', 'VISIT_WRITE',
      'PRESCRIPTION_READ',
      'LAB_READ',
      'DOCUMENT_READ',
      'NOTIFICATION_READ',
      'DASHBOARD_READ'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = 'b1000000-0000-4000-8000-000000000004'
        AND rp.permission_id = p.id
  );

-- Receptionist
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000005', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ', 'PATIENT_WRITE',
      'APPOINTMENT_READ', 'APPOINTMENT_WRITE', 'APPOINTMENT_DELETE',
      'DOCTOR_READ',
      'NOTIFICATION_READ', 'NOTIFICATION_WRITE',
      'DASHBOARD_READ'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = 'b1000000-0000-4000-8000-000000000005'
        AND rp.permission_id = p.id
  );

-- Lab Technician
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000006', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ',
      'LAB_READ', 'LAB_WRITE',
      'DOCUMENT_READ', 'DOCUMENT_WRITE',
      'DASHBOARD_READ'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = 'b1000000-0000-4000-8000-000000000006'
        AND rp.permission_id = p.id
  );

-- Pharmacist
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000007', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ',
      'PRESCRIPTION_READ', 'PRESCRIPTION_WRITE',
      'DOCUMENT_READ',
      'DASHBOARD_READ'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = 'b1000000-0000-4000-8000-000000000007'
        AND rp.permission_id = p.id
  );

-- Patient (portal-ready, minimal — no clinical write)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000008', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'APPOINTMENT_READ',
      'DOCUMENT_READ',
      'NOTIFICATION_READ',
      'DASHBOARD_READ'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = 'b1000000-0000-4000-8000-000000000008'
        AND rp.permission_id = p.id
  );
