package com.healthcare.hms.hospitals.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.dto.request.UpdateHospitalSettingsRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalSettingsResponse;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.mapper.HospitalSettingsMapper;
import com.healthcare.hms.hospitals.repository.HospitalRepository;
import com.healthcare.hms.hospitals.service.HospitalSettingsService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and updates the current tenant's default hospital settings.
 *
 * <p>Isolation is enforced by {@link TenantContextHolder} plus the Hibernate
 * {@code tenantFilter} on {@link Hospital}. Lookups never accept a client-supplied
 * hospital or tenant id.
 */
@Service
public class HospitalSettingsServiceImpl implements HospitalSettingsService {

    private static final Logger log = LoggerFactory.getLogger(HospitalSettingsServiceImpl.class);
    private static final String ENTITY_HOSPITAL = "HOSPITAL";

    private final HospitalRepository hospitalRepository;
    private final HospitalSettingsMapper hospitalSettingsMapper;
    private final AuditLogService auditLogService;

    public HospitalSettingsServiceImpl(
            final HospitalRepository hospitalRepository,
            final HospitalSettingsMapper hospitalSettingsMapper,
            final AuditLogService auditLogService
    ) {
        this.hospitalRepository = hospitalRepository;
        this.hospitalSettingsMapper = hospitalSettingsMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.HOSPITAL_READ)
    public HospitalSettingsResponse getSettings() {
        return hospitalSettingsMapper.toResponse(requireDefaultHospital());
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.HOSPITAL_UPDATE)
    public HospitalSettingsResponse updateSettings(
            final UpdateHospitalSettingsRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final Hospital hospital = requireDefaultHospital();
        final String oldSnapshot = settingsSnapshot(hospital);

        final String normalizedName = request.name().trim();
        if (hospitalRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(
                hospital.getTenantId(),
                normalizedName,
                hospital.getId()
        )) {
            throw new ConflictException("HOSPITAL_NAME_EXISTS", "Hospital name is already in use for this tenant");
        }

        hospitalSettingsMapper.updateEntity(request, hospital);
        final Hospital saved = hospitalRepository.save(hospital);

        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_HOSPITAL,
                saved.getId().toString(),
                AuditAction.UPDATE,
                oldSnapshot,
                settingsSnapshot(saved),
                ipAddress,
                userAgent
        );

        log.info(
                "Hospital settings updated hospitalId={} tenantId={} actorId={}",
                saved.getId(),
                saved.getTenantId(),
                actorId
        );

        return hospitalSettingsMapper.toResponse(saved);
    }

    private Hospital requireDefaultHospital() {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return hospitalRepository.findByTenantIdAndDefaultHospitalTrue(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Default hospital settings not found for tenant"));
    }

    private static String settingsSnapshot(final Hospital hospital) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("name", hospital.getName());
        fields.put("description", hospital.getDescription());
        fields.put("logoUrl", hospital.getLogoUrl());
        fields.put("timezone", hospital.getTimezone());
        fields.put("currency", hospital.getCurrency());
        fields.put("language", hospital.getLanguage());
        fields.put("email", hospital.getEmail());
        fields.put("phone", hospital.getPhone());
        fields.put("secondaryPhone", hospital.getSecondaryPhone());
        fields.put("website", hospital.getWebsite());
        fields.put("address", hospital.getAddress());
        fields.put("city", hospital.getCity());
        fields.put("stateProvince", hospital.getStateProvince());
        fields.put("country", hospital.getCountry());
        fields.put("postalCode", hospital.getPostalCode());
        fields.put("workingHours", hospital.getWorkingHours());
        return fields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + Objects.toString(entry.getValue(), ""))
                .collect(Collectors.joining("; "));
    }
}
