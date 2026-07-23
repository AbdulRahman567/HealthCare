package com.healthcare.hms.users.rbac;

import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.enums.RoleType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default system-role permission matrix (Phase 3.5).
 *
 * <p>Single source of truth for which permission codes each default role receives.
 * Used by Flyway seeds, tenant role provisioning, and platform RBAC bootstrap.
 *
 * <p>Runtime authorization still uses the explicit {@code role_permissions} grants —
 * this matrix only defines <em>default</em> system/tenant role templates.
 */
public final class SystemRolePermissionMatrix {

    private static final Map<RoleType, Set<String>> GRANTS = new EnumMap<>(RoleType.class);

    static {
        PermissionCatalog.assertConstantsAligned();

        GRANTS.put(RoleType.SUPER_ADMIN, Set.copyOf(PermissionCatalog.allCodes()));

        GRANTS.put(RoleType.HOSPITAL_ADMIN, Set.of(
                PermissionConstants.USER_READ,
                PermissionConstants.USER_CREATE,
                PermissionConstants.USER_UPDATE,
                PermissionConstants.USER_DELETE,
                PermissionConstants.ROLE_READ,
                PermissionConstants.ROLE_CREATE,
                PermissionConstants.ROLE_UPDATE,
                PermissionConstants.HOSPITAL_READ,
                PermissionConstants.HOSPITAL_UPDATE,
                PermissionConstants.DEPARTMENT_READ,
                PermissionConstants.DEPARTMENT_CREATE,
                PermissionConstants.DEPARTMENT_UPDATE,
                PermissionConstants.DEPARTMENT_DELETE,
                PermissionConstants.DOCTOR_READ,
                PermissionConstants.DOCTOR_CREATE,
                PermissionConstants.DOCTOR_UPDATE,
                PermissionConstants.DOCTOR_DELETE,
                PermissionConstants.PATIENT_READ,
                PermissionConstants.PATIENT_CREATE,
                PermissionConstants.PATIENT_UPDATE,
                PermissionConstants.PATIENT_DELETE,
                PermissionConstants.APPOINTMENT_READ,
                PermissionConstants.APPOINTMENT_CREATE,
                PermissionConstants.APPOINTMENT_UPDATE,
                PermissionConstants.APPOINTMENT_DELETE,
                PermissionConstants.VISIT_READ,
                PermissionConstants.VISIT_CREATE,
                PermissionConstants.VISIT_UPDATE,
                PermissionConstants.VISIT_DELETE,
                PermissionConstants.PRESCRIPTION_READ,
                PermissionConstants.PRESCRIPTION_CREATE,
                PermissionConstants.PRESCRIPTION_UPDATE,
                PermissionConstants.PRESCRIPTION_DELETE,
                PermissionConstants.LAB_READ,
                PermissionConstants.LAB_CREATE,
                PermissionConstants.LAB_UPDATE,
                PermissionConstants.LAB_DELETE,
                PermissionConstants.RADIOLOGY_READ,
                PermissionConstants.RADIOLOGY_CREATE,
                PermissionConstants.RADIOLOGY_UPDATE,
                PermissionConstants.RADIOLOGY_DELETE,
                PermissionConstants.DOCUMENT_READ,
                PermissionConstants.DOCUMENT_CREATE,
                PermissionConstants.DOCUMENT_UPDATE,
                PermissionConstants.DOCUMENT_DELETE,
                PermissionConstants.NOTIFICATION_READ,
                PermissionConstants.NOTIFICATION_CREATE,
                PermissionConstants.NOTIFICATION_UPDATE,
                PermissionConstants.NOTIFICATION_DELETE,
                PermissionConstants.BILLING_READ,
                PermissionConstants.BILLING_CREATE,
                PermissionConstants.BILLING_UPDATE,
                PermissionConstants.BILLING_DELETE,
                PermissionConstants.AUDIT_READ,
                PermissionConstants.DASHBOARD_READ,
                PermissionConstants.REPORT_READ
        ));

        GRANTS.put(RoleType.DOCTOR, Set.of(
                PermissionConstants.PATIENT_READ,
                PermissionConstants.PATIENT_CREATE,
                PermissionConstants.PATIENT_UPDATE,
                PermissionConstants.APPOINTMENT_READ,
                PermissionConstants.APPOINTMENT_CREATE,
                PermissionConstants.APPOINTMENT_UPDATE,
                PermissionConstants.VISIT_READ,
                PermissionConstants.VISIT_CREATE,
                PermissionConstants.VISIT_UPDATE,
                PermissionConstants.PRESCRIPTION_READ,
                PermissionConstants.PRESCRIPTION_CREATE,
                PermissionConstants.PRESCRIPTION_UPDATE,
                PermissionConstants.LAB_READ,
                PermissionConstants.RADIOLOGY_READ,
                PermissionConstants.DOCUMENT_READ,
                PermissionConstants.DOCUMENT_CREATE,
                PermissionConstants.DOCUMENT_UPDATE,
                PermissionConstants.DASHBOARD_READ
        ));

        GRANTS.put(RoleType.NURSE, Set.of(
                PermissionConstants.PATIENT_READ,
                PermissionConstants.APPOINTMENT_READ,
                PermissionConstants.VISIT_READ,
                PermissionConstants.VISIT_CREATE,
                PermissionConstants.VISIT_UPDATE,
                PermissionConstants.PRESCRIPTION_READ,
                PermissionConstants.LAB_READ,
                PermissionConstants.DOCUMENT_READ,
                PermissionConstants.NOTIFICATION_READ,
                PermissionConstants.DASHBOARD_READ
        ));

        GRANTS.put(RoleType.RECEPTIONIST, Set.of(
                PermissionConstants.PATIENT_READ,
                PermissionConstants.PATIENT_CREATE,
                PermissionConstants.PATIENT_UPDATE,
                PermissionConstants.APPOINTMENT_READ,
                PermissionConstants.APPOINTMENT_CREATE,
                PermissionConstants.APPOINTMENT_UPDATE,
                PermissionConstants.APPOINTMENT_DELETE,
                PermissionConstants.DOCTOR_READ,
                PermissionConstants.NOTIFICATION_READ,
                PermissionConstants.NOTIFICATION_CREATE,
                PermissionConstants.NOTIFICATION_UPDATE,
                PermissionConstants.DASHBOARD_READ
        ));

        GRANTS.put(RoleType.LAB_TECHNICIAN, Set.of(
                PermissionConstants.PATIENT_READ,
                PermissionConstants.LAB_READ,
                PermissionConstants.LAB_CREATE,
                PermissionConstants.LAB_UPDATE,
                PermissionConstants.DOCUMENT_READ,
                PermissionConstants.DOCUMENT_CREATE,
                PermissionConstants.DOCUMENT_UPDATE,
                PermissionConstants.DASHBOARD_READ
        ));

        GRANTS.put(RoleType.PHARMACIST, Set.of(
                PermissionConstants.PATIENT_READ,
                PermissionConstants.PRESCRIPTION_READ,
                PermissionConstants.PRESCRIPTION_UPDATE,
                PermissionConstants.DOCUMENT_READ,
                PermissionConstants.DASHBOARD_READ
        ));

        GRANTS.put(RoleType.ACCOUNTANT, Set.of(
                PermissionConstants.BILLING_READ,
                PermissionConstants.BILLING_CREATE,
                PermissionConstants.BILLING_UPDATE,
                PermissionConstants.BILLING_DELETE,
                PermissionConstants.PATIENT_READ,
                PermissionConstants.APPOINTMENT_READ,
                PermissionConstants.DOCUMENT_READ,
                PermissionConstants.REPORT_READ,
                PermissionConstants.DASHBOARD_READ
        ));

        GRANTS.put(RoleType.PATIENT, Set.of(
                PermissionConstants.APPOINTMENT_READ,
                PermissionConstants.DOCUMENT_READ,
                PermissionConstants.NOTIFICATION_READ,
                PermissionConstants.DASHBOARD_READ
        ));

        validateGrants();
    }

    private SystemRolePermissionMatrix() {
    }

    /**
     * Default permission codes for {@code type}. Empty only if the type is unknown.
     */
    public static Set<String> permissionsFor(final RoleType type) {
        Objects.requireNonNull(type, "type");
        final Set<String> grants = GRANTS.get(type);
        if (grants == null) {
            throw new IllegalArgumentException("No permission matrix entry for role: " + type);
        }
        return grants;
    }

    /**
     * Ordered snapshot of the full matrix (role → permission codes).
     */
    public static Map<RoleType, Set<String>> allGrants() {
        final Map<RoleType, Set<String>> copy = new LinkedHashMap<>();
        for (final RoleType type : RoleType.values()) {
            copy.put(type, permissionsFor(type));
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Display metadata for default system roles (platform templates + tenant provisioner).
     */
    public static RoleProfile profileOf(final RoleType type) {
        Objects.requireNonNull(type, "type");
        return switch (type) {
            case SUPER_ADMIN -> new RoleProfile("Super Admin", "Platform-wide administrator");
            case HOSPITAL_ADMIN -> new RoleProfile("Hospital Admin", "Hospital tenant administrator");
            case DOCTOR -> new RoleProfile("Doctor", "Clinical doctor role");
            case NURSE -> new RoleProfile("Nurse", "Nursing staff role");
            case RECEPTIONIST -> new RoleProfile("Receptionist", "Front-desk receptionist role");
            case LAB_TECHNICIAN -> new RoleProfile("Lab Technician", "Laboratory technician role");
            case PHARMACIST -> new RoleProfile("Pharmacist", "Pharmacy staff role");
            case ACCOUNTANT -> new RoleProfile("Accountant", "Hospital billing and finance role");
            case PATIENT -> new RoleProfile("Patient", "Patient portal role");
        };
    }

    private static void validateGrants() {
        for (final Map.Entry<RoleType, Set<String>> entry : GRANTS.entrySet()) {
            for (final String code : entry.getValue()) {
                if (!PermissionCatalog.contains(code)) {
                    throw new IllegalStateException(
                            "Matrix grant for " + entry.getKey() + " references unknown permission: " + code
                    );
                }
            }
        }
        final Set<RoleType> missing = new LinkedHashSet<>();
        for (final RoleType type : RoleType.values()) {
            if (!GRANTS.containsKey(type)) {
                missing.add(type);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Matrix missing role types: " + missing);
        }
    }

    public record RoleProfile(String name, String description) {
    }
}
