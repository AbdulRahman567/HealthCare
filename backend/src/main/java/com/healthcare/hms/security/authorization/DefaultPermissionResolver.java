package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.principal.CurrentUser;
import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Default permission/role resolution — no mutable state; safe for concurrent use.
 */
@Component
public class DefaultPermissionResolver implements PermissionResolver {

    @Override
    public Set<String> resolvePermissions(final CurrentUser user) {
        Objects.requireNonNull(user, "user");
        return user.getPermissions();
    }

    @Override
    public Set<String> resolveRoles(final CurrentUser user) {
        Objects.requireNonNull(user, "user");
        return user.getRoles();
    }

    @Override
    public Set<String> resolvePermissions(final User user) {
        Objects.requireNonNull(user, "user");
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<String> resolveRoles(final User user) {
        Objects.requireNonNull(user, "user");
        return user.getRoles().stream()
                .map(Role::getType)
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
    }
}
