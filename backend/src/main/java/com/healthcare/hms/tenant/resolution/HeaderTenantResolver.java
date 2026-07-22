package com.healthcare.hms.tenant.resolution;

import com.healthcare.hms.security.SecurityConstants;
import com.healthcare.hms.tenant.exception.InvalidTenantIdentifierException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves tenant identity from the {@code X-Tenant-ID} request header.
 *
 * <p>Absent header → empty (request may be platform-scoped).
 * Present but non-UUID → {@link InvalidTenantIdentifierException}.
 * Multiple distinct values → rejected (header spoofing defence).
 */
@Component
@Order(TenantResolverOrders.HEADER)
public class HeaderTenantResolver implements TenantResolver {

    @Override
    public TenantIdentificationSource source() {
        return TenantIdentificationSource.REQUEST_HEADER;
    }

    @Override
    public Optional<UUID> resolveTenantId(final HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }

        final Enumeration<String> headers = request.getHeaders(SecurityConstants.TENANT_HEADER);
        if (headers == null || !headers.hasMoreElements()) {
            return Optional.empty();
        }

        String firstNonBlank = null;
        while (headers.hasMoreElements()) {
            final String candidate = headers.nextElement();
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            final String trimmed = candidate.trim();
            if (firstNonBlank == null) {
                firstNonBlank = trimmed;
            } else if (!firstNonBlank.equalsIgnoreCase(trimmed)) {
                throw new InvalidTenantIdentifierException(
                        "X-Tenant-ID must not supply conflicting values"
                );
            }
        }

        if (firstNonBlank == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(firstNonBlank));
        } catch (final IllegalArgumentException exception) {
            throw new InvalidTenantIdentifierException(
                    "X-Tenant-ID must be a valid UUID"
            );
        }
    }
}
