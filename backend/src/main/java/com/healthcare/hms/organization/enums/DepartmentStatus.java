package com.healthcare.hms.organization.enums;

/**
 * Operational lifecycle of a hospital department.
 *
 * <pre>
 * ACTIVE     → INACTIVE   (temporarily closed / not scheduling)
 * INACTIVE   → ACTIVE     (reopened)
 * ACTIVE | INACTIVE → SUSPENDED  (compliance or admin hold)
 * SUSPENDED  → ACTIVE | INACTIVE
 * *          → soft-delete (logical removal)
 * </pre>
 */
public enum DepartmentStatus {

    /** Open and available for staffing / scheduling. */
    ACTIVE,

    /** Intentionally closed; retain history. */
    INACTIVE,

    /** Administrative or compliance hold. */
    SUSPENDED
}
