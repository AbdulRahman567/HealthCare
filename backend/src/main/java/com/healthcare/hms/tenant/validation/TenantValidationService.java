package com.healthcare.hms.tenant.validation;

import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.exception.TenantMismatchException;
import com.healthcare.hms.tenant.service.TenantAccessService;
import com.healthcare.hms.users.enums.RoleType;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Default tenant validation used by {@link com.healthcare.hms.tenant.web.TenantFilter}.
 */
@Component
public class TenantValidationService implements TenantValidation {

    private final TenantAccessService tenantAccessService;

    public TenantValidationService(final TenantAccessService tenantAccessService) {
        this.tenantAccessService = tenantAccessService;
    }

    @Override
    public TenantContext validate(final UUID tenantId) {
        assertMatchesAuthenticatedPrincipal(tenantId);
        final Tenant tenant = tenantAccessService.requireActiveTenant(tenantId);
        return tenantAccessService.toContext(tenant);
    }

    /**
     * Hospital users may only bind their own tenant. Platform actors with
     * {@code tenantId == null} may bind a header tenant only when they hold
     * {@code SUPER_ADMIN} (same trust bar as unscoped platform bypass).
     */
    private void assertMatchesAuthenticatedPrincipal(final UUID resolvedTenantId) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return;
        }

        final UUID principalTenantId = user.getTenantId();
        if (principalTenantId != null) {
            if (!principalTenantId.equals(resolvedTenantId)) {
                throw TenantMismatchException.headerDoesNotMatchPrincipal();
            }
            return;
        }

        if (!user.getRoles().contains(RoleType.Names.SUPER_ADMIN)) {
            throw TenantMismatchException.headerDoesNotMatchPrincipal();
        }
    }
}
