package com.healthcare.hms.users.enums;

/**
 * System role types supported by HMS RBAC.
 */
public enum RoleType {
    SUPER_ADMIN,
    HOSPITAL_ADMIN,
    DOCTOR,
    NURSE,
    RECEPTIONIST,
    LAB_TECHNICIAN,
    PHARMACIST,
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
        public static final String PATIENT = "PATIENT";

        private Names() {
        }
    }
}
