package com.healthcare.hms.users.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import com.healthcare.hms.users.enums.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SystemRolePermissionMatrix")
class SystemRolePermissionMatrixTest {

    @Test
    @DisplayName("covers every RoleType with catalog-aligned grants")
    void coversAllRoleTypes() {
        for (final RoleType type : RoleType.values()) {
            assertThat(SystemRolePermissionMatrix.permissionsFor(type))
                    .as("grants for %s", type)
                    .isNotEmpty()
                    .allMatch(PermissionCatalog::contains);
        }
    }

    @Test
    @DisplayName("Super Admin receives the full permission catalog")
    void superAdminHasAllPermissions() {
        assertThat(SystemRolePermissionMatrix.permissionsFor(RoleType.SUPER_ADMIN))
                .containsExactlyInAnyOrderElementsOf(PermissionCatalog.allCodes());
    }

    @Test
    @DisplayName("Accountant receives billing CRUD plus supporting reads")
    void accountantMatrix() {
        assertThat(SystemRolePermissionMatrix.permissionsFor(RoleType.ACCOUNTANT))
                .contains(
                        PermissionConstants.BILLING_READ,
                        PermissionConstants.BILLING_CREATE,
                        PermissionConstants.BILLING_UPDATE,
                        PermissionConstants.BILLING_DELETE,
                        PermissionConstants.PATIENT_READ,
                        PermissionConstants.REPORT_READ,
                        PermissionConstants.DASHBOARD_READ
                )
                .doesNotContain(
                        PermissionConstants.USER_DELETE,
                        PermissionConstants.VISIT_DELETE
                );
    }

    @Test
    @DisplayName("Doctor uses CREATE/UPDATE split instead of legacy WRITE")
    void doctorUsesCreateUpdate() {
        assertThat(SystemRolePermissionMatrix.permissionsFor(RoleType.DOCTOR))
                .contains(
                        PermissionConstants.PATIENT_CREATE,
                        PermissionConstants.PATIENT_UPDATE,
                        PermissionConstants.PRESCRIPTION_CREATE,
                        PermissionConstants.PRESCRIPTION_UPDATE
                )
                .doesNotContain("PATIENT_WRITE", "PRESCRIPTION_WRITE");
    }

    @Test
    @DisplayName("tenant catalog includes Accountant and excludes Super Admin")
    void tenantCatalogAlignment() {
        final var definitions = com.healthcare.hms.hospitals.bootstrap.DefaultTenantRoleCatalog.definitions();
        assertThat(definitions.keySet())
                .contains(
                        RoleType.HOSPITAL_ADMIN,
                        RoleType.DOCTOR,
                        RoleType.NURSE,
                        RoleType.RECEPTIONIST,
                        RoleType.LAB_TECHNICIAN,
                        RoleType.PHARMACIST,
                        RoleType.ACCOUNTANT
                )
                .doesNotContain(RoleType.SUPER_ADMIN, RoleType.PATIENT);
        assertThat(definitions.get(RoleType.ACCOUNTANT).permissionCodes())
                .isEqualTo(SystemRolePermissionMatrix.permissionsFor(RoleType.ACCOUNTANT));
    }

    @Test
    @DisplayName("permission catalog codes parse via PermissionNaming")
    void catalogCodesAreCanonical() {
        for (final var entry : PermissionCatalog.entries()) {
            assertThat(PermissionNaming.code(entry.group(), entry.action())).isEqualTo(entry.code());
            assertThat(PermissionNaming.parseGroup(entry.code())).contains(entry.group());
            assertThat(PermissionNaming.parseAction(entry.code())).contains(entry.action());
        }
        assertThat(PermissionCatalog.entries())
                .extracting(e -> e.action())
                .contains(PermissionAction.CREATE, PermissionAction.UPDATE)
                .doesNotContain((PermissionAction) null);
        assertThat(PermissionCatalog.entries())
                .extracting(e -> e.group())
                .contains(PermissionGroup.BILLING, PermissionGroup.PATIENT);
    }
}
