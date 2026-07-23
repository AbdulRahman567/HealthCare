package com.healthcare.hms.users.enums;

/**
 * Allowed actions that may be granted on a {@link PermissionGroup}.
 *
 * <p>Canonical permission codes are {@code {GROUP}_{ACTION}} (for example
 * {@code PATIENT_READ}, {@code PATIENT_CREATE}, {@code PATIENT_UPDATE},
 * {@code PATIENT_DELETE}).
 *
 * <p>Phase 3.5 standardized on {@link #UPDATE} (replacing legacy {@code WRITE}).
 */
public enum PermissionAction {
    /** View / list / retrieve a resource. */
    READ,
    /** Create a new resource. */
    CREATE,
    /** Update an existing resource. */
    UPDATE,
    /** Soft-delete or cancel a resource. */
    DELETE
}
