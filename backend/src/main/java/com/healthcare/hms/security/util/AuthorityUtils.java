package com.healthcare.hms.security.util;

import com.healthcare.hms.security.SecurityConstants;
import java.util.Locale;
import java.util.Objects;

/**
 * Stateless helpers for normalizing Spring Security role/permission authorities.
 *
 * <p>Thread-safe; no mutable state.
 */
public final class AuthorityUtils {

    private AuthorityUtils() {
    }

    /**
     * Normalizes a role token to uppercase without the {@code ROLE_} prefix.
     */
    public static String normalizeRole(final String role) {
        if (role == null) {
            return "";
        }
        final String trimmed = role.trim().toUpperCase(Locale.ROOT);
        return stripRolePrefix(trimmed);
    }

    /**
     * Returns {@code ROLE_} + normalized role type (idempotent if prefix already present).
     */
    public static String toRoleAuthority(final String role) {
        final String normalized = normalizeRole(role);
        if (normalized.isEmpty()) {
            return SecurityConstants.ROLE_PREFIX;
        }
        return SecurityConstants.ROLE_PREFIX + normalized;
    }

    public static String stripRolePrefix(final String role) {
        Objects.requireNonNull(role, "role");
        if (role.startsWith(SecurityConstants.ROLE_PREFIX)) {
            return role.substring(SecurityConstants.ROLE_PREFIX.length());
        }
        return role;
    }

    public static boolean isRoleAuthority(final String authority) {
        return authority != null && authority.startsWith(SecurityConstants.ROLE_PREFIX);
    }

    /**
     * Trims permission codes; empty/null become empty string.
     */
    public static String normalizePermission(final String permission) {
        if (permission == null) {
            return "";
        }
        return permission.trim();
    }

    public static boolean isBlankAuthority(final String authority) {
        return authority == null || authority.isBlank();
    }
}
