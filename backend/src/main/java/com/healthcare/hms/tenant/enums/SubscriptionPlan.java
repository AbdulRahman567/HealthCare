package com.healthcare.hms.tenant.enums;

/**
 * Commercial subscription tier attached to a tenant.
 * Billing enforcement is out of scope for Phase 2.1; the field is persisted for later phases.
 */
public enum SubscriptionPlan {
    BASIC,
    STANDARD,
    PREMIUM,
    ENTERPRISE
}
