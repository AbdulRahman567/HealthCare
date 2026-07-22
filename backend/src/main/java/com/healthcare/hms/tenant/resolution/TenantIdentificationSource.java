package com.healthcare.hms.tenant.resolution;

/**
 * Ordered strategies for identifying which tenant a request belongs to.
 *
 * <p>Phase 2.2 activates {@link #REQUEST_HEADER} via {@link HeaderTenantResolver}.
 * {@link #SUBDOMAIN} is wired through {@link SubdomainTenantResolver} but disabled
 * until explicitly enabled.
 *
 * <p>JWT claim cross-checks remain an authentication concern and are intentionally
 * outside these resolvers.
 */
public enum TenantIdentificationSource {
    REQUEST_HEADER,
    SUBDOMAIN,
    JWT_CLAIM,
    EXPLICIT_PARAMETER
}
