package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.principal.CurrentUser;
import com.healthcare.hms.security.util.AuthorityUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Default-deny permission/role evaluation against a {@link CurrentUser} snapshot.
 *
 * <p>Checks both the typed role/permission sets and Spring {@link GrantedAuthority}
 * values when the principal also implements {@link UserDetails}.
 */
@Component
public class DefaultPermissionEvaluator implements PermissionEvaluator {

    private final PermissionResolver permissionResolver;

    public DefaultPermissionEvaluator(final PermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    @Override
    public boolean hasPermission(final CurrentUser user, final String permission) {
        Objects.requireNonNull(user, "user");
        final String normalized = AuthorityUtils.normalizePermission(permission);
        if (AuthorityUtils.isBlankAuthority(normalized)) {
            return false;
        }
        if (permissionResolver.resolvePermissions(user).contains(normalized)) {
            return true;
        }
        return hasAuthority(user, normalized);
    }

    @Override
    public boolean hasAnyPermission(final CurrentUser user, final String... permissions) {
        Objects.requireNonNull(user, "user");
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(user, permission));
    }

    @Override
    public boolean hasAllPermissions(final CurrentUser user, final String... permissions) {
        Objects.requireNonNull(user, "user");
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions).allMatch(permission -> hasPermission(user, permission));
    }

    @Override
    public boolean hasRole(final CurrentUser user, final String role) {
        Objects.requireNonNull(user, "user");
        final String normalized = AuthorityUtils.normalizeRole(role);
        if (AuthorityUtils.isBlankAuthority(normalized)) {
            return false;
        }
        final var roles = permissionResolver.resolveRoles(user);
        if (roles.contains(normalized)) {
            return true;
        }
        return hasAuthority(user, AuthorityUtils.toRoleAuthority(normalized));
    }

    @Override
    public boolean hasAnyRole(final CurrentUser user, final String... roles) {
        Objects.requireNonNull(user, "user");
        if (roles == null || roles.length == 0) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(role -> hasRole(user, role));
    }

    @Override
    public boolean hasAllRoles(final CurrentUser user, final String... roles) {
        Objects.requireNonNull(user, "user");
        if (roles == null || roles.length == 0) {
            return false;
        }
        return Arrays.stream(roles).allMatch(role -> hasRole(user, role));
    }

    private static boolean hasAuthority(final CurrentUser user, final String authority) {
        if (!(user instanceof UserDetails userDetails)) {
            return false;
        }
        final Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return authorities.stream().anyMatch(granted -> authority.equals(granted.getAuthority()));
    }
}
