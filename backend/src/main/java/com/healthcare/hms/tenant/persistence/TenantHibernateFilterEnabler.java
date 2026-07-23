package com.healthcare.hms.tenant.persistence;

import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.common.persistence.TenantPersistence;
import com.healthcare.hms.security.authorization.PlatformPrincipalSupport;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import jakarta.persistence.EntityManager;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Enables / disables the Hibernate {@link TenantPersistence#FILTER_NAME} on the
 * current persistence {@link Session} from {@link TenantContextHolder}.
 *
 * <p>When no tenant is bound (public auth flows, Super Admin without a tenant header),
 * the filter stays disabled so login and platform lookups continue to work.
 */
@Component
public class TenantHibernateFilterEnabler {

    private static final Logger log = LoggerFactory.getLogger(TenantHibernateFilterEnabler.class);

    /**
     * Enables the tenant filter when a request tenant is present; otherwise leaves
     * the session unfiltered.
     */
    public void enableForCurrentTenant(final EntityManager entityManager) {
        final Session session = entityManager.unwrap(Session.class);
        final UUID tenantId = TenantContextHolder.getTenantId().orElse(null);
        if (tenantId == null) {
            if (session.getEnabledFilter(TenantPersistence.FILTER_NAME) != null) {
                session.disableFilter(TenantPersistence.FILTER_NAME);
            }
            return;
        }

        final Filter filter = session.enableFilter(TenantPersistence.FILTER_NAME);
        filter.setParameter(TenantPersistence.PARAM_TENANT_ID, tenantId);
    }

    /**
     * Temporarily disables the tenant filter for an explicitly authorized platform
     * operation (Super Admin / bootstrap only), then restores prior enablement.
     *
     * <p><strong>Security:</strong> callers must pass a non-blank {@code reason} that is
     * logged. Do not use this for ordinary hospital traffic — prefer binding the correct
     * tenant in {@link TenantContextHolder} instead. Hibernate filters never apply to
     * native SQL; native queries remain forbidden for tenant-owned tables.
     *
     * @param reason short audit label explaining why cross-tenant visibility is required
     */
    public <T> T executeWithoutTenantFilter(
            final EntityManager entityManager,
            final String reason,
            final Supplier<T> action
    ) {
        Objects.requireNonNull(action, "action");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A non-blank reason is required to disable the tenant filter");
        }

        final AuthenticatedUser actor = SecurityUtils.findAuthenticatedUser()
                .orElseThrow(UnauthorizedException::new);
        PlatformPrincipalSupport.requirePlatformSuperAdmin(actor);

        final Session session = entityManager.unwrap(Session.class);
        final boolean wasEnabled = session.getEnabledFilter(TenantPersistence.FILTER_NAME) != null;
        final UUID previousTenantId = TenantContextHolder.getTenantId().orElse(null);

        log.warn(
                "Temporarily disabling Hibernate tenantFilter reason={} previousTenantId={}",
                reason,
                previousTenantId
        );

        if (wasEnabled) {
            session.disableFilter(TenantPersistence.FILTER_NAME);
        }
        try {
            return action.get();
        } finally {
            if (wasEnabled && previousTenantId != null) {
                final Filter filter = session.enableFilter(TenantPersistence.FILTER_NAME);
                filter.setParameter(TenantPersistence.PARAM_TENANT_ID, previousTenantId);
            }
        }
    }
}
