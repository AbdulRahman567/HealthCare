-- Phase 6.9: Patient portal must not hold staff APPOINTMENT_READ.
-- Staff appointment APIs are tenant-wide; self-scoped patient portal APIs are not shipped yet.

DELETE rp
FROM role_permissions rp
INNER JOIN roles r
        ON r.id = rp.role_id
       AND r.type = 'PATIENT'
       AND r.deleted = FALSE
INNER JOIN permissions p
        ON p.id = rp.permission_id
       AND p.code = 'APPOINTMENT_READ'
       AND p.deleted = FALSE;
