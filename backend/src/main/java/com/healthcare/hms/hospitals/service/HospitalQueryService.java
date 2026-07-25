package com.healthcare.hms.hospitals.service;

import com.healthcare.hms.hospitals.entity.Hospital;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-side hospital lookups for other modules (keeps repository access inside hospitals).
 */
public interface HospitalQueryService {

    /**
     * Returns the current tenant's default hospital profile.
     *
     * @throws com.healthcare.hms.common.exception.ResourceNotFoundException if missing
     */
    Hospital requireDefaultHospital();

    /**
     * Returns the current tenant's default hospital id.
     */
    UUID requireDefaultHospitalId();

    /**
     * Tenant-scoped hospital lookup (safe for public flows that already know tenantId).
     */
    Optional<Hospital> findByIdAndTenantId(UUID hospitalId, UUID tenantId);
}
