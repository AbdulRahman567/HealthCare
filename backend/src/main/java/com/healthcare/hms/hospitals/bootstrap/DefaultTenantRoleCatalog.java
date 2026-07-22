package com.healthcare.hms.hospitals.bootstrap;

import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.enums.RoleType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catalog of default tenant roles and their permission grants provisioned at registration.
 *
 * <p>Permissions themselves are platform-global (seeded by Flyway); this catalog only
 * defines which codes attach to each newly created tenant-scoped role.
 */
public final class DefaultTenantRoleCatalog {

    private static final Set<String> HOSPITAL_ADMIN_PERMISSIONS = Set.of(
            PermissionConstants.USER_READ,
            PermissionConstants.USER_WRITE,
            PermissionConstants.USER_DELETE,
            PermissionConstants.ROLE_READ,
            PermissionConstants.HOSPITAL_READ,
            PermissionConstants.HOSPITAL_WRITE,
            PermissionConstants.DEPARTMENT_READ,
            PermissionConstants.DEPARTMENT_WRITE,
            PermissionConstants.DEPARTMENT_DELETE,
            PermissionConstants.DOCTOR_READ,
            PermissionConstants.DOCTOR_WRITE,
            PermissionConstants.DOCTOR_DELETE,
            PermissionConstants.PATIENT_READ,
            PermissionConstants.PATIENT_WRITE,
            PermissionConstants.PATIENT_DELETE,
            PermissionConstants.APPOINTMENT_READ,
            PermissionConstants.APPOINTMENT_WRITE,
            PermissionConstants.APPOINTMENT_DELETE,
            PermissionConstants.VISIT_READ,
            PermissionConstants.VISIT_WRITE,
            PermissionConstants.VISIT_DELETE,
            PermissionConstants.PRESCRIPTION_READ,
            PermissionConstants.PRESCRIPTION_CREATE,
            PermissionConstants.PRESCRIPTION_WRITE,
            PermissionConstants.PRESCRIPTION_DELETE,
            PermissionConstants.LAB_READ,
            PermissionConstants.LAB_WRITE,
            PermissionConstants.LAB_DELETE,
            PermissionConstants.RADIOLOGY_READ,
            PermissionConstants.RADIOLOGY_WRITE,
            PermissionConstants.RADIOLOGY_DELETE,
            PermissionConstants.DOCUMENT_READ,
            PermissionConstants.DOCUMENT_WRITE,
            PermissionConstants.DOCUMENT_DELETE,
            PermissionConstants.NOTIFICATION_READ,
            PermissionConstants.NOTIFICATION_WRITE,
            PermissionConstants.AUDIT_READ,
            PermissionConstants.DASHBOARD_READ,
            PermissionConstants.REPORT_READ
    );

    private static final Set<String> DOCTOR_PERMISSIONS = Set.of(
            PermissionConstants.PATIENT_READ,
            PermissionConstants.PATIENT_WRITE,
            PermissionConstants.APPOINTMENT_READ,
            PermissionConstants.APPOINTMENT_WRITE,
            PermissionConstants.VISIT_READ,
            PermissionConstants.VISIT_WRITE,
            PermissionConstants.PRESCRIPTION_READ,
            PermissionConstants.PRESCRIPTION_CREATE,
            PermissionConstants.PRESCRIPTION_WRITE,
            PermissionConstants.LAB_READ,
            PermissionConstants.RADIOLOGY_READ,
            PermissionConstants.DOCUMENT_READ,
            PermissionConstants.DOCUMENT_WRITE,
            PermissionConstants.DASHBOARD_READ
    );

    private static final Set<String> NURSE_PERMISSIONS = Set.of(
            PermissionConstants.PATIENT_READ,
            PermissionConstants.APPOINTMENT_READ,
            PermissionConstants.VISIT_READ,
            PermissionConstants.VISIT_WRITE,
            PermissionConstants.PRESCRIPTION_READ,
            PermissionConstants.LAB_READ,
            PermissionConstants.DOCUMENT_READ,
            PermissionConstants.NOTIFICATION_READ,
            PermissionConstants.DASHBOARD_READ
    );

    private static final Set<String> RECEPTIONIST_PERMISSIONS = Set.of(
            PermissionConstants.PATIENT_READ,
            PermissionConstants.PATIENT_WRITE,
            PermissionConstants.APPOINTMENT_READ,
            PermissionConstants.APPOINTMENT_WRITE,
            PermissionConstants.APPOINTMENT_DELETE,
            PermissionConstants.DOCTOR_READ,
            PermissionConstants.NOTIFICATION_READ,
            PermissionConstants.NOTIFICATION_WRITE,
            PermissionConstants.DASHBOARD_READ
    );

    private static final Set<String> LAB_PERMISSIONS = Set.of(
            PermissionConstants.PATIENT_READ,
            PermissionConstants.LAB_READ,
            PermissionConstants.LAB_WRITE,
            PermissionConstants.DOCUMENT_READ,
            PermissionConstants.DOCUMENT_WRITE,
            PermissionConstants.DASHBOARD_READ
    );

    private static final Set<String> PHARMACIST_PERMISSIONS = Set.of(
            PermissionConstants.PATIENT_READ,
            PermissionConstants.PRESCRIPTION_READ,
            PermissionConstants.PRESCRIPTION_WRITE,
            PermissionConstants.DOCUMENT_READ,
            PermissionConstants.DASHBOARD_READ
    );

    private DefaultTenantRoleCatalog() {
    }

    /**
     * Ordered role definitions provisioned for every new hospital tenant.
     */
    public static Map<RoleType, RoleDefinition> definitions() {
        final Map<RoleType, RoleDefinition> definitions = new LinkedHashMap<>();
        definitions.put(
                RoleType.HOSPITAL_ADMIN,
                new RoleDefinition("Hospital Admin", "Hospital tenant administrator", HOSPITAL_ADMIN_PERMISSIONS)
        );
        definitions.put(
                RoleType.DOCTOR,
                new RoleDefinition("Doctor", "Clinical doctor role", DOCTOR_PERMISSIONS)
        );
        definitions.put(
                RoleType.NURSE,
                new RoleDefinition("Nurse", "Nursing staff role", NURSE_PERMISSIONS)
        );
        definitions.put(
                RoleType.RECEPTIONIST,
                new RoleDefinition("Receptionist", "Front-desk receptionist role", RECEPTIONIST_PERMISSIONS)
        );
        definitions.put(
                RoleType.LAB_TECHNICIAN,
                new RoleDefinition("Lab Technician", "Laboratory technician role", LAB_PERMISSIONS)
        );
        definitions.put(
                RoleType.PHARMACIST,
                new RoleDefinition("Pharmacist", "Pharmacy staff role", PHARMACIST_PERMISSIONS)
        );
        return Collections.unmodifiableMap(definitions);
    }

    public record RoleDefinition(String name, String description, Set<String> permissionCodes) {
        public RoleDefinition {
            permissionCodes = Set.copyOf(permissionCodes);
        }

        public List<String> permissionCodeList() {
            return List.copyOf(permissionCodes);
        }
    }
}
