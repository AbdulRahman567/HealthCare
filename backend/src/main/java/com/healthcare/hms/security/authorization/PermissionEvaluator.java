package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.principal.CurrentUser;

/**
 * Evaluates whether a principal satisfies role/permission requirements.
 *
 * <p>Pure decision API: returns booleans only. Enforcement (throwing) belongs in
 * {@link AuthorizationService}. Implementations must be stateless and thread-safe.
 */
public interface PermissionEvaluator {

    boolean hasPermission(CurrentUser user, String permission);

    boolean hasAnyPermission(CurrentUser user, String... permissions);

    boolean hasAllPermissions(CurrentUser user, String... permissions);

    boolean hasRole(CurrentUser user, String role);

    boolean hasAnyRole(CurrentUser user, String... roles);

    boolean hasAllRoles(CurrentUser user, String... roles);
}
