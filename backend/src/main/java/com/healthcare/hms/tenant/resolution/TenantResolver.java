package com.healthcare.hms.tenant.resolution;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Strategy that extracts a candidate tenant UUID from an inbound HTTP request.
 *
 * <p>Implementations must be side-effect free and must not perform authentication
 * or tenant persistence lookups — validation belongs to
 * {@link com.healthcare.hms.tenant.service.TenantAccessService}.
 *
 * <p>Multiple resolvers are composed by order ({@link TenantResolverOrders});
 * the first that returns a value wins.
 */
public interface TenantResolver {

    /**
     * Identification channel this strategy implements.
     */
    TenantIdentificationSource source();

    /**
     * Whether this strategy participates in resolution.
     * Disabled strategies are skipped (used to keep subdomain extensibility offline).
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Attempts to resolve a tenant id from the request.
     *
     * @return empty when this strategy cannot identify a tenant for the request
     */
    Optional<UUID> resolveTenantId(HttpServletRequest request);
}
