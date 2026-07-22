package com.healthcare.hms.tenant.resolution;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;

/**
 * Composes ordered {@link TenantResolver} strategies to extract a candidate tenant id.
 * Existence / ACTIVE checks belong to {@link com.healthcare.hms.tenant.validation.TenantValidation}.
 */
@Service
public class TenantResolutionService {

    private final List<TenantResolver> resolvers;

    public TenantResolutionService(final List<TenantResolver> resolvers) {
        final List<TenantResolver> ordered = new ArrayList<>(resolvers);
        AnnotationAwareOrderComparator.sort(ordered);
        this.resolvers = List.copyOf(ordered);
    }

    /**
     * Resolves a tenant id using the first enabled strategy that returns a value.
     */
    public Optional<UUID> resolveTenantId(final HttpServletRequest request) {
        for (final TenantResolver resolver : resolvers) {
            if (!resolver.isEnabled()) {
                continue;
            }
            final Optional<UUID> resolved = resolver.resolveTenantId(request);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
        return Optional.empty();
    }
}
