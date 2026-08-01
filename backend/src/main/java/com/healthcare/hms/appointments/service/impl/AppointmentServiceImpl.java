package com.healthcare.hms.appointments.service.impl;

import com.healthcare.hms.appointments.dto.request.AppointmentSearchCriteria;
import com.healthcare.hms.appointments.dto.request.CancelAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.CreateAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.RescheduleAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.UpdateAppointmentRequest;
import com.healthcare.hms.appointments.dto.response.AppointmentResponse;
import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.mapper.AppointmentMapper;
import com.healthcare.hms.appointments.queue.repository.QueueEntryRepository;
import com.healthcare.hms.appointments.reminder.service.AppointmentReminderLifecycle;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.appointments.repository.AppointmentSpecifications;
import com.healthcare.hms.appointments.service.AppointmentService;
import com.healthcare.hms.appointments.support.AppointmentActorScopeSupport;
import com.healthcare.hms.appointments.support.AppointmentBookingAccessSupport;
import com.healthcare.hms.appointments.support.AppointmentConflictGuard;
import com.healthcare.hms.appointments.support.AppointmentLabelEnricher;
import com.healthcare.hms.appointments.support.AppointmentNumberGenerator;
import com.healthcare.hms.appointments.support.DoctorAvailabilityBookingGuard;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Appointment booking lifecycle (Phase 6.3).
 *
 * <p>Healthcare workflow: only ACTIVE patients and ACTIVE doctors; slots must be
 * in the future (hospital TZ), inside published availability, and free of
 * doctor/patient overlaps for SCHEDULED/CONFIRMED rows.
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);
    private static final String ENTITY = "APPOINTMENT";
    private static final int MAX_NUMBER_ATTEMPTS = 5;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_SEARCH_TEXT_LENGTH = 100;
    private static final Set<String> ALLOWED_SORT = Set.of(
            "appointmentDate",
            "startTime",
            "endTime",
            "status",
            "visitType",
            "createdAt",
            "updatedAt",
            "appointmentNumber",
            "doctorId",
            "departmentId",
            "patientId"
    );

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentBookingAccessSupport accessSupport;
    private final AppointmentActorScopeSupport actorScopeSupport;
    private final AppointmentConflictGuard conflictGuard;
    private final DoctorAvailabilityBookingGuard availabilityGuard;
    private final AppointmentNumberGenerator numberGenerator;
    private final AuditLogService auditLogService;
    private final AppointmentReminderLifecycle reminderLifecycle;
    private final QueueEntryRepository queueEntryRepository;
    private final AppointmentLabelEnricher labelEnricher;

    public AppointmentServiceImpl(
            final AppointmentRepository appointmentRepository,
            final AppointmentMapper appointmentMapper,
            final AppointmentBookingAccessSupport accessSupport,
            final AppointmentActorScopeSupport actorScopeSupport,
            final AppointmentConflictGuard conflictGuard,
            final DoctorAvailabilityBookingGuard availabilityGuard,
            final AppointmentNumberGenerator numberGenerator,
            final AuditLogService auditLogService,
            final AppointmentReminderLifecycle reminderLifecycle,
            final QueueEntryRepository queueEntryRepository,
            final AppointmentLabelEnricher labelEnricher
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.accessSupport = accessSupport;
        this.actorScopeSupport = actorScopeSupport;
        this.conflictGuard = conflictGuard;
        this.availabilityGuard = availabilityGuard;
        this.numberGenerator = numberGenerator;
        this.auditLogService = auditLogService;
        this.reminderLifecycle = reminderLifecycle;
        this.queueEntryRepository = queueEntryRepository;
        this.labelEnricher = labelEnricher;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_CREATE)
    public AppointmentResponse create(
            final CreateAppointmentRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.assertDoctorAccessible(tenantId, request.doctorId());
        accessSupport.requireActivePatient(tenantId, request.patientId());
        final Doctor doctor = accessSupport.requireSchedulableDoctor(tenantId, request.doctorId());
        accessSupport.requireActiveDepartment(tenantId, request.departmentId(), doctor);
        final Hospital hospital = accessSupport.requireHospital(tenantId, doctor.getHospitalId());

        validateBookableSlot(
                tenantId,
                hospital,
                request.patientId(),
                request.doctorId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                null
        );

        final Appointment appointment = new Appointment();
        appointment.setHospitalId(doctor.getHospitalId());
        appointment.setAppointmentNumber(allocateNumber(tenantId));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentMapper.applyCreate(request, appointment);

        final Appointment saved = appointmentRepository.save(appointment);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        reminderLifecycle.onAppointmentBooked(saved, hospital);
        log.info(
                "Appointment booked id={} doctorId={} tenantId={} actorId={}",
                saved.getId(), saved.getDoctorId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public AppointmentResponse update(
            final UUID appointmentId,
            final UpdateAppointmentRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Appointment appointment = requireBookable(tenantId, appointmentId);
        actorScopeSupport.assertAppointmentAccessible(tenantId, appointment);
        final String old = snapshot(appointment);

        actorScopeSupport.assertDoctorAccessible(tenantId, request.doctorId());
        final Doctor doctor = accessSupport.requireSchedulableDoctor(tenantId, request.doctorId());
        accessSupport.requireActiveDepartment(tenantId, request.departmentId(), doctor);
        final Hospital hospital = accessSupport.requireHospital(tenantId, doctor.getHospitalId());
        if (!doctor.getHospitalId().equals(appointment.getHospitalId())) {
            throw new BusinessException(
                    "APPOINTMENT_HOSPITAL_IMMUTABLE",
                    "Cannot move an appointment to a doctor in a different hospital"
            );
        }

        validateBookableSlot(
                tenantId,
                hospital,
                appointment.getPatientId(),
                request.doctorId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                appointment.getId()
        );

        appointmentMapper.applyUpdate(request, appointment);
        final Appointment saved = appointmentRepository.save(appointment);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        reminderLifecycle.onAppointmentRescheduled(saved, hospital);
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public AppointmentResponse reschedule(
            final UUID appointmentId,
            final RescheduleAppointmentRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Appointment appointment = requireBookable(tenantId, appointmentId);
        actorScopeSupport.assertAppointmentAccessible(tenantId, appointment);
        final String old = snapshot(appointment);

        actorScopeSupport.assertDoctorAccessible(tenantId, request.doctorId());
        final Doctor doctor = accessSupport.requireSchedulableDoctor(tenantId, request.doctorId());
        accessSupport.requireActiveDepartment(tenantId, request.departmentId(), doctor);
        final Hospital hospital = accessSupport.requireHospital(tenantId, doctor.getHospitalId());
        if (!doctor.getHospitalId().equals(appointment.getHospitalId())) {
            throw new BusinessException(
                    "APPOINTMENT_HOSPITAL_IMMUTABLE",
                    "Cannot reschedule an appointment to a doctor in a different hospital"
            );
        }

        validateBookableSlot(
                tenantId,
                hospital,
                appointment.getPatientId(),
                request.doctorId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                appointment.getId()
        );

        appointmentMapper.applyReschedule(request, appointment);
        final Appointment saved = appointmentRepository.save(appointment);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        reminderLifecycle.onAppointmentRescheduled(saved, hospital);
        log.info("Appointment rescheduled id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId());
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public AppointmentResponse cancel(
            final UUID appointmentId,
            final CancelAppointmentRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Appointment appointment = requireBookable(tenantId, appointmentId);
        actorScopeSupport.assertAppointmentAccessible(tenantId, appointment);
        final String old = snapshot(appointment);
        final String reason = request == null ? null : trimToNull(request.reason());
        try {
            appointment.cancel(reason);
        } catch (final IllegalStateException ex) {
            throw new BusinessException("APPOINTMENT_INVALID_TRANSITION", ex.getMessage());
        }
        final Appointment saved = appointmentRepository.save(appointment);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        terminateActiveQueueEntries(tenantId, saved.getId());
        reminderLifecycle.onAppointmentCancelled(saved);
        log.info("Appointment cancelled id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId());
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public AppointmentResponse confirm(
            final UUID appointmentId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Appointment appointment = require(tenantId, appointmentId);
        actorScopeSupport.assertAppointmentAccessible(tenantId, appointment);
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            return labelEnricher.enrichOne(tenantId, appointment);
        }
        final String old = snapshot(appointment);
        try {
            appointment.confirm();
        } catch (final IllegalStateException ex) {
            throw new BusinessException("APPOINTMENT_INVALID_TRANSITION", ex.getMessage());
        }
        final Appointment saved = appointmentRepository.save(appointment);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info("Appointment confirmed id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId());
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public AppointmentResponse getById(
            final UUID appointmentId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Appointment appointment = require(tenantId, appointmentId);
        actorScopeSupport.assertAppointmentAccessible(tenantId, appointment);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                appointment.getTenantId(),
                actorId,
                ENTITY,
                appointment.getId().toString(),
                AuditAction.VIEW,
                null,
                "{id=" + appointment.getId() + "}",
                ipAddress,
                userAgent
        );
        return labelEnricher.enrichOne(tenantId, appointment);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public PageResponse<AppointmentResponse> search(
            final AppointmentSearchCriteria criteria,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final AppointmentSearchCriteria scoped = actorScopeSupport.constrainSearch(
                tenantId,
                sanitizeSearchCriteria(criteria)
        );
        validateDateRange(scoped);
        final var page = appointmentRepository.findAll(
                AppointmentSpecifications.withFilters(tenantId, scoped),
                sanitizePageable(pageable)
        );
        final List<AppointmentResponse> content = labelEnricher.enrich(tenantId, page.getContent());
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private static AppointmentSearchCriteria sanitizeSearchCriteria(final AppointmentSearchCriteria criteria) {
        return new AppointmentSearchCriteria(
                truncate(criteria.appointmentNumber(), MAX_SEARCH_TEXT_LENGTH),
                criteria.patientId(),
                truncate(criteria.patientName(), MAX_SEARCH_TEXT_LENGTH),
                criteria.doctorId(),
                truncate(criteria.doctorName(), MAX_SEARCH_TEXT_LENGTH),
                criteria.departmentId(),
                truncate(criteria.departmentName(), MAX_SEARCH_TEXT_LENGTH),
                criteria.status(),
                criteria.visitType(),
                criteria.fromDate(),
                criteria.toDate(),
                criteria.queueStatus()
        );
    }

    private static String truncate(final String value, final int maxLength) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed.isEmpty() ? null : trimmed;
        }
        return trimmed.substring(0, maxLength);
    }

    private static void validateDateRange(final AppointmentSearchCriteria criteria) {
        if (criteria.fromDate() != null
                && criteria.toDate() != null
                && criteria.toDate().isBefore(criteria.fromDate())) {
            throw new BusinessException("INVALID_DATE_RANGE", "toDate must be on or after fromDate");
        }
    }

    private void validateBookableSlot(
            final UUID tenantId,
            final Hospital hospital,
            final UUID patientId,
            final UUID doctorId,
            final LocalDate date,
            final LocalTime startTime,
            final LocalTime endTime,
            final UUID excludeId
    ) {
        accessSupport.assertNotInPast(hospital, date, startTime);
        conflictGuard.assertNoConflictsUnderLock(
                tenantId, patientId, doctorId, date, startTime, endTime, excludeId);
        availabilityGuard.assertSlotAvailable(tenantId, doctorId, date, startTime, endTime, excludeId);
    }

    private String allocateNumber(final UUID tenantId) {
        for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {
            final String candidate = numberGenerator.next();
            if (!appointmentRepository.existsByTenantIdAndAppointmentNumberIgnoreCase(tenantId, candidate)) {
                return candidate;
            }
        }
        throw new ConflictException(
                "APPOINTMENT_NUMBER_COLLISION",
                "Unable to allocate a unique appointment number; please retry"
        );
    }

    private Appointment requireBookable(final UUID tenantId, final UUID appointmentId) {
        final Appointment appointment = require(tenantId, appointmentId);
        if (!appointment.isBookableSlot()) {
            throw new BusinessException(
                    "APPOINTMENT_NOT_MUTABLE",
                    "Only SCHEDULED or CONFIRMED appointments can be changed (status="
                            + appointment.getStatus() + ")"
            );
        }
        return appointment;
    }

    private Appointment require(final UUID tenantId, final UUID appointmentId) {
        return appointmentRepository.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    /**
     * Cancelling an appointment must clear any live OPD queue position so staff
     * cannot continue consultation for a cancelled booking.
     */
    private void terminateActiveQueueEntries(final UUID tenantId, final UUID appointmentId) {
        queueEntryRepository.findByTenantIdAndAppointmentId(tenantId, appointmentId).ifPresent(entry -> {
            if (entry.isTerminal()) {
                return;
            }
            final UUID actorId = SecurityUtils.requireCurrentUserId();
            entry.cancel();
            entry.markDeleted(actorId);
            queueEntryRepository.save(entry);
            log.info(
                    "Terminated queue entry after appointment cancel entryId={} appointmentId={} tenantId={}",
                    entry.getId(),
                    appointmentId,
                    tenantId
            );
        });
    }

    private static Pageable sanitizePageable(final Pageable pageable) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "appointmentDate").and(Sort.by(Sort.Direction.ASC, "startTime"))
            );
        }

        final Sort safeSort = Sort.by(pageable.getSort().stream()
                .filter(order -> ALLOWED_SORT.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList());

        if (safeSort.isUnsorted()) {
            throw new BusinessException(
                    "INVALID_SORT",
                    "Sort must be one of: " + String.join(", ", ALLOWED_SORT)
            );
        }
        return PageRequest.of(page, size, safeSort);
    }

    private void audit(
            final Appointment appointment,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                appointment.getTenantId(),
                actorId,
                ENTITY,
                appointment.getId().toString(),
                action,
                oldSnapshot,
                snapshot(appointment),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final Appointment appointment) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", appointment.getId());
        fields.put("appointmentNumber", appointment.getAppointmentNumber());
        fields.put("patientId", appointment.getPatientId());
        fields.put("doctorId", appointment.getDoctorId());
        fields.put("departmentId", appointment.getDepartmentId());
        fields.put("appointmentDate", appointment.getAppointmentDate());
        fields.put("startTime", appointment.getStartTime());
        fields.put("endTime", appointment.getEndTime());
        fields.put("status", appointment.getStatus());
        fields.put("deleted", appointment.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
