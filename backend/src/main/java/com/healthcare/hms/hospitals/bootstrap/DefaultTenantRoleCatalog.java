package com.healthcare.hms.hospitals.bootstrap;

import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.rbac.RoleHierarchy;
import com.healthcare.hms.users.rbac.SystemRolePermissionMatrix;
import com.healthcare.hms.users.rbac.SystemRolePermissionMatrix.RoleProfile;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catalog of default tenant roles and their permission grants provisioned at registration.
 *
 * <p>Permissions themselves are platform-global (seeded by Flyway / platform bootstrap);
 * this catalog selects which {@link RoleType}s are created per hospital tenant and
 * attaches grants from {@link SystemRolePermissionMatrix}.
 *
 * <p>{@link RoleType#HOSPITAL_ADMIN} must remain first in {@link #definitions()} so the
 * provisioner can attach child roles to the tenant root.
 */
public final class DefaultTenantRoleCatalog {

    /**
     * Tenant-scoped default roles (excludes platform-only {@link RoleType#SUPER_ADMIN}
     * and portal {@link RoleType#PATIENT}).
     */
    private static final List<RoleType> TENANT_ROLE_ORDER = List.of(
            RoleType.HOSPITAL_ADMIN,
            RoleType.DOCTOR,
            RoleType.NURSE,
            RoleType.RECEPTIONIST,
            RoleType.LAB_TECHNICIAN,
            RoleType.PHARMACIST,
            RoleType.ACCOUNTANT
    );

    private DefaultTenantRoleCatalog() {
    }

    /**
     * Ordered role definitions provisioned for every new hospital tenant.
     */
    public static Map<RoleType, RoleDefinition> definitions() {
        final Map<RoleType, RoleDefinition> definitions = new LinkedHashMap<>();
        for (final RoleType type : TENANT_ROLE_ORDER) {
            if (RoleHierarchy.isPlatformOnly(type)) {
                continue;
            }
            final RoleProfile profile = SystemRolePermissionMatrix.profileOf(type);
            definitions.put(
                    type,
                    new RoleDefinition(
                            profile.name(),
                            profile.description(),
                            SystemRolePermissionMatrix.permissionsFor(type)
                    )
            );
        }
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
