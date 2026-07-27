package com.healthcare.hms.appointments.queue.service.impl;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.queue.dto.request.CheckInQueueRequest;
import com.healthcare.hms.appointments.queue.dto.request.QueueStatusUpdateRequest;
import com.healthcare.hms.appointments.queue.dto.response.DoctorDayQueueResponse;
import com.healthcare.hms.appointments.queue.dto.response.QueueEntryResponse;
import com.healthcare.hms.appointments.queue.entity.DoctorDayQueue;
import com.healthcare.hms.appointments.queue.entity.QueueEntry;
import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import com.healthcare.hms.appointments.queue.mapper.QueueMapper;
import com.healthcare.hms.appointments.queue.repository.DoctorDayQueueRepository;
import com.healthcare.hms.appointments.queue.repository.QueueEntryRepository;
import com.healthcare.hms.appointments.queue.service.QueueService;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.appointments.support.AppointmentActorScopeSupport;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Daily OPD queue per doctor (Phase 6.4).
 *
 * <p>One queue per doctor per day; automatic monotonic queue numbers; chronological
 * listing by queue number; every status change is audit-logged.
 */
@Service
public class QueueServiceImpl implements QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueServiceImpl.class);
    private static final String ENTITY_QUEUE = "DOCTOR_DAY_QUEUE";
    private static final String ENTITY_ENTRY = "QUEUE_ENTRY";

    private final DoctorDayQueueRepository queueRepository;
    private final QueueEntryRepository entryRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final QueueMapper queueMapper;
    private final AuditLogService auditLogService;
    private final AppointmentActorScopeSupport actorScopeSupport;

    public QueueServiceImpl(
            final DoctorDayQueueRepository queueRepository,
            final QueueEntryRepository entryRepository,
            final AppointmentRepository appointmentRepository,
            final DoctorRepository doctorRepository,
            final QueueMapper queueMapper,
            final AuditLogService auditLogService,
            final AppointmentActorScopeSupport actorScopeSupport
    ) {
        this.queueRepository = queueRepository;
        this.entryRepository = entryRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.queueMapper = queueMapper;
        this.auditLogService = auditLogService;
        this.actorScopeSupport = actorScopeSupport;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public QueueEntryResponse checkIn(
            final CheckInQueueRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Appointment appointment = appointmentRepository.findByIdAndTenantId(request.appointmentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        actorScopeSupport.assertAppointmentAccessible(tenantId, appointment);

        if (!appointment.isBookableSlot()) {
            throw new BusinessException(
                    "APPOINTMENT_NOT_CHECKINABLE",
                    "Only SCHEDULED or CONFIRMED appointments can check in (status="
                            + appointment.getStatus() + ")"
            );
        }
        if (entryRepository.existsByTenantIdAndAppointmentId(tenantId, appointment.getId())) {
            throw new ConflictException(
                    "ALREADY_CHECKED_IN",
                    "This appointment is already on the doctor's queue"
            );
        }

        final Doctor doctor = doctorRepository.findByIdAndTenantId(appointment.getDoctorId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        final LocalDate queueDate = appointment.getAppointmentDate();
        final DoctorDayQueue queue = getOrCreateQueue(tenantId, doctor, queueDate, ipAddress, userAgent);
        // Re-lock for number allocation
        final DoctorDayQueue locked = queueRepository.findForUpdate(tenantId, doctor.getId(), queueDate)
                .orElse(queue);

        final Instant now = Instant.now();
        final int number = locked.allocateNextNumber();
        queueRepository.save(locked);

        final QueueEntry entry = new QueueEntry();
        entry.setQueueId(locked.getId());
        entry.setAppointmentId(appointment.getId());
        entry.setPatientId(appointment.getPatientId());
        entry.setDoctorId(appointment.getDoctorId());
        entry.setHospitalId(appointment.getHospitalId());
        entry.setQueueNumber(number);
        entry.setStatus(QueueEntryStatus.CHECKED_IN);
        entry.setCheckedInAt(now);
        entry.setStatusChangedAt(now);
        entry.setNotes(trimToNull(request.notes()));

        try {
            final QueueEntry saved = entryRepository.save(entry);
            auditEntry(saved, AuditAction.CREATE, null, "CHECK_IN", ipAddress, userAgent);
            log.info(
                    "Queue check-in entryId={} queueNumber={} doctorId={} date={} tenantId={}",
                    saved.getId(), number, doctor.getId(), queueDate, tenantId
            );
            return queueMapper.toEntryResponse(saved);
        } catch (final DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "ALREADY_CHECKED_IN",
                    "This appointment is already on the doctor's queue"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public DoctorDayQueueResponse getDailyQueue(final UUID doctorId, final LocalDate date) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.assertDoctorAccessible(tenantId, doctorId);
        requireDoctor(tenantId, doctorId);
        final LocalDate queueDate = date == null ? LocalDate.now() : date;
        final DoctorDayQueue queue = queueRepository
                .findByTenantIdAndDoctorIdAndQueueDate(tenantId, doctorId, queueDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No queue found for this doctor on " + queueDate));
        return toQueueResponse(tenantId, queue);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public DoctorDayQueueResponse getQueueById(final UUID queueId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final DoctorDayQueue queue = requireQueue(tenantId, queueId);
        actorScopeSupport.assertDoctorAccessible(tenantId, queue.getDoctorId());
        return toQueueResponse(tenantId, queue);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public QueueEntryResponse getEntryById(final UUID entryId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final QueueEntry entry = requireEntry(tenantId, entryId);
        actorScopeSupport.assertDoctorAccessible(tenantId, entry.getDoctorId());
        return queueMapper.toEntryResponse(entry);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public QueueEntryResponse markWaiting(
            final UUID entryId,
            final QueueStatusUpdateRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        return transition(
                entryId,
                request,
                QueueEntry::markWaiting,
                "WAITING",
                ipAddress,
                userAgent,
                null,
                true
        );
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public QueueEntryResponse startConsultation(
            final UUID entryId,
            final QueueStatusUpdateRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final QueueEntry entry = requireEntry(tenantId, entryId);
        actorScopeSupport.assertDoctorAccessible(tenantId, entry.getDoctorId());
        requireBookableAppointment(tenantId, entry.getAppointmentId());
        final DoctorDayQueue queue = requireQueue(tenantId, entry.getQueueId());
        queueRepository.findForUpdate(tenantId, queue.getDoctorId(), queue.getQueueDate())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor day queue not found"));
        if (entryRepository.existsByTenantIdAndQueueIdAndStatus(
                tenantId, entry.getQueueId(), QueueEntryStatus.IN_CONSULTATION)) {
            throw new ConflictException(
                    "CONSULTATION_IN_PROGRESS",
                    "Another patient is already IN_CONSULTATION for this doctor today"
            );
        }
        return transition(entryId, request, QueueEntry::startConsultation, "IN_CONSULTATION", ipAddress, userAgent, null, false);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public QueueEntryResponse complete(
            final UUID entryId,
            final QueueStatusUpdateRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        return transition(entryId, request, QueueEntry::complete, "COMPLETED", ipAddress, userAgent, appointment -> {
            if (appointment.isBookableSlot()) {
                appointment.complete();
                appointmentRepository.save(appointment);
            }
        }, true);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public QueueEntryResponse markMissed(
            final UUID entryId,
            final QueueStatusUpdateRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        return transition(entryId, request, QueueEntry::markMissed, "MISSED", ipAddress, userAgent, appointment -> {
            if (appointment.isBookableSlot()) {
                appointment.markMissed();
                appointmentRepository.save(appointment);
            }
        }, true);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    public QueueEntryResponse cancel(
            final UUID entryId,
            final QueueStatusUpdateRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        // Soft-delete frees the appointment unique slot so the patient can re-check-in.
        // Appointment itself stays SCHEDULED/CONFIRMED (queue removal ≠ appointment cancel).
        return transition(
                entryId,
                request,
                entry -> {
                    entry.cancel();
                    entry.markDeleted(SecurityUtils.requireCurrentUserId());
                },
                "CANCELLED",
                ipAddress,
                userAgent,
                null,
                false
        );
    }

    private QueueEntryResponse transition(
            final UUID entryId,
            final QueueStatusUpdateRequest request,
            final Consumer<QueueEntry> action,
            final String eventName,
            final String ipAddress,
            final String userAgent,
            final Consumer<Appointment> appointmentSync,
            final boolean requireBookableAppointment
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final QueueEntry entry = requireEntry(tenantId, entryId);
        actorScopeSupport.assertDoctorAccessible(tenantId, entry.getDoctorId());
        if (requireBookableAppointment) {
            requireBookableAppointment(tenantId, entry.getAppointmentId());
        }
        if (entry.isTerminal()) {
            throw new BusinessException(
                    "QUEUE_ENTRY_TERMINAL",
                    "Queue entry is already terminal (status=" + entry.getStatus() + ")"
            );
        }
        final String old = snapshot(entry);
        try {
            action.accept(entry);
        } catch (final IllegalStateException ex) {
            throw new BusinessException("QUEUE_INVALID_TRANSITION", ex.getMessage());
        }
        if (request != null && request.notes() != null) {
            final String notes = trimToNull(request.notes());
            if (notes != null) {
                entry.setNotes(notes);
            }
        }
        final QueueEntry saved = entryRepository.save(entry);
        if (appointmentSync != null) {
            appointmentRepository.findByIdAndTenantId(saved.getAppointmentId(), tenantId)
                    .ifPresent(appointmentSync);
        }
        auditEntry(saved, AuditAction.UPDATE, old, eventName, ipAddress, userAgent);
        log.info("Queue {} entryId={} number={} tenantId={}", eventName, saved.getId(), saved.getQueueNumber(), tenantId);
        return queueMapper.toEntryResponse(saved);
    }

    private void requireBookableAppointment(final UUID tenantId, final UUID appointmentId) {
        final Appointment appointment = appointmentRepository.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        if (!appointment.isBookableSlot()) {
            throw new BusinessException(
                    "APPOINTMENT_NOT_QUEUEABLE",
                    "Appointment is not SCHEDULED/CONFIRMED (status=" + appointment.getStatus() + ")"
            );
        }
    }

    private DoctorDayQueue getOrCreateQueue(
            final UUID tenantId,
            final Doctor doctor,
            final LocalDate queueDate,
            final String ipAddress,
            final String userAgent
    ) {
        return queueRepository.findByTenantIdAndDoctorIdAndQueueDate(tenantId, doctor.getId(), queueDate)
                .orElseGet(() -> {
                    final DoctorDayQueue created = new DoctorDayQueue();
                    created.setDoctorId(doctor.getId());
                    created.setHospitalId(doctor.getHospitalId());
                    created.setQueueDate(queueDate);
                    created.setLastQueueNumber(0);
                    try {
                        final DoctorDayQueue saved = queueRepository.saveAndFlush(created);
                        auditQueue(saved, AuditAction.CREATE, ipAddress, userAgent);
                        return saved;
                    } catch (final DataIntegrityViolationException ex) {
                        return queueRepository.findByTenantIdAndDoctorIdAndQueueDate(
                                        tenantId, doctor.getId(), queueDate)
                                .orElseThrow(() -> ex);
                    }
                });
    }

    private DoctorDayQueueResponse toQueueResponse(final UUID tenantId, final DoctorDayQueue queue) {
        final List<QueueEntry> entries =
                entryRepository.findByTenantIdAndQueueIdOrderByQueueNumberAsc(tenantId, queue.getId());
        return queueMapper.toQueueResponse(queue, entries);
    }

    private DoctorDayQueue requireQueue(final UUID tenantId, final UUID queueId) {
        return queueRepository.findByIdAndTenantId(queueId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor day queue not found"));
    }

    private QueueEntry requireEntry(final UUID tenantId, final UUID entryId) {
        return entryRepository.findByIdAndTenantId(entryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));
    }

    private Doctor requireDoctor(final UUID tenantId, final UUID doctorId) {
        return doctorRepository.findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private void auditEntry(
            final QueueEntry entry,
            final AuditAction action,
            final String oldSnapshot,
            final String eventName,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        final String neu = snapshot(entry) + ", event=" + eventName;
        auditLogService.record(
                entry.getTenantId(),
                actorId,
                ENTITY_ENTRY,
                entry.getId().toString(),
                action,
                oldSnapshot,
                neu,
                ipAddress,
                userAgent
        );
    }

    private void auditQueue(
            final DoctorDayQueue queue,
            final AuditAction action,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                queue.getTenantId(),
                actorId,
                ENTITY_QUEUE,
                queue.getId().toString(),
                action,
                null,
                "doctorId=" + queue.getDoctorId() + ", date=" + queue.getQueueDate(),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final QueueEntry entry) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", entry.getId());
        fields.put("queueId", entry.getQueueId());
        fields.put("appointmentId", entry.getAppointmentId());
        fields.put("queueNumber", entry.getQueueNumber());
        fields.put("status", entry.getStatus());
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
