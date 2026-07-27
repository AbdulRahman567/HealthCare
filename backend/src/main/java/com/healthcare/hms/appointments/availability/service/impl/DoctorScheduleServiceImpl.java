package com.healthcare.hms.appointments.availability.service.impl;

import com.healthcare.hms.appointments.availability.dto.request.ScheduleBreakRequest;
import com.healthcare.hms.appointments.availability.dto.request.ScheduleWindowRequest;
import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorScheduleRequest;
import com.healthcare.hms.appointments.availability.dto.response.DoctorScheduleResponse;
import com.healthcare.hms.appointments.availability.entity.DoctorSchedule;
import com.healthcare.hms.appointments.availability.entity.DoctorScheduleBreak;
import com.healthcare.hms.appointments.availability.entity.DoctorScheduleWindow;
import com.healthcare.hms.appointments.availability.enums.ScheduleStatus;
import com.healthcare.hms.appointments.availability.mapper.DoctorAvailabilityMapper;
import com.healthcare.hms.appointments.availability.repository.DoctorScheduleBreakRepository;
import com.healthcare.hms.appointments.availability.repository.DoctorScheduleRepository;
import com.healthcare.hms.appointments.availability.repository.DoctorScheduleWindowRepository;
import com.healthcare.hms.appointments.availability.service.DoctorScheduleService;
import com.healthcare.hms.appointments.availability.support.AvailabilityOverlapGuard;
import com.healthcare.hms.appointments.availability.support.DoctorAvailabilityAccessSupport;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.authorization.PermissionGuard;
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
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private static final Logger log = LoggerFactory.getLogger(DoctorScheduleServiceImpl.class);
    private static final String ENTITY = "DOCTOR_SCHEDULE";

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorScheduleWindowRepository windowRepository;
    private final DoctorScheduleBreakRepository breakRepository;
    private final DoctorAvailabilityAccessSupport accessSupport;
    private final AvailabilityOverlapGuard overlapGuard;
    private final DoctorAvailabilityMapper mapper;
    private final AuditLogService auditLogService;
    private final PermissionGuard permissionGuard;

    public DoctorScheduleServiceImpl(
            final DoctorScheduleRepository scheduleRepository,
            final DoctorScheduleWindowRepository windowRepository,
            final DoctorScheduleBreakRepository breakRepository,
            final DoctorAvailabilityAccessSupport accessSupport,
            final AvailabilityOverlapGuard overlapGuard,
            final DoctorAvailabilityMapper mapper,
            final AuditLogService auditLogService,
            final PermissionGuard permissionGuard
    ) {
        this.scheduleRepository = scheduleRepository;
        this.windowRepository = windowRepository;
        this.breakRepository = breakRepository;
        this.accessSupport = accessSupport;
        this.overlapGuard = overlapGuard;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.permissionGuard = permissionGuard;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_CREATE)
    public DoctorScheduleResponse create(
            final UUID doctorId,
            final UpsertDoctorScheduleRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Doctor doctor = accessSupport.requireDoctor(tenantId, doctorId);
        final ScheduleStatus status = request.status() != null ? request.status() : ScheduleStatus.ACTIVE;
        if (status == ScheduleStatus.INACTIVE) {
            permissionGuard.requireAny(PermissionConstants.APPOINTMENT_DELETE);
        }

        overlapGuard.assertNoOverlappingActiveSchedule(
                tenantId, doctorId, request.effectiveFrom(), request.effectiveTo(), status, null);

        final DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(doctor.getId());
        schedule.setHospitalId(doctor.getHospitalId());
        mapper.applySchedule(request, schedule);
        final DoctorSchedule saved = scheduleRepository.save(schedule);

        replaceChildren(saved, request.windows(), request.breaks());
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info("Doctor schedule created id={} doctorId={} tenantId={}", saved.getId(), doctorId, tenantId);
        return toResponse(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public DoctorScheduleResponse update(
            final UUID doctorId,
            final UUID scheduleId,
            final UpsertDoctorScheduleRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        accessSupport.requireDoctor(tenantId, doctorId);
        final DoctorSchedule schedule = require(tenantId, doctorId, scheduleId);
        final String old = snapshot(schedule);
        final ScheduleStatus status = request.status() != null ? request.status() : ScheduleStatus.ACTIVE;
        assertScheduleDeactivationAllowed(schedule.getStatus(), status);

        overlapGuard.assertNoOverlappingActiveSchedule(
                tenantId, doctorId, request.effectiveFrom(), request.effectiveTo(), status, schedule.getId());

        mapper.applySchedule(request, schedule);
        final DoctorSchedule saved = scheduleRepository.save(schedule);
        replaceChildren(saved, request.windows(), request.breaks());
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        return toResponse(tenantId, saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public DoctorScheduleResponse getById(final UUID doctorId, final UUID scheduleId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        accessSupport.requireDoctor(tenantId, doctorId);
        return toResponse(tenantId, require(tenantId, doctorId, scheduleId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public List<DoctorScheduleResponse> list(final UUID doctorId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        accessSupport.requireDoctor(tenantId, doctorId);
        return scheduleRepository.findByTenantIdAndDoctorIdOrderByEffectiveFromDesc(tenantId, doctorId).stream()
                .map(schedule -> toResponse(tenantId, schedule))
                .toList();
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_DELETE)
    public void delete(
            final UUID doctorId,
            final UUID scheduleId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        accessSupport.requireDoctor(tenantId, doctorId);
        final DoctorSchedule schedule = require(tenantId, doctorId, scheduleId);
        final String old = snapshot(schedule);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        softDeleteChildren(tenantId, schedule.getId(), actorId);
        schedule.markDeleted(actorId);
        scheduleRepository.save(schedule);
        audit(schedule, AuditAction.DELETE, old, ipAddress, userAgent);
    }

    /**
     * Deactivating a schedule (ACTIVE → INACTIVE) is equivalent to removing capacity
     * and requires {@link PermissionConstants#APPOINTMENT_DELETE}, matching soft-delete RBAC.
     */
    private void assertScheduleDeactivationAllowed(
            final ScheduleStatus current,
            final ScheduleStatus requested
    ) {
        if (current == ScheduleStatus.ACTIVE && requested == ScheduleStatus.INACTIVE) {
            permissionGuard.requireAny(PermissionConstants.APPOINTMENT_DELETE);
        }
    }

    private void replaceChildren(
            final DoctorSchedule schedule,
            final List<ScheduleWindowRequest> windows,
            final List<ScheduleBreakRequest> breaks
    ) {
        final UUID tenantId = schedule.getTenantId();
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        softDeleteChildren(tenantId, schedule.getId(), actorId);

        final List<ScheduleWindowRequest> safeWindows = windows == null ? List.of() : windows;
        windowRepository.saveAll(safeWindows.stream()
                .map(window -> mapper.toWindow(window, schedule))
                .toList());

        final List<ScheduleBreakRequest> safeBreaks = breaks == null ? List.of() : breaks;
        breakRepository.saveAll(safeBreaks.stream()
                .map(brk -> mapper.toBreak(brk, schedule))
                .toList());
    }

    private void softDeleteChildren(final UUID tenantId, final UUID scheduleId, final UUID actorId) {
        final List<DoctorScheduleWindow> windows =
                windowRepository.findByTenantIdAndScheduleIdOrderByDayOfWeekAscStartTimeAsc(tenantId, scheduleId);
        for (final DoctorScheduleWindow window : windows) {
            window.markDeleted(actorId);
        }
        windowRepository.saveAll(windows);

        final List<DoctorScheduleBreak> breaks =
                breakRepository.findByTenantIdAndScheduleIdOrderByDayOfWeekAscStartTimeAsc(tenantId, scheduleId);
        for (final DoctorScheduleBreak brk : breaks) {
            brk.markDeleted(actorId);
        }
        breakRepository.saveAll(breaks);
    }

    private DoctorScheduleResponse toResponse(final UUID tenantId, final DoctorSchedule schedule) {
        final List<DoctorScheduleWindow> windows =
                windowRepository.findByTenantIdAndScheduleIdOrderByDayOfWeekAscStartTimeAsc(tenantId, schedule.getId());
        final List<DoctorScheduleBreak> breaks =
                breakRepository.findByTenantIdAndScheduleIdOrderByDayOfWeekAscStartTimeAsc(tenantId, schedule.getId());
        return mapper.toScheduleResponse(schedule, windows, breaks);
    }

    private DoctorSchedule require(final UUID tenantId, final UUID doctorId, final UUID scheduleId) {
        return scheduleRepository.findByIdAndTenantIdAndDoctorId(scheduleId, tenantId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found"));
    }

    private void audit(
            final DoctorSchedule schedule,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                schedule.getTenantId(),
                actorId,
                ENTITY,
                schedule.getId().toString(),
                action,
                oldSnapshot,
                snapshot(schedule),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final DoctorSchedule schedule) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", schedule.getId());
        fields.put("doctorId", schedule.getDoctorId());
        fields.put("effectiveFrom", schedule.getEffectiveFrom());
        fields.put("effectiveTo", schedule.getEffectiveTo());
        fields.put("maxAppointmentsPerDay", schedule.getMaxAppointmentsPerDay());
        fields.put("status", schedule.getStatus());
        fields.put("recurrenceType", schedule.getRecurrenceType());
        fields.put("deleted", schedule.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
