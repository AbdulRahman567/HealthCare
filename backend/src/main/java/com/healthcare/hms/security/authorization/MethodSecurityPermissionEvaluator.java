package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.principal.CurrentUser;
import java.io.Serializable;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Spring Security {@link org.springframework.security.access.PermissionEvaluator}
 * bridge for {@code @PreAuthorize("hasPermission(...)")} expressions.
 *
 * <p>Phase 3.2 evaluates permission <em>codes</em> only. Domain-object ACL
 * (target id / type ownership) is reserved for later phases and fails closed
 * when a non-blank target type is supplied without a future policy.
 */
@Component("hmsPermissionEvaluator")
public class MethodSecurityPermissionEvaluator
        implements org.springframework.security.access.PermissionEvaluator {

    private final PermissionEvaluator permissionEvaluator;

    public MethodSecurityPermissionEvaluator(final PermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public boolean hasPermission(
            final Authentication authentication,
            final Object targetDomainObject,
            final Object permission
    ) {
        final CurrentUser user = extractUser(authentication);
        if (user == null) {
            return false;
        }
        final String code = toPermissionCode(permission);
        if (code == null) {
            return false;
        }
        return permissionEvaluator.hasPermission(user, code);
    }

    @Override
    public boolean hasPermission(
            final Authentication authentication,
            final Serializable targetId,
            final String targetType,
            final Object permission
    ) {
        // Domain-object ACL not implemented in Phase 3.2 — evaluate permission code only
        // when targetType is blank; otherwise fail closed.
        if (targetType != null && !targetType.isBlank()) {
            return false;
        }
        return hasPermission(authentication, targetId, permission);
    }

    private static CurrentUser extractUser(final Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser;
        }
        return null;
    }

    private static String toPermissionCode(final Object permission) {
        if (permission == null) {
            return null;
        }
        if (permission instanceof String code) {
            return code.isBlank() ? null : code.trim();
        }
        if (permission instanceof Collection<?> collection) {
            // SpEL sometimes passes a collection; require a single code.
            if (collection.size() != 1) {
                return null;
            }
            final Object only = collection.iterator().next();
            return only == null ? null : only.toString().trim();
        }
        final String asString = permission.toString();
        return asString.isBlank() ? null : asString.trim();
    }
}
