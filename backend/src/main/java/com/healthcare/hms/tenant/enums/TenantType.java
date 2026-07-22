package com.healthcare.hms.tenant.enums;

/**
 * Classification of a tenant organization on the shared HMS platform.
 * Extensible for future facility kinds without changing isolation strategy.
 */
public enum TenantType {

    /** Full-service hospital (default for HMS SaaS onboarding). */
    HOSPITAL,

    /** Outpatient or specialty clinic. */
    CLINIC,

    /** Multi-site hospital network treated as one billing/ops tenant. */
    HOSPITAL_GROUP
}
