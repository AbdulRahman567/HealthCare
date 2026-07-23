package com.healthcare.hms.users.rbac;

import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Canonical permission naming convention for HMS RBAC.
 *
 * <p><strong>Format:</strong> {@code {PERMISSION_GROUP}_{PERMISSION_ACTION}}
 * using uppercase snake segments that match the enum constant names.
 *
 * <p>Examples: {@code PATIENT_READ}, {@code PATIENT_CREATE}, {@code PATIENT_UPDATE},
 * {@code HOSPITAL_UPDATE}.
 *
 * <p>Codes are immutable catalog keys. New permissions must be added via Flyway seed
 * and {@link com.healthcare.hms.users.constant.PermissionConstants} — never invented at runtime.
 */
public final class PermissionNaming {

    public static final char SEPARATOR = '_';

    private PermissionNaming() {
    }

    /**
     * Builds the canonical permission code for a group/action pair.
     */
    public static String code(final PermissionGroup group, final PermissionAction action) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(action, "action");
        return group.name() + SEPARATOR + action.name();
    }

    /**
     * Returns true when {@code code} parses to a known group and action.
     */
    public static boolean isValid(final String code) {
        return parseGroup(code).isPresent() && parseAction(code).isPresent();
    }

    public static Optional<PermissionGroup> parseGroup(final String code) {
        final String normalized = normalize(code);
        if (normalized == null) {
            return Optional.empty();
        }
        final int separator = normalized.lastIndexOf(SEPARATOR);
        if (separator <= 0 || separator >= normalized.length() - 1) {
            return Optional.empty();
        }
        try {
            return Optional.of(PermissionGroup.valueOf(normalized.substring(0, separator)));
        } catch (final IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static Optional<PermissionAction> parseAction(final String code) {
        final String normalized = normalize(code);
        if (normalized == null) {
            return Optional.empty();
        }
        final int separator = normalized.lastIndexOf(SEPARATOR);
        if (separator <= 0 || separator >= normalized.length() - 1) {
            return Optional.empty();
        }
        try {
            return Optional.of(PermissionAction.valueOf(normalized.substring(separator + 1)));
        } catch (final IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static PermissionGroup requireGroup(final String code) {
        return parseGroup(code).orElseThrow(() -> new IllegalArgumentException(
                "Invalid permission code group: " + code
        ));
    }

    public static PermissionAction requireAction(final String code) {
        return parseAction(code).orElseThrow(() -> new IllegalArgumentException(
                "Invalid permission code action: " + code
        ));
    }

    private static String normalize(final String code) {
        if (code == null) {
            return null;
        }
        final String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}
