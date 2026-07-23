package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.principal.CurrentUser;
import java.util.Set;

/**
 * Central authorization facade for RBAC checks and enforcement.
 *
 * <p>Boolean methods never throw. {@code require*} methods throw authorization
 * exceptions (or unauthorized when no principal is present). Bean name {@code authz}
 * supports SpEL: {@code @PreAuthorize("@authz.hasPermission('PATIENT_READ')")}.
 */
public interface AuthorizationService {

    boolean hasPermission(String permission);

    boolean hasAnyPermission(String... permissions);

    boolean hasAllPermissions(String... permissions);

    boolean hasRole(String role);

    boolean hasAnyRole(String... roles);

    boolean hasAllRoles(String... roles);

    boolean hasPermission(CurrentUser user, String permission);

    boolean hasAnyPermission(CurrentUser user, String... permissions);

    boolean hasAllPermissions(CurrentUser user, String... permissions);

    boolean hasRole(CurrentUser user, String role);

    boolean hasAnyRole(CurrentUser user, String... roles);

    boolean hasAllRoles(CurrentUser user, String... roles);

    CurrentUser requireAuthenticated();

    void requirePermission(String... permissions);

    void requireAnyPermission(String... permissions);

    void requireAllPermissions(String... permissions);

    void requireAnyRole(String... roles);

    void requireAllRoles(String... roles);

    Set<String> currentPermissions();

    Set<String> currentRoles();
}
