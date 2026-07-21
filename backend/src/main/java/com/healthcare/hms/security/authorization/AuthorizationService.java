package com.healthcare.hms.security.authorization;

import com.healthcare.hms.common.exception.auth.ForbiddenException;
import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.SecurityConstants;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.security.util.SecurityUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Central authorization checks for roles and permissions.
 */
@Service("authz")
public class AuthorizationService {

    public boolean hasPermission(final String permission) {
        final AuthenticatedUser user = SecurityUtils.findCurrentUser().orElse(null);
        return user != null && hasPermission(user, permission);
    }

    public boolean hasAnyPermission(final String... permissions) {
        final AuthenticatedUser user = SecurityUtils.findCurrentUser().orElse(null);
        return user != null && hasAnyPermission(user, permissions);
    }

    public boolean hasAllPermissions(final String... permissions) {
        final AuthenticatedUser user = SecurityUtils.findCurrentUser().orElse(null);
        return user != null && hasAllPermissions(user, permissions);
    }

    public boolean hasRole(final String role) {
        final AuthenticatedUser user = SecurityUtils.findCurrentUser().orElse(null);
        return user != null && hasRole(user, role);
    }

    public boolean hasAnyRole(final String... roles) {
        final AuthenticatedUser user = SecurityUtils.findCurrentUser().orElse(null);
        return user != null && hasAnyRole(user, roles);
    }

    public boolean hasPermission(final AuthenticatedUser user, final String permission) {
        return user.getPermissions().contains(permission)
                || hasAuthority(user.getAuthorities(), permission);
    }

    public boolean hasAnyPermission(final AuthenticatedUser user, final String... permissions) {
        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(user, permission));
    }

    public boolean hasAllPermissions(final AuthenticatedUser user, final String... permissions) {
        return Arrays.stream(permissions).allMatch(permission -> hasPermission(user, permission));
    }

    public boolean hasRole(final AuthenticatedUser user, final String role) {
        final String normalized = normalizeRole(role);
        return user.getRoles().contains(stripRolePrefix(normalized))
                || user.getRoles().contains(normalized)
                || hasAuthority(user.getAuthorities(), withRolePrefix(normalized));
    }

    public boolean hasAnyRole(final AuthenticatedUser user, final String... roles) {
        return Arrays.stream(roles).anyMatch(role -> hasRole(user, role));
    }

    public boolean hasAllRoles(final AuthenticatedUser user, final String... roles) {
        return Arrays.stream(roles).allMatch(role -> hasRole(user, role));
    }

    public AuthenticatedUser requireAuthenticated() {
        return SecurityUtils.findCurrentUser()
                .orElseThrow(UnauthorizedException::new);
    }

    public void requirePermission(final String... permissions) {
        requireAnyPermission(permissions);
    }

    public void requireAnyPermission(final String... permissions) {
        final AuthenticatedUser user = requireAuthenticated();
        if (!hasAnyPermission(user, permissions)) {
            throw new ForbiddenException();
        }
    }

    public void requireAllPermissions(final String... permissions) {
        final AuthenticatedUser user = requireAuthenticated();
        if (!hasAllPermissions(user, permissions)) {
            throw new ForbiddenException();
        }
    }

    public void requireAnyRole(final String... roles) {
        final AuthenticatedUser user = requireAuthenticated();
        if (!hasAnyRole(user, roles)) {
            throw new ForbiddenException();
        }
    }

    public void requireAllRoles(final String... roles) {
        final AuthenticatedUser user = requireAuthenticated();
        if (!hasAllRoles(user, roles)) {
            throw new ForbiddenException();
        }
    }

    private static boolean hasAuthority(
            final Collection<? extends GrantedAuthority> authorities,
            final String authority
    ) {
        return authorities.stream().anyMatch(granted -> granted.getAuthority().equals(authority));
    }

    private static String normalizeRole(final String role) {
        return role == null ? "" : role.trim().toUpperCase();
    }

    private static String stripRolePrefix(final String role) {
        if (role.startsWith(SecurityConstants.ROLE_PREFIX)) {
            return role.substring(SecurityConstants.ROLE_PREFIX.length());
        }
        return role;
    }

    private static String withRolePrefix(final String role) {
        final String stripped = stripRolePrefix(role);
        return SecurityConstants.ROLE_PREFIX + stripped;
    }

    public Set<String> currentPermissions() {
        return SecurityUtils.findCurrentUser()
                .map(AuthenticatedUser::getPermissions)
                .orElse(Set.of());
    }
}
