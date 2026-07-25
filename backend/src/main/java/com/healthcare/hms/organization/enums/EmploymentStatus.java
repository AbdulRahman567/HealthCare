package com.healthcare.hms.organization.enums;

/**
 * Lifecycle status of a staff employment relationship within a hospital tenant.
 *
 * <pre>
 * PENDING    → ACTIVE       (onboarding / credentialing complete)
 * ACTIVE     → ON_LEAVE     (temporary absence; retains affiliation)
 * ON_LEAVE   → ACTIVE       (return from leave)
 * ACTIVE     → SUSPENDED    (disciplinary or compliance hold)
 * SUSPENDED  → ACTIVE       (clearance)
 * ACTIVE | ON_LEAVE | SUSPENDED → TERMINATED  (employment ended)
 * *          → soft-delete  (logical removal; never hard-delete staff rows)
 * </pre>
 */
public enum EmploymentStatus {

    /** Hired but not yet cleared for duty (credentialing / orientation). */
    PENDING,

    /** Currently employed and eligible for scheduled work. */
    ACTIVE,

    /** Temporarily away; still affiliated with the hospital. */
    ON_LEAVE,

    /** Access / scheduling blocked pending investigation or compliance. */
    SUSPENDED,

    /** Employment ended; retain historical assignments for audit. */
    TERMINATED
}
