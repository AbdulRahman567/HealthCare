package com.healthcare.hms.security.principal;

import java.util.Set;
import java.util.UUID;

/**
 * Abstraction over the authenticated security principal used by authorization infrastructure.
 *
 * <p>Implementations must be immutable and thread-safe once constructed. Controllers may
 * inject {@link com.healthcare.hms.security.annotation.CurrentUser}; services should depend
 * on this interface (or {@link com.healthcare.hms.security.authorization.CurrentUserAccessor}).
 */
public interface CurrentUser {

    UUID getUserId();

    /**
     * Hospital tenant id, or {@code null} for platform principals (e.g. Super Admin).
     */
    UUID getTenantId();

    String getEmail();

    /**
     * Role type names without {@code ROLE_} prefix (e.g. {@code HOSPITAL_ADMIN}).
     */
    Set<String> getRoles();

    /**
     * Effective permission codes (e.g. {@code PATIENT_READ}).
     */
    Set<String> getPermissions();

    long getTokenVersion();

    /**
     * {@code true} when the principal is not bound to a hospital tenant.
     */
    default boolean isPlatformPrincipal() {
        return getTenantId() == null;
    }

    default boolean hasRole(final String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        final String normalized = role.trim().toUpperCase();
        final String stripped = normalized.startsWith("ROLE_")
                ? normalized.substring("ROLE_".length())
                : normalized;
        return getRoles().contains(stripped) || getRoles().contains(normalized);
    }

    default boolean hasPermission(final String permission) {
        return permission != null && !permission.isBlank() && getPermissions().contains(permission.trim());
    }

    default boolean hasAnyPermission(final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        for (final String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    default boolean hasAllPermissions(final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        for (final String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }
}
