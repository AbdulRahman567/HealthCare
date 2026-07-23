package com.healthcare.hms.hospitals.bootstrap;

import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.hospitals.bootstrap.DefaultTenantRoleCatalog.RoleDefinition;
import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.rbac.RoleHierarchy;
import com.healthcare.hms.users.repository.PermissionRepository;
import com.healthcare.hms.users.repository.RoleRepository;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Provisions tenant-scoped default roles with hierarchy linkage and platform permission grants.
 */
@Component
public class TenantRoleProvisioner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public TenantRoleProvisioner(
            final RoleRepository roleRepository,
            final PermissionRepository permissionRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Creates default operational roles for the tenant and returns them ordered by catalog.
     *
     * <p>{@link RoleType#HOSPITAL_ADMIN} is the tenant root ({@code parent_role_id = null}).
     * Remaining roles reference that hospital-admin role as parent.
     */
    public List<Role> provisionDefaultRoles(final UUID tenantId) {
        final Map<RoleType, RoleDefinition> catalog = DefaultTenantRoleCatalog.definitions();
        final Map<RoleType, Role> createdByType = new EnumMap<>(RoleType.class);
        final List<Role> created = new ArrayList<>(catalog.size());

        for (final Map.Entry<RoleType, RoleDefinition> entry : catalog.entrySet()) {
            final RoleType type = entry.getKey();
            final RoleDefinition definition = entry.getValue();

            if (roleRepository.existsByTenantIdAndType(tenantId, type)) {
                throw new BusinessException(
                        "TENANT_ROLES_ALREADY_PROVISIONED",
                        "Default roles already exist for this tenant"
                );
            }

            final Role role = new Role();
            role.setTenantId(tenantId);
            role.setType(type);
            role.setName(definition.name());
            role.setDescription(definition.description());
            role.setSystemRole(false);
            role.applyCatalogHierarchy();

            if (!RoleHierarchy.isTenantRoot(type)) {
                final Role parent = createdByType.get(RoleType.HOSPITAL_ADMIN);
                if (parent == null) {
                    throw new BusinessException(
                            "TENANT_ROLE_HIERARCHY_INVALID",
                            "Hospital Admin must be provisioned before " + type.name()
                    );
                }
                role.setParentRole(parent);
            }

            final List<Permission> permissions = permissionRepository.findByCodeIn(definition.permissionCodes());
            if (permissions.size() != definition.permissionCodes().size()) {
                throw new BusinessException(
                        "PERMISSION_CATALOG_INCOMPLETE",
                        "Default permission catalog is incomplete for role " + type.name()
                );
            }
            permissions.forEach(role::addPermission);

            final Role saved = roleRepository.save(role);
            createdByType.put(type, saved);
            created.add(saved);
        }

        return List.copyOf(created);
    }
}
