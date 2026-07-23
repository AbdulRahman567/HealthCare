-- Align upgraded Hospital Admin grants with SystemRolePermissionMatrix.
-- V10 incorrectly granted HOSPITAL_DELETE to existing tenant HOSPITAL_ADMIN roles;
-- the matrix and TenantRoleProvisioner do not include that permission.

DELETE rp
FROM role_permissions rp
INNER JOIN roles r
        ON r.id = rp.role_id
       AND r.type = 'HOSPITAL_ADMIN'
       AND r.tenant_id IS NOT NULL
       AND r.deleted = FALSE
INNER JOIN permissions p
        ON p.id = rp.permission_id
       AND p.code = 'HOSPITAL_DELETE'
       AND p.deleted = FALSE;
