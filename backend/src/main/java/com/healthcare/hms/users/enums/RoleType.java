package com.healthcare.hms.users.enums;

/**
 * System role types supported by HMS RBAC.
 *
 * <p>Hierarchy and assignability are defined in
 * {@link com.healthcare.hms.users.rbac.RoleHierarchy}.
 *
 * <p>Default permission grants for each type live in
 * {@link com.healthcare.hms.users.rbac.SystemRolePermissionMatrix} (Phase 3.5).
 */
public enum RoleType {
    SUPER_ADMIN,
    HOSPITAL_ADMIN,
    DOCTOR,
    NURSE,
    RECEPTIONIST,
    LAB_TECHNICIAN,
    PHARMACIST,
    ACCOUNTANT,
    PATIENT;

    /**
     * String constants usable in annotation attributes.
     */
    public static final class Names {
        public static final String SUPER_ADMIN = "SUPER_ADMIN";
        public static final String HOSPITAL_ADMIN = "HOSPITAL_ADMIN";
        public static final String DOCTOR = "DOCTOR";
        public static final String NURSE = "NURSE";
        public static final String RECEPTIONIST = "RECEPTIONIST";
        public static final String LAB_TECHNICIAN = "LAB_TECHNICIAN";
        public static final String PHARMACIST = "PHARMACIST";
        public static final String ACCOUNTANT = "ACCOUNTANT";
        public static final String PATIENT = "PATIENT";

        private Names() {
        }
    }
}
