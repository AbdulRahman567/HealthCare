package com.healthcare.hms.tenant.resolution;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Placeholder for future subdomain / custom-domain tenant mapping.
 *
 * <p>Disabled by default ({@code hms.tenant.resolution.subdomain-enabled=false}).
 * When enabled in a later phase, this resolver will map {@code Host} /
 * subdomain labels to a tenant UUID without changing the filter pipeline.
 */
@Component
@Order(TenantResolverOrders.SUBDOMAIN)
public class SubdomainTenantResolver implements TenantResolver {

    private final boolean enabled;

    public SubdomainTenantResolver(
            @Value("${hms.tenant.resolution.subdomain-enabled:false}") final boolean enabled
    ) {
        this.enabled = enabled;
    }

    @Override
    public TenantIdentificationSource source() {
        return TenantIdentificationSource.SUBDOMAIN;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Optional<UUID> resolveTenantId(final HttpServletRequest request) {
        if (!enabled || request == null) {
            return Optional.empty();
        }

        // Future: parse Host / X-Forwarded-Host, map subdomain → tenant UUID.
        // Intentionally unimplemented in Phase 2.2 so the extension point is wired
        // without silently accepting untrusted host headers.
        return Optional.empty();
    }
}
