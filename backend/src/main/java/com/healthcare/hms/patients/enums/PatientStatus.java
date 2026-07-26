package com.healthcare.hms.patients.enums;

/**
 * Operational lifecycle of a patient registration within a tenant.
 *
 * <pre>
 * ACTIVE   → INACTIVE   (temporarily not seeking care / moved)
 * INACTIVE → ACTIVE     (re-activated)
 * ACTIVE | INACTIVE → DECEASED
 * ACTIVE | INACTIVE → ARCHIVED   (admin archive; retain chart)
 * ARCHIVED → ACTIVE | INACTIVE   (restore)
 * *        → soft-delete (logical removal; MRN slot freed)
 * </pre>
 *
 * <p>Soft delete ({@code deleted = true}) is separate from status and is used
 * for mistaken registrations / compliance removal, not for deceased patients.
 */
public enum PatientStatus {

    /** Registered and eligible for appointments / clinical workflows. */
    ACTIVE,

    /** Temporarily not active; chart retained. */
    INACTIVE,

    /** Marked deceased; clinical history remains immutable for audit. */
    DECEASED,

    /** Administratively archived (e.g. transferred out); searchable when needed. */
    ARCHIVED
}
