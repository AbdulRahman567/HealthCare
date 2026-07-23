package com.healthcare.hms.hospitals.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.rbac.RoleHierarchy;
import com.healthcare.hms.users.repository.PermissionRepository;
import com.healthcare.hms.users.repository.RoleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantRoleProvisioner")
class TenantRoleProvisionerTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private TenantRoleProvisioner provisioner;

    @Test
    @DisplayName("provisions every catalog role with matching permissions")
    void provisionDefaultRoles_success() {
        final UUID tenantId = UUID.randomUUID();
        when(roleRepository.existsByTenantIdAndType(any(), any())).thenReturn(false);
        when(permissionRepository.findByCodeIn(any())).thenAnswer(invocation -> {
            final Set<String> codes = invocation.getArgument(0);
            final List<Permission> permissions = new ArrayList<>();
            for (final String code : codes) {
                final Permission permission = new Permission();
                permission.setId(UUID.randomUUID());
                permission.setCode(code);
                permission.setName(code);
                permission.setPermissionGroup(PermissionGroup.PATIENT);
                permission.setAction(PermissionAction.READ);
                permissions.add(permission);
            }
            return permissions;
        });
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final List<Role> roles = provisioner.provisionDefaultRoles(tenantId);

        assertThat(roles).hasSize(DefaultTenantRoleCatalog.definitions().size());
        assertThat(roles).extracting(Role::getType)
                .containsExactlyElementsOf(DefaultTenantRoleCatalog.definitions().keySet());
        assertThat(roles).allSatisfy(role -> {
            assertThat(role.getTenantId()).isEqualTo(tenantId);
            assertThat(role.isSystemRole()).isFalse();
            assertThat(role.getPermissions()).isNotEmpty();
            assertThat(role.getHierarchyLevel()).isEqualTo(RoleHierarchy.levelOf(role.getType()));
            assertThat(role.isAssignable()).isEqualTo(RoleHierarchy.isAssignable(role.getType()));
        });
        final Role hospitalAdmin = roles.stream()
                .filter(role -> role.getType() == RoleType.HOSPITAL_ADMIN)
                .findFirst()
                .orElseThrow();
        assertThat(hospitalAdmin.getParentRole()).isNull();
        assertThat(roles.stream().filter(role -> role.getType() != RoleType.HOSPITAL_ADMIN))
                .allSatisfy(role -> assertThat(role.getParentRole()).isSameAs(hospitalAdmin));
        verify(roleRepository, times(DefaultTenantRoleCatalog.definitions().size())).save(any(Role.class));
    }

    @Test
    @DisplayName("fails when roles were already provisioned")
    void provisionDefaultRoles_alreadyExists() {
        final UUID tenantId = UUID.randomUUID();
        when(roleRepository.existsByTenantIdAndType(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> provisioner.provisionDefaultRoles(tenantId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Default roles already exist");
    }
}
