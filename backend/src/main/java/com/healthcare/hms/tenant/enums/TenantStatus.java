package com.healthcare.hms.tenant.enums;

/**
 * Lifecycle status for a hospital (or clinic) tenant.
 *
 * <pre>
 * PENDING  → ACTIVE     (onboarding complete / admin verified)
 * ACTIVE   → SUSPENDED  (temporary platform hold)
 * SUSPENDED → ACTIVE    (reactivation)
 * ACTIVE   → INACTIVE   (graceful shutdown / offboarding)
 * INACTIVE → ACTIVE     (re-onboarding, rare)
 * *        → soft-delete (logical removal; never hard-delete clinical tenancy)
 * </pre>
 */
public enum TenantStatus {

    /** Registered; awaiting initial admin verification before staff login. */
    PENDING,

    /** Fully operational; tenant-scoped APIs may proceed. */
    ACTIVE,

    /** Temporarily blocked by platform; data retained, access denied. */
    SUSPENDED,

    /** Gracefully deactivated; retained for audit / compliance. */
    INACTIVE
}
