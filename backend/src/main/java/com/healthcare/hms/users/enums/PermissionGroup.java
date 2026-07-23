package com.healthcare.hms.users.enums;

/**
 * Logical resource groups for RBAC permissions.
 *
 * <p>Permission codes follow {@code {GROUP}_{ACTION}} (see
 * {@link com.healthcare.hms.users.rbac.PermissionNaming}).
 */
public enum PermissionGroup {
    USER,
    ROLE,
    HOSPITAL,
    DEPARTMENT,
    DOCTOR,
    PATIENT,
    APPOINTMENT,
    VISIT,
    PRESCRIPTION,
    LAB,
    RADIOLOGY,
    DOCUMENT,
    NOTIFICATION,
    BILLING,
    AUDIT,
    DASHBOARD,
    REPORT
}
