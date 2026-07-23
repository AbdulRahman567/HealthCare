package com.healthcare.hms.users.bootstrap;

import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.rbac.PermissionCatalog;
import com.healthcare.hms.users.rbac.PermissionCatalog.CatalogEntry;
import com.healthcare.hms.users.rbac.RoleHierarchy;
import com.healthcare.hms.users.rbac.SystemRolePermissionMatrix;
import com.healthcare.hms.users.rbac.SystemRolePermissionMatrix.RoleProfile;
import com.healthcare.hms.users.repository.PermissionRepository;
import com.healthcare.hms.users.repository.RoleRepository;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent startup seed for the platform permission catalog and default system roles
 * (Phase 3.5). Complements Flyway migrations so new environments and upgrades stay aligned
 * with {@link PermissionCatalog} / {@link SystemRolePermissionMatrix}.
 *
 * <p>Does <strong>not</strong> mutate tenant-scoped roles — those are provisioned at
 * hospital registration via {@code TenantRoleProvisioner}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class PlatformRbacBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatformRbacBootstrap.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PlatformRbacBootstrap(
            final PermissionRepository permissionRepository,
            final RoleRepository roleRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        final Map<String, Permission> permissionsByCode = ensurePermissionCatalog();
        final Map<RoleType, Role> platformRoles = ensurePlatformRoles();
        syncPlatformRoleGrants(platformRoles, permissionsByCode);
        log.info(
                "Platform RBAC bootstrap complete: {} permissions, {} system roles",
                permissionsByCode.size(),
                platformRoles.size()
        );
    }

    private Map<String, Permission> ensurePermissionCatalog() {
        final Map<String, Permission> byCode = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Permission::getCode,
                        permission -> permission,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        for (final CatalogEntry entry : PermissionCatalog.entries()) {
            Permission permission = byCode.get(entry.code());
            if (permission == null) {
                permission = new Permission();
                permission.setCode(entry.code());
                permission.setName(entry.name());
                permission.setDescription(entry.description());
                permission.setPermissionGroup(entry.group());
                permission.setAction(entry.action());
                permission.setSystemPermission(true);
                permission = permissionRepository.save(permission);
                byCode.put(entry.code(), permission);
            } else {
                boolean dirty = false;
                if (!entry.name().equals(permission.getName())) {
                    permission.setName(entry.name());
                    dirty = true;
                }
                if (entry.description() != null && !entry.description().equals(permission.getDescription())) {
                    permission.setDescription(entry.description());
                    dirty = true;
                }
                if (permission.getPermissionGroup() != entry.group()) {
                    permission.setPermissionGroup(entry.group());
                    dirty = true;
                }
                if (permission.getAction() != entry.action()) {
                    permission.setAction(entry.action());
                    dirty = true;
                }
                if (!permission.isSystemPermission()) {
                    permission.setSystemPermission(true);
                    dirty = true;
                }
                if (dirty) {
                    permission = permissionRepository.save(permission);
                    byCode.put(entry.code(), permission);
                }
            }
        }
        return byCode;
    }

    private Map<RoleType, Role> ensurePlatformRoles() {
        final Map<RoleType, Role> roles = new LinkedHashMap<>();

        for (final RoleType type : RoleType.values()) {
            final RoleProfile profile = SystemRolePermissionMatrix.profileOf(type);
            Role role = roleRepository.findSystemRoleWithPermissions(type).orElse(null);
            if (role == null) {
                role = new Role();
                role.setTenantId(null);
                role.setType(type);
                role.setName(profile.name());
                role.setDescription(profile.description());
                role.setSystemRole(true);
                role.applyCatalogHierarchy();
                role = roleRepository.save(role);
                // Re-fetch with permissions collection initialized
                role = roleRepository.findSystemRoleWithPermissions(type).orElse(role);
                log.info("Seeded platform system role {}", type);
            } else {
                boolean dirty = false;
                if (!profile.name().equals(role.getName())) {
                    role.setName(profile.name());
                    dirty = true;
                }
                if (!profile.description().equals(role.getDescription())) {
                    role.setDescription(profile.description());
                    dirty = true;
                }
                final int expectedLevel = RoleHierarchy.levelOf(type);
                if (role.getHierarchyLevel() != expectedLevel) {
                    role.setHierarchyLevel(expectedLevel);
                    dirty = true;
                }
                final boolean expectedAssignable = RoleHierarchy.isAssignable(type);
                if (role.isAssignable() != expectedAssignable) {
                    role.setAssignable(expectedAssignable);
                    dirty = true;
                }
                if (!role.isSystemRole()) {
                    role.setSystemRole(true);
                    dirty = true;
                }
                if (dirty) {
                    role = roleRepository.save(role);
                }
            }
            roles.put(type, role);
        }

        for (final Map.Entry<RoleType, Role> entry : roles.entrySet()) {
            final RoleType type = entry.getKey();
            final Role role = entry.getValue();
            final Role expectedParent = RoleHierarchy.parentOf(type)
                    .map(roles::get)
                    .orElse(null);
            final UUID currentParentId = role.getParentRole() == null ? null : role.getParentRole().getId();
            final UUID expectedParentId = expectedParent == null ? null : expectedParent.getId();
            if (currentParentId == null ? expectedParentId != null : !currentParentId.equals(expectedParentId)) {
                role.setParentRole(expectedParent);
                roleRepository.save(role);
            }
        }

        return roles;
    }

    private void syncPlatformRoleGrants(
            final Map<RoleType, Role> platformRoles,
            final Map<String, Permission> permissionsByCode
    ) {
        for (final Map.Entry<RoleType, Role> entry : platformRoles.entrySet()) {
            final RoleType type = entry.getKey();
            final Role role = entry.getValue();
            final Set<String> expectedCodes = SystemRolePermissionMatrix.permissionsFor(type);
            final Set<Permission> expected = new HashSet<>();
            for (final String code : expectedCodes) {
                final Permission permission = permissionsByCode.get(code);
                if (permission == null) {
                    throw new IllegalStateException("Missing permission for matrix grant: " + code);
                }
                expected.add(permission);
            }

            final Set<String> currentCodes = role.getPermissions().stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toSet());
            if (currentCodes.equals(expectedCodes)) {
                continue;
            }

            final Set<Permission> snapshot = Set.copyOf(role.getPermissions());
            for (final Permission permission : snapshot) {
                role.removePermission(permission);
            }
            expected.forEach(role::addPermission);
            roleRepository.save(role);
            log.info("Synced platform role {} grants ({} permissions)", type, expected.size());
        }
    }
}
