package com.healthcare.hms.users.constant;

import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import com.healthcare.hms.users.rbac.PermissionNaming;

/**
 * Canonical permission codes used across RBAC assignment and authorization checks.
 *
 * <p><strong>Naming convention (Phase 3.5):</strong> {@code {PermissionGroup}_{PermissionAction}}
 * — {@code READ}, {@code CREATE}, {@code UPDATE}, {@code DELETE}.
 *
 * <p>Examples: {@code PATIENT_READ}, {@code PATIENT_CREATE}, {@code PATIENT_UPDATE},
 * {@code PATIENT_DELETE}, {@code APPOINTMENT_READ}.
 */
public final class PermissionConstants {

    public static final String USER_READ = "USER_READ";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";

    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";

    public static final String HOSPITAL_READ = "HOSPITAL_READ";
    public static final String HOSPITAL_CREATE = "HOSPITAL_CREATE";
    public static final String HOSPITAL_UPDATE = "HOSPITAL_UPDATE";
    public static final String HOSPITAL_DELETE = "HOSPITAL_DELETE";

    public static final String DEPARTMENT_READ = "DEPARTMENT_READ";
    public static final String DEPARTMENT_CREATE = "DEPARTMENT_CREATE";
    public static final String DEPARTMENT_UPDATE = "DEPARTMENT_UPDATE";
    public static final String DEPARTMENT_DELETE = "DEPARTMENT_DELETE";

    public static final String DOCTOR_READ = "DOCTOR_READ";
    public static final String DOCTOR_CREATE = "DOCTOR_CREATE";
    public static final String DOCTOR_UPDATE = "DOCTOR_UPDATE";
    public static final String DOCTOR_DELETE = "DOCTOR_DELETE";

    public static final String PATIENT_READ = "PATIENT_READ";
    public static final String PATIENT_CREATE = "PATIENT_CREATE";
    public static final String PATIENT_UPDATE = "PATIENT_UPDATE";
    public static final String PATIENT_DELETE = "PATIENT_DELETE";

    public static final String APPOINTMENT_READ = "APPOINTMENT_READ";
    public static final String APPOINTMENT_CREATE = "APPOINTMENT_CREATE";
    public static final String APPOINTMENT_UPDATE = "APPOINTMENT_UPDATE";
    public static final String APPOINTMENT_DELETE = "APPOINTMENT_DELETE";

    public static final String VISIT_READ = "VISIT_READ";
    public static final String VISIT_CREATE = "VISIT_CREATE";
    public static final String VISIT_UPDATE = "VISIT_UPDATE";
    public static final String VISIT_DELETE = "VISIT_DELETE";

    public static final String PRESCRIPTION_READ = "PRESCRIPTION_READ";
    public static final String PRESCRIPTION_CREATE = "PRESCRIPTION_CREATE";
    public static final String PRESCRIPTION_UPDATE = "PRESCRIPTION_UPDATE";
    public static final String PRESCRIPTION_DELETE = "PRESCRIPTION_DELETE";

    public static final String LAB_READ = "LAB_READ";
    public static final String LAB_CREATE = "LAB_CREATE";
    public static final String LAB_UPDATE = "LAB_UPDATE";
    public static final String LAB_DELETE = "LAB_DELETE";

    public static final String RADIOLOGY_READ = "RADIOLOGY_READ";
    public static final String RADIOLOGY_CREATE = "RADIOLOGY_CREATE";
    public static final String RADIOLOGY_UPDATE = "RADIOLOGY_UPDATE";
    public static final String RADIOLOGY_DELETE = "RADIOLOGY_DELETE";

    public static final String DOCUMENT_READ = "DOCUMENT_READ";
    public static final String DOCUMENT_CREATE = "DOCUMENT_CREATE";
    public static final String DOCUMENT_UPDATE = "DOCUMENT_UPDATE";
    public static final String DOCUMENT_DELETE = "DOCUMENT_DELETE";

    public static final String NOTIFICATION_READ = "NOTIFICATION_READ";
    public static final String NOTIFICATION_CREATE = "NOTIFICATION_CREATE";
    public static final String NOTIFICATION_UPDATE = "NOTIFICATION_UPDATE";
    public static final String NOTIFICATION_DELETE = "NOTIFICATION_DELETE";

    public static final String BILLING_READ = "BILLING_READ";
    public static final String BILLING_CREATE = "BILLING_CREATE";
    public static final String BILLING_UPDATE = "BILLING_UPDATE";
    public static final String BILLING_DELETE = "BILLING_DELETE";

    public static final String AUDIT_READ = "AUDIT_READ";
    public static final String DASHBOARD_READ = "DASHBOARD_READ";
    public static final String REPORT_READ = "REPORT_READ";

    private PermissionConstants() {
    }

    /**
     * Convenience builder that delegates to {@link PermissionNaming}.
     */
    public static String of(final PermissionGroup group, final PermissionAction action) {
        return PermissionNaming.code(group, action);
    }
}
