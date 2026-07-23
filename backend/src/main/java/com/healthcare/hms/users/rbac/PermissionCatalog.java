package com.healthcare.hms.users.rbac;

import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Complete platform permission catalog (Phase 3.5).
 *
 * <p>Every seeded permission must appear here. Codes follow
 * {@code {GROUP}_{ACTION}} via {@link PermissionNaming}.
 */
public final class PermissionCatalog {

    private static final List<CatalogEntry> ENTRIES = List.of(
            entry(PermissionGroup.USER, PermissionAction.READ, "Read users", "View user profiles and listings"),
            entry(PermissionGroup.USER, PermissionAction.CREATE, "Create users", "Create user accounts"),
            entry(PermissionGroup.USER, PermissionAction.UPDATE, "Update users", "Update user profiles and status"),
            entry(PermissionGroup.USER, PermissionAction.DELETE, "Delete users", "Soft-delete users"),

            entry(PermissionGroup.ROLE, PermissionAction.READ, "Read roles", "View roles and assignments"),
            entry(PermissionGroup.ROLE, PermissionAction.CREATE, "Create roles", "Create custom roles"),
            entry(PermissionGroup.ROLE, PermissionAction.UPDATE, "Update roles", "Update roles and permission grants"),
            entry(PermissionGroup.ROLE, PermissionAction.DELETE, "Delete roles", "Soft-delete roles"),

            entry(PermissionGroup.HOSPITAL, PermissionAction.READ, "Read hospitals", "View hospital profiles and settings"),
            entry(PermissionGroup.HOSPITAL, PermissionAction.CREATE, "Create hospitals", "Create hospital profiles"),
            entry(PermissionGroup.HOSPITAL, PermissionAction.UPDATE, "Update hospitals", "Update hospital profiles and settings"),
            entry(PermissionGroup.HOSPITAL, PermissionAction.DELETE, "Delete hospitals", "Soft-delete hospitals"),

            entry(PermissionGroup.DEPARTMENT, PermissionAction.READ, "Read departments", "View departments"),
            entry(PermissionGroup.DEPARTMENT, PermissionAction.CREATE, "Create departments", "Create departments"),
            entry(PermissionGroup.DEPARTMENT, PermissionAction.UPDATE, "Update departments", "Update departments"),
            entry(PermissionGroup.DEPARTMENT, PermissionAction.DELETE, "Delete departments", "Soft-delete departments"),

            entry(PermissionGroup.DOCTOR, PermissionAction.READ, "Read doctors", "View doctor profiles"),
            entry(PermissionGroup.DOCTOR, PermissionAction.CREATE, "Create doctors", "Create doctor profiles"),
            entry(PermissionGroup.DOCTOR, PermissionAction.UPDATE, "Update doctors", "Update doctor profiles"),
            entry(PermissionGroup.DOCTOR, PermissionAction.DELETE, "Delete doctors", "Soft-delete doctors"),

            entry(PermissionGroup.PATIENT, PermissionAction.READ, "Read patients", "View patient records"),
            entry(PermissionGroup.PATIENT, PermissionAction.CREATE, "Create patients", "Register patients"),
            entry(PermissionGroup.PATIENT, PermissionAction.UPDATE, "Update patients", "Update patient records"),
            entry(PermissionGroup.PATIENT, PermissionAction.DELETE, "Delete patients", "Soft-delete patients"),

            entry(PermissionGroup.APPOINTMENT, PermissionAction.READ, "Read appointments", "View appointments"),
            entry(PermissionGroup.APPOINTMENT, PermissionAction.CREATE, "Create appointments", "Schedule appointments"),
            entry(PermissionGroup.APPOINTMENT, PermissionAction.UPDATE, "Update appointments", "Reschedule or modify appointments"),
            entry(PermissionGroup.APPOINTMENT, PermissionAction.DELETE, "Delete appointments", "Cancel or soft-delete appointments"),

            entry(PermissionGroup.VISIT, PermissionAction.READ, "Read visits", "View clinical visits"),
            entry(PermissionGroup.VISIT, PermissionAction.CREATE, "Create visits", "Open clinical visits"),
            entry(PermissionGroup.VISIT, PermissionAction.UPDATE, "Update visits", "Update clinical visit notes"),
            entry(PermissionGroup.VISIT, PermissionAction.DELETE, "Delete visits", "Soft-delete visits"),

            entry(PermissionGroup.PRESCRIPTION, PermissionAction.READ, "Read prescriptions", "View prescriptions"),
            entry(PermissionGroup.PRESCRIPTION, PermissionAction.CREATE, "Create prescriptions", "Create prescriptions"),
            entry(PermissionGroup.PRESCRIPTION, PermissionAction.UPDATE, "Update prescriptions", "Update prescriptions"),
            entry(PermissionGroup.PRESCRIPTION, PermissionAction.DELETE, "Delete prescriptions", "Soft-delete prescriptions"),

            entry(PermissionGroup.LAB, PermissionAction.READ, "Read laboratory", "View lab orders and results"),
            entry(PermissionGroup.LAB, PermissionAction.CREATE, "Create laboratory", "Create lab orders"),
            entry(PermissionGroup.LAB, PermissionAction.UPDATE, "Update laboratory", "Update lab orders and results"),
            entry(PermissionGroup.LAB, PermissionAction.DELETE, "Delete laboratory", "Soft-delete lab data"),

            entry(PermissionGroup.RADIOLOGY, PermissionAction.READ, "Read radiology", "View radiology orders and reports"),
            entry(PermissionGroup.RADIOLOGY, PermissionAction.CREATE, "Create radiology", "Create radiology orders"),
            entry(PermissionGroup.RADIOLOGY, PermissionAction.UPDATE, "Update radiology", "Update radiology reports"),
            entry(PermissionGroup.RADIOLOGY, PermissionAction.DELETE, "Delete radiology", "Soft-delete radiology data"),

            entry(PermissionGroup.DOCUMENT, PermissionAction.READ, "Read documents", "View medical documents"),
            entry(PermissionGroup.DOCUMENT, PermissionAction.CREATE, "Create documents", "Upload medical documents"),
            entry(PermissionGroup.DOCUMENT, PermissionAction.UPDATE, "Update documents", "Update document metadata"),
            entry(PermissionGroup.DOCUMENT, PermissionAction.DELETE, "Delete documents", "Soft-delete documents"),

            entry(PermissionGroup.NOTIFICATION, PermissionAction.READ, "Read notifications", "View notifications"),
            entry(PermissionGroup.NOTIFICATION, PermissionAction.CREATE, "Create notifications", "Send notifications"),
            entry(PermissionGroup.NOTIFICATION, PermissionAction.UPDATE, "Update notifications", "Update notification state"),
            entry(PermissionGroup.NOTIFICATION, PermissionAction.DELETE, "Delete notifications", "Soft-delete notifications"),

            entry(PermissionGroup.BILLING, PermissionAction.READ, "Read billing", "View invoices and billing records"),
            entry(PermissionGroup.BILLING, PermissionAction.CREATE, "Create billing", "Create invoices and charges"),
            entry(PermissionGroup.BILLING, PermissionAction.UPDATE, "Update billing", "Update invoices and payments"),
            entry(PermissionGroup.BILLING, PermissionAction.DELETE, "Delete billing", "Void or soft-delete billing records"),

            entry(PermissionGroup.AUDIT, PermissionAction.READ, "Read audit logs", "View audit trail entries"),
            entry(PermissionGroup.DASHBOARD, PermissionAction.READ, "Read dashboards", "View operational dashboards"),
            entry(PermissionGroup.REPORT, PermissionAction.READ, "Read reports", "View analytics reports")
    );

    private static final Set<String> ALL_CODES;

    static {
        final Set<String> codes = new LinkedHashSet<>();
        for (final CatalogEntry entry : ENTRIES) {
            codes.add(entry.code());
        }
        ALL_CODES = Collections.unmodifiableSet(codes);
    }

    private PermissionCatalog() {
    }

    public static List<CatalogEntry> entries() {
        return ENTRIES;
    }

    public static Set<String> allCodes() {
        return ALL_CODES;
    }

    public static boolean contains(final String code) {
        return code != null && ALL_CODES.contains(code);
    }

    /**
     * Sanity check that {@link PermissionConstants} literals stay aligned with the catalog.
     */
    public static void assertConstantsAligned() {
        final List<String> constants = Arrays.asList(
                PermissionConstants.USER_READ, PermissionConstants.USER_CREATE,
                PermissionConstants.USER_UPDATE, PermissionConstants.USER_DELETE,
                PermissionConstants.ROLE_READ, PermissionConstants.ROLE_CREATE,
                PermissionConstants.ROLE_UPDATE, PermissionConstants.ROLE_DELETE,
                PermissionConstants.HOSPITAL_READ, PermissionConstants.HOSPITAL_CREATE,
                PermissionConstants.HOSPITAL_UPDATE, PermissionConstants.HOSPITAL_DELETE,
                PermissionConstants.DEPARTMENT_READ, PermissionConstants.DEPARTMENT_CREATE,
                PermissionConstants.DEPARTMENT_UPDATE, PermissionConstants.DEPARTMENT_DELETE,
                PermissionConstants.DOCTOR_READ, PermissionConstants.DOCTOR_CREATE,
                PermissionConstants.DOCTOR_UPDATE, PermissionConstants.DOCTOR_DELETE,
                PermissionConstants.PATIENT_READ, PermissionConstants.PATIENT_CREATE,
                PermissionConstants.PATIENT_UPDATE, PermissionConstants.PATIENT_DELETE,
                PermissionConstants.APPOINTMENT_READ, PermissionConstants.APPOINTMENT_CREATE,
                PermissionConstants.APPOINTMENT_UPDATE, PermissionConstants.APPOINTMENT_DELETE,
                PermissionConstants.VISIT_READ, PermissionConstants.VISIT_CREATE,
                PermissionConstants.VISIT_UPDATE, PermissionConstants.VISIT_DELETE,
                PermissionConstants.PRESCRIPTION_READ, PermissionConstants.PRESCRIPTION_CREATE,
                PermissionConstants.PRESCRIPTION_UPDATE, PermissionConstants.PRESCRIPTION_DELETE,
                PermissionConstants.LAB_READ, PermissionConstants.LAB_CREATE,
                PermissionConstants.LAB_UPDATE, PermissionConstants.LAB_DELETE,
                PermissionConstants.RADIOLOGY_READ, PermissionConstants.RADIOLOGY_CREATE,
                PermissionConstants.RADIOLOGY_UPDATE, PermissionConstants.RADIOLOGY_DELETE,
                PermissionConstants.DOCUMENT_READ, PermissionConstants.DOCUMENT_CREATE,
                PermissionConstants.DOCUMENT_UPDATE, PermissionConstants.DOCUMENT_DELETE,
                PermissionConstants.NOTIFICATION_READ, PermissionConstants.NOTIFICATION_CREATE,
                PermissionConstants.NOTIFICATION_UPDATE, PermissionConstants.NOTIFICATION_DELETE,
                PermissionConstants.BILLING_READ, PermissionConstants.BILLING_CREATE,
                PermissionConstants.BILLING_UPDATE, PermissionConstants.BILLING_DELETE,
                PermissionConstants.AUDIT_READ, PermissionConstants.DASHBOARD_READ,
                PermissionConstants.REPORT_READ
        );
        for (final String constant : constants) {
            if (!ALL_CODES.contains(constant)) {
                throw new IllegalStateException("PermissionConstants literal missing from catalog: " + constant);
            }
        }
        if (constants.size() != ALL_CODES.size()) {
            throw new IllegalStateException(
                    "PermissionConstants count (" + constants.size()
                            + ") does not match catalog size (" + ALL_CODES.size() + ")"
            );
        }
    }

    private static CatalogEntry entry(
            final PermissionGroup group,
            final PermissionAction action,
            final String name,
            final String description
    ) {
        return new CatalogEntry(group, action, PermissionNaming.code(group, action), name, description);
    }

    public record CatalogEntry(
            PermissionGroup group,
            PermissionAction action,
            String code,
            String name,
            String description
    ) {
    }
}
