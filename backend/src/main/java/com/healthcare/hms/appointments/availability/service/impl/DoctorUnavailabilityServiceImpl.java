package com.healthcare.hms.appointments.availability.service.impl;

import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorUnavailabilityRequest;
import com.healthcare.hms.appointments.availability.dto.response.DoctorUnavailabilityResponse;
import com.healthcare.hms.appointments.availability.entity.DoctorUnavailability;
import com.healthcare.hms.appointments.availability.enums.UnavailabilityType;
import com.healthcare.hms.appointments.availability.mapper.DoctorAvailabilityMapper;
import com.healthcare.hms.appointments.availability.repository.DoctorUnavailabilityRepository;
import com.healthcare.hms.appointments.availability.service.DoctorUnavailabilityService;
import com.healthcare.hms.appointments.availability.support.AvailabilityOverlapGuard;
import com.healthcare.hms.appointments.availability.support.DoctorAvailabilityAccessSupport;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorUnavailabilityServiceImpl implements DoctorUnavailabilityService {

    private static final Logger log = LoggerFactory.getLogger(DoctorUnavailabilityServiceImpl.class);
    private static final String ENTITY = "DOCTOR_UNAVAILABILITY";

    private final DoctorUnavailabilityRepository unavailabilityRepository;
    private final DoctorAvailabilityAccessSupport accessSupport;
    private final AvailabilityOverlapGuard overlapGuard;
    private final DoctorAvailabilityMapper mapper;
    private final AuditLogService auditLogService;

    public DoctorUnavailabilityServiceImpl(
            final DoctorUnavailabilityRepository unavailabilityRepository,
            final DoctorAvailabilityAccessSupport accessSupport,
            final AvailabilityOverlapGuard overlapGuard,
            final DoctorAvailabilityMapper mapper,
            final AuditLogService auditLogService
    ) {
        this.unavailabilityRepository = unavailabilityRepository;
        this.accessSupport = accessSupport;
        this.overlapGuard = overlapGuard;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_CREATE)
    public DoctorUnavailabilityResponse create(
            final UUID doctorId,
            final UpsertDoctorUnavailabilityRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Doctor doctor = accessSupport.requireDoctor(tenantId, doctorId);
        final boolean allDay = Boolean.TRUE.equals(request.allDay());

        overlapGuard.assertNoOverlappingUnavailability(
                tenantId,
                doctorId,
                request.startDate(),
                request.endDate(),
                allDay,
                request.startTime(),
                request.endTime(),
                null
        );

        final DoctorUnavailability entity = new DoctorUnavailability();
        entity.setDoctorId(doctor.getId());
        entity.setHospitalId(doctor.getHospitalId());
        mapper.applyUnavailability(request, entity);
        final DoctorUnavailability saved = unavailabilityRepository.save(entity);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info(
                "Doctor unavailability created id={} doctorId={} type={} tenantId={}",
                saved.getId(), doctorId, saved.getUnavailabilityType(), tenantId
        );
        return mapper.toUnavailabilityResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public DoctorUnavailabilityResponse update(
            final UUID doctorId,
            final UUID unavailabilityId,
            final UpsertDoctorUnavailabilityRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        accessSupport.requireDoctor(tenantId, doctorId);
        final DoctorUnavailability entity = require(tenantId, doctorId, unavailabilityId);
        final String old = snapshot(entity);
        final boolean allDay = Boolean.TRUE.equals(request.allDay());

        overlapGuard.assertNoOverlappingUnavailability(
                tenantId,
                doctorId,
                request.startDate(),
                request.endDate(),
                allDay,
                request.startTime(),
                request.endTime(),
                entity.getId()
        );

        mapper.applyUnavailability(request, entity);
        final DoctorUnavailability saved = unavailabilityRepository.save(entity);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        return mapper.toUnavailabilityResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public DoctorUnavailabilityResponse getById(final UUID doctorId, final UUID unavailabilityId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        accessSupport.requireDoctor(tenantId, doctorId);
        return mapper.toUnavailabilityResponse(require(tenantId, doctorId, unavailabilityId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public List<DoctorUnavailabilityResponse> list(final UUID doctorId, final UnavailabilityType type) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        accessSupport.requireDoctor(tenantId, doctorId);
        final List<DoctorUnavailability> rows = type == null
                ? unavailabilityRepository.findByTenantIdAndDoctorIdOrderByStartDateDesc(tenantId, doctorId)
                : unavailabilityRepository.findByTenantIdAndDoctorIdAndUnavailabilityTypeOrderByStartDateDesc(
                        tenantId, doctorId, type);
        return rows.stream().map(mapper::toUnavailabilityResponse).toList();
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_DELETE)
    public void delete(
            final UUID doctorId,
            final UUID unavailabilityId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        accessSupport.requireDoctor(tenantId, doctorId);
        final DoctorUnavailability entity = require(tenantId, doctorId, unavailabilityId);
        final String old = snapshot(entity);
        entity.markDeleted(SecurityUtils.requireCurrentUser().getUserId());
        unavailabilityRepository.save(entity);
        audit(entity, AuditAction.DELETE, old, ipAddress, userAgent);
    }

    private DoctorUnavailability require(final UUID tenantId, final UUID doctorId, final UUID id) {
        return unavailabilityRepository.findByIdAndTenantIdAndDoctorId(id, tenantId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor unavailability not found"));
    }

    private void audit(
            final DoctorUnavailability entity,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                entity.getTenantId(),
                actorId,
                ENTITY,
                entity.getId().toString(),
                action,
                oldSnapshot,
                snapshot(entity),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final DoctorUnavailability entity) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", entity.getId());
        fields.put("doctorId", entity.getDoctorId());
        fields.put("type", entity.getUnavailabilityType());
        fields.put("startDate", entity.getStartDate());
        fields.put("endDate", entity.getEndDate());
        fields.put("allDay", entity.isAllDay());
        fields.put("deleted", entity.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
