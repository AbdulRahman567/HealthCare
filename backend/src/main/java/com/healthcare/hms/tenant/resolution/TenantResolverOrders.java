package com.healthcare.hms.tenant.resolution;

/**
 * Relative order for {@link TenantResolver} strategies.
 * Lower values run first.
 */
public final class TenantResolverOrders {

    /** Primary Phase 2.2 strategy: {@code X-Tenant-ID} header. */
    public static final int HEADER = 100;

    /** Reserved for white-label / subdomain mapping (disabled until configured). */
    public static final int SUBDOMAIN = 200;

    private TenantResolverOrders() {
    }
}
