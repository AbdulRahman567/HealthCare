package com.healthcare.hms.tenant.service.impl;

import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.exception.TenantNotActiveException;
import com.healthcare.hms.tenant.exception.TenantNotFoundException;
import com.healthcare.hms.tenant.repository.TenantRepository;
import com.healthcare.hms.tenant.service.TenantAccessService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default tenant validation service used by the request lifecycle
 * after resolution and before business logic.
 */
@Service
@Transactional(readOnly = true)
public class TenantAccessServiceImpl implements TenantAccessService {

    private final TenantRepository tenantRepository;

    public TenantAccessServiceImpl(final TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Tenant requireTenant(final UUID tenantId) {
        if (tenantId == null) {
            throw new TenantNotFoundException("Tenant id is required");
        }
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
    }

    @Override
    public Tenant requireActiveTenant(final UUID tenantId) {
        final Tenant tenant = requireTenant(tenantId);
        if (!tenant.isOperational()) {
            throw new TenantNotActiveException(tenant.getStatus());
        }
        return tenant;
    }

    @Override
    public TenantContext toContext(final Tenant tenant) {
        return new TenantContext(
                tenant.getId(),
                tenant.getSlug(),
                tenant.getTenantType(),
                tenant.getStatus()
        );
    }
}
