package com.healthcare.hms.security.authorization;

import com.healthcare.hms.common.exception.authorization.PermissionDeniedException;
import com.healthcare.hms.common.exception.authorization.RoleDeniedException;
import com.healthcare.hms.security.principal.CurrentUser;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Default {@link AuthorizationService} — delegates decisions to {@link PermissionEvaluator}
 * and principal lookup to {@link CurrentUserAccessor}.
 *
 * <p>Stateless service bean; thread-safety comes from immutable principals and
 * request-scoped SecurityContext access.
 */
@Service("authz")
public class DefaultAuthorizationService implements AuthorizationService {

    private final CurrentUserAccessor currentUserAccessor;
    private final PermissionEvaluator permissionEvaluator;
    private final PermissionResolver permissionResolver;

    public DefaultAuthorizationService(
            final CurrentUserAccessor currentUserAccessor,
            final PermissionEvaluator permissionEvaluator,
            final PermissionResolver permissionResolver
    ) {
        this.currentUserAccessor = currentUserAccessor;
        this.permissionEvaluator = permissionEvaluator;
        this.permissionResolver = permissionResolver;
    }

    @Override
    public boolean hasPermission(final String permission) {
        return currentUserAccessor.findCurrentUser()
                .map(user -> hasPermission(user, permission))
                .orElse(false);
    }

    @Override
    public boolean hasAnyPermission(final String... permissions) {
        return currentUserAccessor.findCurrentUser()
                .map(user -> hasAnyPermission(user, permissions))
                .orElse(false);
    }

    @Override
    public boolean hasAllPermissions(final String... permissions) {
        return currentUserAccessor.findCurrentUser()
                .map(user -> hasAllPermissions(user, permissions))
                .orElse(false);
    }

    @Override
    public boolean hasRole(final String role) {
        return currentUserAccessor.findCurrentUser()
                .map(user -> hasRole(user, role))
                .orElse(false);
    }

    @Override
    public boolean hasAnyRole(final String... roles) {
        return currentUserAccessor.findCurrentUser()
                .map(user -> hasAnyRole(user, roles))
                .orElse(false);
    }

    @Override
    public boolean hasAllRoles(final String... roles) {
        return currentUserAccessor.findCurrentUser()
                .map(user -> hasAllRoles(user, roles))
                .orElse(false);
    }

    @Override
    public boolean hasPermission(final CurrentUser user, final String permission) {
        return permissionEvaluator.hasPermission(user, permission);
    }

    @Override
    public boolean hasAnyPermission(final CurrentUser user, final String... permissions) {
        return permissionEvaluator.hasAnyPermission(user, permissions);
    }

    @Override
    public boolean hasAllPermissions(final CurrentUser user, final String... permissions) {
        return permissionEvaluator.hasAllPermissions(user, permissions);
    }

    @Override
    public boolean hasRole(final CurrentUser user, final String role) {
        return permissionEvaluator.hasRole(user, role);
    }

    @Override
    public boolean hasAnyRole(final CurrentUser user, final String... roles) {
        return permissionEvaluator.hasAnyRole(user, roles);
    }

    @Override
    public boolean hasAllRoles(final CurrentUser user, final String... roles) {
        return permissionEvaluator.hasAllRoles(user, roles);
    }

    @Override
    public CurrentUser requireAuthenticated() {
        return currentUserAccessor.requireCurrentUser();
    }

    @Override
    public void requirePermission(final String... permissions) {
        requireAnyPermission(permissions);
    }

    @Override
    public void requireAnyPermission(final String... permissions) {
        final CurrentUser user = requireAuthenticated();
        if (!hasAnyPermission(user, permissions)) {
            throw new PermissionDeniedException(toSet(permissions), false);
        }
    }

    @Override
    public void requireAllPermissions(final String... permissions) {
        final CurrentUser user = requireAuthenticated();
        if (!hasAllPermissions(user, permissions)) {
            throw new PermissionDeniedException(toSet(permissions), true);
        }
    }

    @Override
    public void requireAnyRole(final String... roles) {
        final CurrentUser user = requireAuthenticated();
        if (!hasAnyRole(user, roles)) {
            throw new RoleDeniedException(toSet(roles), false);
        }
    }

    @Override
    public void requireAllRoles(final String... roles) {
        final CurrentUser user = requireAuthenticated();
        if (!hasAllRoles(user, roles)) {
            throw new RoleDeniedException(toSet(roles), true);
        }
    }

    @Override
    public Set<String> currentPermissions() {
        return currentUserAccessor.findCurrentUser()
                .map(permissionResolver::resolvePermissions)
                .orElseGet(Set::of);
    }

    @Override
    public Set<String> currentRoles() {
        return currentUserAccessor.findCurrentUser()
                .map(permissionResolver::resolveRoles)
                .orElseGet(Set::of);
    }

    private static Set<String> toSet(final String... values) {
        if (values == null || values.length == 0) {
            return Set.of();
        }
        return Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
