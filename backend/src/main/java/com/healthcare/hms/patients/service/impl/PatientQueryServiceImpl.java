package com.healthcare.hms.patients.service.impl;

import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.patients.service.PatientQueryService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientQueryServiceImpl implements PatientQueryService {

    private final PatientRepository patientRepository;

    public PatientQueryServiceImpl(final PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Patient requireById(final UUID tenantId, final UUID patientId) {
        return patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }
}
