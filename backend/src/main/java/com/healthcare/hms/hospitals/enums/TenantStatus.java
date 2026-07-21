package com.healthcare.hms.hospitals.enums;

/**
 * Lifecycle status for hospital tenants.
 */
public enum TenantStatus {
    /** Hospital registered; awaiting initial admin verification before staff login. */
    PENDING,
    ACTIVE,
    SUSPENDED,
    INACTIVE
}
