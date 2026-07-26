package com.healthcare.hms.patients.service;

import com.healthcare.hms.patients.entity.Patient;
import java.util.UUID;

/**
 * Read-side patient lookups for other modules (keeps repository access inside patients).
 */
public interface PatientQueryService {

    /**
     * Tenant-scoped patient lookup.
     *
     * @throws com.healthcare.hms.common.exception.ResourceNotFoundException if missing
     */
    Patient requireById(UUID tenantId, UUID patientId);
}
