package com.healthcare.hms.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultPermissionResolver")
class DefaultPermissionResolverTest {

    private final DefaultPermissionResolver resolver = new DefaultPermissionResolver();

    @Test
    @DisplayName("resolves snapshot permissions from CurrentUser")
    void resolveFromCurrentUser() {
        final AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "n@h.test",
                Set.of(RoleType.NURSE.name()),
                Set.of(PermissionConstants.PATIENT_READ),
                1L
        );

        assertThat(resolver.resolveRoles(user)).containsExactly(RoleType.NURSE.name());
        assertThat(resolver.resolvePermissions(user)).containsExactly(PermissionConstants.PATIENT_READ);
    }

    @Test
    @DisplayName("resolves grants from persisted User roles")
    void resolveFromUserEntity() {
        final Permission permission = new Permission();
        permission.setCode(PermissionConstants.LAB_UPDATE);
        permission.setName("Write lab");
        permission.setPermissionGroup(PermissionGroup.LAB);
        permission.setAction(PermissionAction.UPDATE);

        final Role role = new Role();
        role.setType(RoleType.LAB_TECHNICIAN);
        role.setName("Lab Technician");
        role.applyCatalogHierarchy();
        role.addPermission(permission);

        final User user = new User();
        user.setId(UUID.randomUUID());
        user.setTenantId(UUID.randomUUID());
        user.setFirstName("Lab");
        user.setLastName("Tech");
        user.setEmail("lab@h.test");
        user.setPasswordHash("hash");
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(role);

        assertThat(resolver.resolveRoles(user)).containsExactly(RoleType.LAB_TECHNICIAN.name());
        assertThat(resolver.resolvePermissions(user)).containsExactly(PermissionConstants.LAB_UPDATE);
    }
}
