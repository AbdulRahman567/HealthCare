package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.annotation.RequiresPermission;
import com.healthcare.hms.security.annotation.RequiresRole;
import com.healthcare.hms.security.authorization.PermissionAnnotationSupport.PermissionRequirement;
import org.springframework.stereotype.Component;

/**
 * Reusable permission-based authorization guard for declarative and programmatic checks.
 *
 * <p>Controllers and services must not implement authorization branching themselves —
 * call this guard (or rely on {@link RequirePermission} / interceptor / aspect).
 *
 * <p><strong>Programmatic usage (rare — prefer annotations):</strong>
 * <pre>{@code
 * @Service
 * public class VisitServiceImpl implements VisitService {
 *     private final PermissionGuard permissionGuard;
 *
 *     public void archive(UUID visitId) {
 *         permissionGuard.requireAny(PermissionConstants.VISIT_DELETE);
 *         // ...
 *     }
 * }
 * }</pre>
 */
@Component
public class PermissionGuard {

    private final AuthorizationService authorizationService;

    public PermissionGuard(final AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Enforces a resolved {@link PermissionRequirement} (default deny on empty list).
     */
    public void enforce(final PermissionRequirement requirement) {
        if (requirement == null || requirement.isEmpty()) {
            authorizationService.requireAuthenticated();
            return;
        }
        if (requirement.requireAll()) {
            authorizationService.requireAllPermissions(requirement.permissions());
        } else {
            authorizationService.requireAnyPermission(requirement.permissions());
        }
    }

    public void enforce(final RequirePermission annotation) {
        if (annotation == null) {
            authorizationService.requireAuthenticated();
            return;
        }
        enforce(PermissionRequirement.from(annotation));
    }

    /**
     * @deprecated Prefer {@link #enforce(RequirePermission)}.
     */
    @Deprecated(since = "3.3", forRemoval = false)
    public void enforce(final RequiresPermission annotation) {
        if (annotation == null) {
            authorizationService.requireAuthenticated();
            return;
        }
        enforce(PermissionRequirement.from(annotation));
    }

    public void enforce(final RequiresRole annotation) {
        if (annotation == null || annotation.value().length == 0) {
            authorizationService.requireAuthenticated();
            return;
        }
        if (annotation.requireAll()) {
            authorizationService.requireAllRoles(annotation.value());
        } else {
            authorizationService.requireAnyRole(annotation.value());
        }
    }

    public void requireAny(final String... permissions) {
        authorizationService.requireAnyPermission(permissions);
    }

    public void requireAll(final String... permissions) {
        authorizationService.requireAllPermissions(permissions);
    }

    public boolean allowsAny(final String... permissions) {
        return authorizationService.hasAnyPermission(permissions);
    }

    public boolean allowsAll(final String... permissions) {
        return authorizationService.hasAllPermissions(permissions);
    }

    public void requireAuthenticated() {
        authorizationService.requireAuthenticated();
    }
}
