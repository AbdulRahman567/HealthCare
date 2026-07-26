package com.healthcare.hms.patients.support;

import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.PatientStatus;
import com.healthcare.hms.patients.repository.PatientRepository;
import java.util.UUID;

/**
 * Shared patient lookup guards for nested clinical modules (Phase 5.9).
 */
public final class PatientAccessSupport {

    private PatientAccessSupport() {
    }

    public static Patient requirePatient(final PatientRepository repository, final UUID tenantId, final UUID patientId) {
        return repository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    /**
     * Clinical chart mutations require an ACTIVE patient (except lifecycle APIs).
     */
    public static Patient requireActivePatient(
            final PatientRepository repository,
            final UUID tenantId,
            final UUID patientId
    ) {
        final Patient patient = requirePatient(repository, tenantId, patientId);
        if (patient.getStatus() != PatientStatus.ACTIVE) {
            throw new BusinessException(
                    "PATIENT_NOT_ACTIVE",
                    "Patient must be ACTIVE to modify clinical chart records"
            );
        }
        return patient;
    }
}
