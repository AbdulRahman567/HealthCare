-- Phase 3.5: Default system roles — canonicalize CREATE/UPDATE, BILLING, ACCOUNTANT.
-- Naming convention: {GROUP}_{READ|CREATE|UPDATE|DELETE}

-- ---------------------------------------------------------------------------
-- 1. Rename legacy WRITE → UPDATE (keep same permission rows / FKs)
-- ---------------------------------------------------------------------------

UPDATE permissions
SET code = CONCAT(permission_group, '_UPDATE'),
    action = 'UPDATE',
    name = CASE
              WHEN name LIKE 'Write %' THEN CONCAT('Update ', SUBSTRING(name, 7))
              ELSE name
          END,
    description = CASE
                      WHEN description LIKE 'Create and update %'
                          THEN CONCAT('Update ', SUBSTRING(description, 18))
                      WHEN description LIKE 'Create %' AND description NOT LIKE 'Create and %'
                          THEN description
                      ELSE description
                  END
WHERE action = 'WRITE'
  AND deleted = FALSE;

-- ---------------------------------------------------------------------------
-- 2. Seed missing CREATE / UPDATE / DELETE / BILLING catalog rows
-- ---------------------------------------------------------------------------

INSERT INTO permissions (id, code, name, description, permission_group, action, system_permission)
SELECT * FROM (
    SELECT 'a1000000-0000-4000-8000-000000000050' AS id, 'USER_CREATE' AS code, 'Create users' AS name,
           'Create user accounts' AS description, 'USER' AS permission_group, 'CREATE' AS action, TRUE AS system_permission
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000051', 'ROLE_CREATE', 'Create roles',
           'Create custom roles', 'ROLE', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000052', 'HOSPITAL_CREATE', 'Create hospitals',
           'Create hospital profiles', 'HOSPITAL', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000053', 'HOSPITAL_DELETE', 'Delete hospitals',
           'Soft-delete hospitals', 'HOSPITAL', 'DELETE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000054', 'DEPARTMENT_CREATE', 'Create departments',
           'Create departments', 'DEPARTMENT', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000055', 'DOCTOR_CREATE', 'Create doctors',
           'Create doctor profiles', 'DOCTOR', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000056', 'PATIENT_CREATE', 'Create patients',
           'Register patients', 'PATIENT', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000057', 'APPOINTMENT_CREATE', 'Create appointments',
           'Schedule appointments', 'APPOINTMENT', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000058', 'VISIT_CREATE', 'Create visits',
           'Open clinical visits', 'VISIT', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000059', 'LAB_CREATE', 'Create laboratory',
           'Create lab orders', 'LAB', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000060', 'RADIOLOGY_CREATE', 'Create radiology',
           'Create radiology orders', 'RADIOLOGY', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000061', 'DOCUMENT_CREATE', 'Create documents',
           'Upload medical documents', 'DOCUMENT', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000062', 'NOTIFICATION_CREATE', 'Create notifications',
           'Send notifications', 'NOTIFICATION', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000063', 'NOTIFICATION_DELETE', 'Delete notifications',
           'Soft-delete notifications', 'NOTIFICATION', 'DELETE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000064', 'BILLING_READ', 'Read billing',
           'View invoices and billing records', 'BILLING', 'READ', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000065', 'BILLING_CREATE', 'Create billing',
           'Create invoices and charges', 'BILLING', 'CREATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000066', 'BILLING_UPDATE', 'Update billing',
           'Update invoices and payments', 'BILLING', 'UPDATE', TRUE
    UNION ALL SELECT 'a1000000-0000-4000-8000-000000000067', 'BILLING_DELETE', 'Delete billing',
           'Void or soft-delete billing records', 'BILLING', 'DELETE', TRUE
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = seed.code AND p.deleted = FALSE
);

-- Refresh names/descriptions for renamed UPDATE rows where still legacy-styled
UPDATE permissions
SET name = CONCAT('Update ', LOWER(SUBSTRING(permission_group, 1, 1)), SUBSTRING(LOWER(permission_group), 2), 's'),
    description = CONCAT('Update ', LOWER(permission_group), ' records')
WHERE action = 'UPDATE'
  AND name LIKE 'Write %';

-- ---------------------------------------------------------------------------
-- 3. Platform ACCOUNTANT system role
-- ---------------------------------------------------------------------------

INSERT INTO roles (id, tenant_id, name, type, description, system_role, hierarchy_level, assignable, parent_role_id)
SELECT 'b1000000-0000-4000-8000-000000000009',
       NULL,
       'Accountant',
       'ACCOUNTANT',
       'Hospital billing and finance role',
       TRUE,
       30,
       TRUE,
       'b1000000-0000-4000-8000-000000000002'
WHERE NOT EXISTS (
    SELECT 1 FROM roles r
    WHERE r.tenant_id IS NULL
      AND r.type = 'ACCOUNTANT'
      AND r.deleted = FALSE
);

-- Hierarchy for ACCOUNTANT under platform Hospital Admin (existing tenants' copies below)
UPDATE roles
SET hierarchy_level = 30,
    assignable = TRUE,
    parent_role_id = 'b1000000-0000-4000-8000-000000000002'
WHERE tenant_id IS NULL
  AND type = 'ACCOUNTANT'
  AND deleted = FALSE;

-- ---------------------------------------------------------------------------
-- 4. Rebuild platform system-role grants from Phase 3.5 matrix
-- ---------------------------------------------------------------------------

-- Super Admin → all permissions
DELETE FROM role_permissions
WHERE role_id = 'b1000000-0000-4000-8000-000000000001';

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000001', p.id
FROM permissions p
WHERE p.deleted = FALSE;

-- Helper pattern: replace grants for a platform role
DELETE rp FROM role_permissions rp
WHERE rp.role_id IN (
    'b1000000-0000-4000-8000-000000000002',
    'b1000000-0000-4000-8000-000000000003',
    'b1000000-0000-4000-8000-000000000004',
    'b1000000-0000-4000-8000-000000000005',
    'b1000000-0000-4000-8000-000000000006',
    'b1000000-0000-4000-8000-000000000007',
    'b1000000-0000-4000-8000-000000000008',
    'b1000000-0000-4000-8000-000000000009'
);

-- Hospital Admin
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000002', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'USER_READ', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE',
      'ROLE_READ', 'ROLE_CREATE', 'ROLE_UPDATE',
      'HOSPITAL_READ', 'HOSPITAL_UPDATE',
      'DEPARTMENT_READ', 'DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE', 'DEPARTMENT_DELETE',
      'DOCTOR_READ', 'DOCTOR_CREATE', 'DOCTOR_UPDATE', 'DOCTOR_DELETE',
      'PATIENT_READ', 'PATIENT_CREATE', 'PATIENT_UPDATE', 'PATIENT_DELETE',
      'APPOINTMENT_READ', 'APPOINTMENT_CREATE', 'APPOINTMENT_UPDATE', 'APPOINTMENT_DELETE',
      'VISIT_READ', 'VISIT_CREATE', 'VISIT_UPDATE', 'VISIT_DELETE',
      'PRESCRIPTION_READ', 'PRESCRIPTION_CREATE', 'PRESCRIPTION_UPDATE', 'PRESCRIPTION_DELETE',
      'LAB_READ', 'LAB_CREATE', 'LAB_UPDATE', 'LAB_DELETE',
      'RADIOLOGY_READ', 'RADIOLOGY_CREATE', 'RADIOLOGY_UPDATE', 'RADIOLOGY_DELETE',
      'DOCUMENT_READ', 'DOCUMENT_CREATE', 'DOCUMENT_UPDATE', 'DOCUMENT_DELETE',
      'NOTIFICATION_READ', 'NOTIFICATION_CREATE', 'NOTIFICATION_UPDATE', 'NOTIFICATION_DELETE',
      'BILLING_READ', 'BILLING_CREATE', 'BILLING_UPDATE', 'BILLING_DELETE',
      'AUDIT_READ', 'DASHBOARD_READ', 'REPORT_READ'
  );

-- Doctor
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000003', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ', 'PATIENT_CREATE', 'PATIENT_UPDATE',
      'APPOINTMENT_READ', 'APPOINTMENT_CREATE', 'APPOINTMENT_UPDATE',
      'VISIT_READ', 'VISIT_CREATE', 'VISIT_UPDATE',
      'PRESCRIPTION_READ', 'PRESCRIPTION_CREATE', 'PRESCRIPTION_UPDATE',
      'LAB_READ', 'RADIOLOGY_READ',
      'DOCUMENT_READ', 'DOCUMENT_CREATE', 'DOCUMENT_UPDATE',
      'DASHBOARD_READ'
  );

-- Nurse
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000004', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ',
      'APPOINTMENT_READ',
      'VISIT_READ', 'VISIT_CREATE', 'VISIT_UPDATE',
      'PRESCRIPTION_READ',
      'LAB_READ',
      'DOCUMENT_READ',
      'NOTIFICATION_READ',
      'DASHBOARD_READ'
  );

-- Receptionist
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000005', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ', 'PATIENT_CREATE', 'PATIENT_UPDATE',
      'APPOINTMENT_READ', 'APPOINTMENT_CREATE', 'APPOINTMENT_UPDATE', 'APPOINTMENT_DELETE',
      'DOCTOR_READ',
      'NOTIFICATION_READ', 'NOTIFICATION_CREATE', 'NOTIFICATION_UPDATE',
      'DASHBOARD_READ'
  );

-- Lab Technician
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000006', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ',
      'LAB_READ', 'LAB_CREATE', 'LAB_UPDATE',
      'DOCUMENT_READ', 'DOCUMENT_CREATE', 'DOCUMENT_UPDATE',
      'DASHBOARD_READ'
  );

-- Pharmacist
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000007', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'PATIENT_READ',
      'PRESCRIPTION_READ', 'PRESCRIPTION_UPDATE',
      'DOCUMENT_READ',
      'DASHBOARD_READ'
  );

-- Patient (portal)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000008', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'APPOINTMENT_READ',
      'DOCUMENT_READ',
      'NOTIFICATION_READ',
      'DASHBOARD_READ'
  );

-- Accountant
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b1000000-0000-4000-8000-000000000009', p.id
FROM permissions p
WHERE p.deleted = FALSE
  AND p.code IN (
      'BILLING_READ', 'BILLING_CREATE', 'BILLING_UPDATE', 'BILLING_DELETE',
      'PATIENT_READ',
      'APPOINTMENT_READ',
      'DOCUMENT_READ',
      'REPORT_READ',
      'DASHBOARD_READ'
  );

-- ---------------------------------------------------------------------------
-- 5. Existing tenants: add ACCOUNTANT role + grant new CREATE/BILLING codes
-- ---------------------------------------------------------------------------

INSERT INTO roles (id, tenant_id, name, type, description, system_role, hierarchy_level, assignable, parent_role_id)
SELECT UUID(),
       admin.tenant_id,
       'Accountant',
       'ACCOUNTANT',
       'Hospital billing and finance role',
       FALSE,
       30,
       TRUE,
       admin.id
FROM roles admin
WHERE admin.type = 'HOSPITAL_ADMIN'
  AND admin.tenant_id IS NOT NULL
  AND admin.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM roles existing
      WHERE existing.tenant_id = admin.tenant_id
        AND existing.type = 'ACCOUNTANT'
        AND existing.deleted = FALSE
  );

-- Grant CREATE to tenant roles that already hold the matching UPDATE permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT rp.role_id, create_perm.id
FROM role_permissions rp
INNER JOIN permissions upd
        ON upd.id = rp.permission_id
       AND upd.action = 'UPDATE'
       AND upd.deleted = FALSE
INNER JOIN permissions create_perm
        ON create_perm.permission_group = upd.permission_group
       AND create_perm.action = 'CREATE'
       AND create_perm.deleted = FALSE
INNER JOIN roles r
        ON r.id = rp.role_id
       AND r.tenant_id IS NOT NULL
       AND r.deleted = FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions existing
    WHERE existing.role_id = rp.role_id
      AND existing.permission_id = create_perm.id
);

-- Hospital Admin tenant roles: billing suite
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.type = 'HOSPITAL_ADMIN'
  AND r.tenant_id IS NOT NULL
  AND r.deleted = FALSE
  AND p.deleted = FALSE
  AND p.code IN ('BILLING_READ', 'BILLING_CREATE', 'BILLING_UPDATE', 'BILLING_DELETE',
                 'ROLE_CREATE', 'ROLE_UPDATE', 'NOTIFICATION_DELETE', 'HOSPITAL_DELETE')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions existing
      WHERE existing.role_id = r.id
        AND existing.permission_id = p.id
  );

-- Accountant tenant roles: matrix grants
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.type = 'ACCOUNTANT'
  AND r.tenant_id IS NOT NULL
  AND r.deleted = FALSE
  AND p.deleted = FALSE
  AND p.code IN (
      'BILLING_READ', 'BILLING_CREATE', 'BILLING_UPDATE', 'BILLING_DELETE',
      'PATIENT_READ', 'APPOINTMENT_READ', 'DOCUMENT_READ', 'REPORT_READ', 'DASHBOARD_READ'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions existing
      WHERE existing.role_id = r.id
        AND existing.permission_id = p.id
  );
