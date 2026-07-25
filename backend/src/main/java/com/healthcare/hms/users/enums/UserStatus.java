package com.healthcare.hms.users.enums;

/**
 * Lifecycle status for platform and hospital users.
 *
 * <pre>
 * PENDING  → ACTIVE      (activate / email verify / invitation accept)
 * ACTIVE   → INACTIVE    (deactivate)
 * ACTIVE   → SUSPENDED   (suspend)
 * ACTIVE   → LOCKED      (security lock — not via admin lifecycle APIs)
 * INACTIVE → ACTIVE      (activate or restore)
 * SUSPENDED → ACTIVE     (restore)
 * LOCKED   → ACTIVE      (dedicated security unlock — not admin restore)
 * </pre>
 *
 * <p>User management APIs never physically delete accounts; they only change status.
 */
public enum UserStatus {
    /** Eligible to authenticate (when email verified). */
    ACTIVE,

    /** Administratively deactivated; cannot authenticate. */
    INACTIVE,

    /** Temporary administrative hold; cannot authenticate. */
    SUSPENDED,

    /** Security lock (failed auth / policy); cannot authenticate. */
    LOCKED,

    /** Onboarding incomplete; cannot authenticate until activated/verified. */
    PENDING
}
