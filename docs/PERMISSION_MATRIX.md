# Permission Matrix (Phase 3.5)

Canonical RBAC grants for default system roles in Healthcare HMS.

## Naming convention

Every permission code is:

```text
{PERMISSION_GROUP}_{PERMISSION_ACTION}
```

| Segment | Allowed values |
| ------- | -------------- |
| Group | `USER`, `ROLE`, `HOSPITAL`, `DEPARTMENT`, `DOCTOR`, `PATIENT`, `APPOINTMENT`, `VISIT`, `PRESCRIPTION`, `LAB`, `RADIOLOGY`, `DOCUMENT`, `NOTIFICATION`, `BILLING`, `AUDIT`, `DASHBOARD`, `REPORT` |
| Action | `READ`, `CREATE`, `UPDATE`, `DELETE` |

Examples:

- `PATIENT_READ`
- `PATIENT_CREATE`
- `PATIENT_UPDATE`
- `PATIENT_DELETE`
- `APPOINTMENT_READ`
- `BILLING_CREATE`

Source of truth in code:

- Catalog: `PermissionCatalog` / `PermissionConstants`
- Default grants: `SystemRolePermissionMatrix`
- Tenant provisioning: `DefaultTenantRoleCatalog` → `TenantRoleProvisioner`
- Platform seed: Flyway `V10__default_system_roles.sql` + `PlatformRbacBootstrap`

## Default system roles

| Role | Scope | Assignable |
| ---- | ----- | ---------- |
| Super Admin | Platform only | No |
| Hospital Admin | Platform template + per-tenant | Yes |
| Doctor | Platform template + per-tenant | Yes |
| Nurse | Platform template + per-tenant | Yes |
| Receptionist | Platform template + per-tenant | Yes |
| Lab Technician | Platform template + per-tenant | Yes |
| Pharmacist | Platform template + per-tenant | Yes |
| Accountant | Platform template + per-tenant | Yes |
| Patient | Platform portal template | No |

Hierarchy (structural only — permissions are explicit grants, not inherited):

```text
SUPER_ADMIN
  └── HOSPITAL_ADMIN
        ├── DOCTOR
        ├── NURSE
        ├── RECEPTIONIST
        ├── LAB_TECHNICIAN
        ├── PHARMACIST
        ├── ACCOUNTANT
        └── PATIENT
```

## Matrix

Legend: `✓` granted by default.

| Permission | Super Admin | Hospital Admin | Doctor | Nurse | Receptionist | Lab Tech | Pharmacist | Accountant |
| ---------- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| USER_READ | ✓ | ✓ | | | | | | |
| USER_CREATE | ✓ | ✓ | | | | | | |
| USER_UPDATE | ✓ | ✓ | | | | | | |
| USER_DELETE | ✓ | ✓ | | | | | | |
| ROLE_READ | ✓ | ✓ | | | | | | |
| ROLE_CREATE | ✓ | ✓ | | | | | | |
| ROLE_UPDATE | ✓ | ✓ | | | | | | |
| ROLE_DELETE | ✓ | | | | | | | |
| HOSPITAL_READ | ✓ | ✓ | | | | | | |
| HOSPITAL_CREATE | ✓ | | | | | | | |
| HOSPITAL_UPDATE | ✓ | ✓ | | | | | | |
| HOSPITAL_DELETE | ✓ | | | | | | | |
| DEPARTMENT_READ | ✓ | ✓ | | | | | | |
| DEPARTMENT_CREATE | ✓ | ✓ | | | | | | |
| DEPARTMENT_UPDATE | ✓ | ✓ | | | | | | |
| DEPARTMENT_DELETE | ✓ | ✓ | | | | | | |
| DOCTOR_READ | ✓ | ✓ | | | ✓ | | | |
| DOCTOR_CREATE | ✓ | ✓ | | | | | | |
| DOCTOR_UPDATE | ✓ | ✓ | | | | | | |
| DOCTOR_DELETE | ✓ | ✓ | | | | | | |
| PATIENT_READ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| PATIENT_CREATE | ✓ | ✓ | ✓ | | ✓ | | | |
| PATIENT_UPDATE | ✓ | ✓ | ✓ | | ✓ | | | |
| PATIENT_DELETE | ✓ | ✓ | | | | | | |
| APPOINTMENT_READ | ✓ | ✓ | ✓ | ✓ | ✓ | | | ✓ |
| APPOINTMENT_CREATE | ✓ | ✓ | ✓ | | ✓ | | | |
| APPOINTMENT_UPDATE | ✓ | ✓ | ✓ | | ✓ | | | |
| APPOINTMENT_DELETE | ✓ | ✓ | | | ✓ | | | |
| VISIT_READ | ✓ | ✓ | ✓ | ✓ | | | | |
| VISIT_CREATE | ✓ | ✓ | ✓ | ✓ | | | | |
| VISIT_UPDATE | ✓ | ✓ | ✓ | ✓ | | | | |
| VISIT_DELETE | ✓ | ✓ | | | | | | |
| PRESCRIPTION_READ | ✓ | ✓ | ✓ | ✓ | | | ✓ | |
| PRESCRIPTION_CREATE | ✓ | ✓ | ✓ | | | | | |
| PRESCRIPTION_UPDATE | ✓ | ✓ | ✓ | | | | ✓ | |
| PRESCRIPTION_DELETE | ✓ | ✓ | | | | | | |
| LAB_READ | ✓ | ✓ | ✓ | ✓ | | ✓ | | |
| LAB_CREATE | ✓ | ✓ | | | | ✓ | | |
| LAB_UPDATE | ✓ | ✓ | | | | ✓ | | |
| LAB_DELETE | ✓ | ✓ | | | | | | |
| RADIOLOGY_READ | ✓ | ✓ | ✓ | | | | | |
| RADIOLOGY_CREATE | ✓ | ✓ | | | | | | |
| RADIOLOGY_UPDATE | ✓ | ✓ | | | | | | |
| RADIOLOGY_DELETE | ✓ | ✓ | | | | | | |
| DOCUMENT_READ | ✓ | ✓ | ✓ | ✓ | | ✓ | ✓ | ✓ |
| DOCUMENT_CREATE | ✓ | ✓ | ✓ | | | ✓ | | |
| DOCUMENT_UPDATE | ✓ | ✓ | ✓ | | | ✓ | | |
| DOCUMENT_DELETE | ✓ | ✓ | | | | | | |
| NOTIFICATION_READ | ✓ | ✓ | | ✓ | ✓ | | | |
| NOTIFICATION_CREATE | ✓ | ✓ | | | ✓ | | | |
| NOTIFICATION_UPDATE | ✓ | ✓ | | | ✓ | | | |
| NOTIFICATION_DELETE | ✓ | ✓ | | | | | | |
| BILLING_READ | ✓ | ✓ | | | | | | ✓ |
| BILLING_CREATE | ✓ | ✓ | | | | | | ✓ |
| BILLING_UPDATE | ✓ | ✓ | | | | | | ✓ |
| BILLING_DELETE | ✓ | ✓ | | | | | | ✓ |
| AUDIT_READ | ✓ | ✓ | | | | | | |
| DASHBOARD_READ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| REPORT_READ | ✓ | ✓ | | | | | | ✓ |

Patient portal defaults (not shown above): `APPOINTMENT_READ`, `DOCUMENT_READ`, `NOTIFICATION_READ`, `DASHBOARD_READ`.

## Seeding

1. **Flyway `V10__default_system_roles.sql`**
   - Renames legacy `*_WRITE` → `*_UPDATE`
   - Inserts missing `CREATE` / `BILLING` / `NOTIFICATION_DELETE` / `HOSPITAL_DELETE` permissions
   - Adds platform `ACCOUNTANT` role
   - Rebuilds platform `role_permissions` from this matrix
   - Adds `ACCOUNTANT` (+ grants) for existing hospital tenants
2. **Startup `PlatformRbacBootstrap`**
   - Idempotently ensures the platform catalog and system-role grants match Java matrices
3. **Hospital registration**
   - `TenantRoleProvisioner` creates tenant copies of Hospital Admin, Doctor, Nurse, Receptionist, Lab Technician, Pharmacist, and Accountant with matrix grants
