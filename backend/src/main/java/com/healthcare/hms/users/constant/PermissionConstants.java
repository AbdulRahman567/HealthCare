package com.healthcare.hms.users.constant;

/**
 * Canonical permission codes used across RBAC assignment and authorization checks.
 */
public final class PermissionConstants {

    public static final String USER_READ = "USER_READ";
    public static final String USER_WRITE = "USER_WRITE";
    public static final String USER_DELETE = "USER_DELETE";

    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_WRITE = "ROLE_WRITE";
    public static final String ROLE_DELETE = "ROLE_DELETE";

    public static final String HOSPITAL_READ = "HOSPITAL_READ";
    public static final String HOSPITAL_WRITE = "HOSPITAL_WRITE";

    public static final String DEPARTMENT_READ = "DEPARTMENT_READ";
    public static final String DEPARTMENT_WRITE = "DEPARTMENT_WRITE";
    public static final String DEPARTMENT_DELETE = "DEPARTMENT_DELETE";

    public static final String DOCTOR_READ = "DOCTOR_READ";
    public static final String DOCTOR_WRITE = "DOCTOR_WRITE";
    public static final String DOCTOR_DELETE = "DOCTOR_DELETE";

    public static final String PATIENT_READ = "PATIENT_READ";
    public static final String PATIENT_WRITE = "PATIENT_WRITE";
    public static final String PATIENT_DELETE = "PATIENT_DELETE";

    public static final String APPOINTMENT_READ = "APPOINTMENT_READ";
    public static final String APPOINTMENT_WRITE = "APPOINTMENT_WRITE";
    public static final String APPOINTMENT_DELETE = "APPOINTMENT_DELETE";

    public static final String VISIT_READ = "VISIT_READ";
    public static final String VISIT_WRITE = "VISIT_WRITE";
    public static final String VISIT_DELETE = "VISIT_DELETE";

    public static final String PRESCRIPTION_READ = "PRESCRIPTION_READ";
    public static final String PRESCRIPTION_CREATE = "PRESCRIPTION_CREATE";
    public static final String PRESCRIPTION_WRITE = "PRESCRIPTION_WRITE";
    public static final String PRESCRIPTION_DELETE = "PRESCRIPTION_DELETE";

    public static final String LAB_READ = "LAB_READ";
    public static final String LAB_WRITE = "LAB_WRITE";
    public static final String LAB_DELETE = "LAB_DELETE";

    public static final String RADIOLOGY_READ = "RADIOLOGY_READ";
    public static final String RADIOLOGY_WRITE = "RADIOLOGY_WRITE";
    public static final String RADIOLOGY_DELETE = "RADIOLOGY_DELETE";

    public static final String DOCUMENT_READ = "DOCUMENT_READ";
    public static final String DOCUMENT_WRITE = "DOCUMENT_WRITE";
    public static final String DOCUMENT_DELETE = "DOCUMENT_DELETE";

    public static final String NOTIFICATION_READ = "NOTIFICATION_READ";
    public static final String NOTIFICATION_WRITE = "NOTIFICATION_WRITE";

    public static final String AUDIT_READ = "AUDIT_READ";
    public static final String DASHBOARD_READ = "DASHBOARD_READ";
    public static final String REPORT_READ = "REPORT_READ";

    private PermissionConstants() {
    }
}
