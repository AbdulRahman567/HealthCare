package com.healthcare.hms.hospitals.service.impl;

import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.repository.HospitalRepository;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HospitalQueryServiceImpl implements HospitalQueryService {

    private final HospitalRepository hospitalRepository;

    public HospitalQueryServiceImpl(final HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Hospital requireDefaultHospital() {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return hospitalRepository.findByTenantIdAndDefaultHospitalTrue(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Default hospital not found for tenant"));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID requireDefaultHospitalId() {
        return requireDefaultHospital().getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hospital> findByIdAndTenantId(final UUID hospitalId, final UUID tenantId) {
        return hospitalRepository.findByIdAndTenantId(hospitalId, tenantId);
    }
}
