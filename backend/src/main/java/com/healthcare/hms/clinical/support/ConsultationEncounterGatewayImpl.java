package com.healthcare.hms.clinical.support;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.queue.spi.ConsultationEncounterGateway;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.enums.ConsultationStatus;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.security.authorization.PermissionGuard;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Queue ↔ consultation encounter bridge (Phase 7.10).
 */
@Service
public class ConsultationEncounterGatewayImpl implements ConsultationEncounterGateway {

    private static final Logger log = LoggerFactory.getLogger(ConsultationEncounterGatewayImpl.class);
    private static final String ENTITY = "CONSULTATION";
    private static final Set<ConsultationStatus> ACTIVE = Set.of(
            ConsultationStatus.IN_PROGRESS,
            ConsultationStatus.PAUSED
    );

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationAccessSupport accessSupport;
    private final ConsultationActorScopeSupport actorScopeSupport;
    private final ConsultationNumberGenerator numberGenerator;
    private final PermissionGuard permissionGuard;
    private final AuditLogService auditLogService;

    public ConsultationEncounterGatewayImpl(
            final ConsultationRepository consultationRepository,
            final AppointmentRepository appointmentRepository,
            final ConsultationAccessSupport accessSupport,
            final ConsultationActorScopeSupport actorScopeSupport,
            final ConsultationNumberGenerator numberGenerator,
            final PermissionGuard permissionGuard,
            final AuditLogService auditLogService
    ) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.accessSupport = accessSupport;
        this.actorScopeSupport = actorScopeSupport;
        this.numberGenerator = numberGenerator;
        this.permissionGuard = permissionGuard;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public UUID ensureStartedForAppointment(
            final UUID appointmentId,
            final String ipAddress,
            final String userAgent
    ) {
        permissionGuard.requireAny(PermissionConstants.VISIT_CREATE, PermissionConstants.VISIT_UPDATE);
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Appointment appointment = appointmentRepository.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        actorScopeSupport.assertDoctorAccessible(tenantId, appointment.getDoctorId());

        final Optional<Consultation> existing =
                consultationRepository.findByTenantIdAndAppointmentId(tenantId, appointmentId);
        if (existing.isPresent()) {
            return resumeOrStartExisting(tenantId, existing.get(), ipAddress, userAgent);
        }

        accessSupport.requireActivePatient(tenantId, appointment.getPatientId());
        final Doctor doctor = accessSupport.requireConsultingDoctor(tenantId, appointment.getDoctorId());
        accessSupport.requireActiveDepartment(tenantId, appointment.getDepartmentId(), doctor);
        accessSupport.requireConsultableAppointment(
                tenantId,
                appointmentId,
                appointment.getPatientId(),
                appointment.getDoctorId()
        );
        assertNoOtherActiveConsultation(tenantId, appointment.getDoctorId(), null);

        final Consultation consultation = new Consultation();
        consultation.setHospitalId(appointment.getHospitalId());
        consultation.setConsultationNumber(allocateNumber(tenantId));
        consultation.setPatientId(appointment.getPatientId());
        consultation.setDoctorId(appointment.getDoctorId());
        consultation.setDepartmentId(appointment.getDepartmentId());
        consultation.setAppointmentId(appointmentId);
        consultation.setConsultationDate(LocalDate.now());
        consultation.setStatus(ConsultationStatus.DRAFT);
        consultation.setChiefComplaint(null);

        Consultation saved = consultationRepository.save(consultation);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        saved = startConsultation(tenantId, saved, ipAddress, userAgent);
        log.info(
                "Consultation auto-started from queue appointmentId={} consultationId={} tenantId={} actorId={}",
                appointmentId, saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findConsultationIdByAppointment(final UUID appointmentId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return consultationRepository.findByTenantIdAndAppointmentId(tenantId, appointmentId)
                .map(Consultation::getId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, UUID> findConsultationIdsByAppointments(final Collection<UUID> appointmentIds) {
        if (appointmentIds == null || appointmentIds.isEmpty()) {
            return Map.of();
        }
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final List<Consultation> consultations =
                consultationRepository.findByTenantIdAndAppointmentIdIn(tenantId, appointmentIds);
        final Map<UUID, UUID> result = new HashMap<>();
        for (final Consultation consultation : consultations) {
            if (consultation.getAppointmentId() != null) {
                result.put(consultation.getAppointmentId(), consultation.getId());
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOpenConsultation(final UUID appointmentId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return consultationRepository.findByTenantIdAndAppointmentId(tenantId, appointmentId)
                .map(Consultation::isEditable)
                .orElse(false);
    }

    private UUID resumeOrStartExisting(
            final UUID tenantId,
            final Consultation consultation,
            final String ipAddress,
            final String userAgent
    ) {
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        return switch (consultation.getStatus()) {
            case IN_PROGRESS -> consultation.getId();
            case DRAFT -> startConsultation(tenantId, consultation, ipAddress, userAgent).getId();
            case PAUSED -> {
                assertNoOtherActiveConsultation(tenantId, consultation.getDoctorId(), consultation.getId());
                final String old = snapshot(consultation);
                try {
                    consultation.resume();
                } catch (final IllegalStateException ex) {
                    throw new BusinessException("CONSULTATION_INVALID_TRANSITION", ex.getMessage());
                }
                final Consultation saved = consultationRepository.save(consultation);
                audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
                yield saved.getId();
            }
            case COMPLETED, CANCELLED -> throw new ConflictException(
                    "CONSULTATION_TERMINAL",
                    "A terminal consultation already exists for this appointment (status="
                            + consultation.getStatus() + ")"
            );
        };
    }

    private Consultation startConsultation(
            final UUID tenantId,
            final Consultation consultation,
            final String ipAddress,
            final String userAgent
    ) {
        assertNoOtherActiveConsultation(tenantId, consultation.getDoctorId(), consultation.getId());
        final String old = snapshot(consultation);
        try {
            consultation.start();
        } catch (final IllegalStateException ex) {
            throw new BusinessException("CONSULTATION_INVALID_TRANSITION", ex.getMessage());
        }
        final Consultation saved = consultationRepository.save(consultation);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        return saved;
    }

    private void assertNoOtherActiveConsultation(
            final UUID tenantId,
            final UUID doctorId,
            final UUID excludeId
    ) {
        final boolean conflict = excludeId == null
                ? consultationRepository.existsByTenantIdAndDoctorIdAndStatusIn(
                        tenantId, doctorId, ACTIVE)
                : consultationRepository.existsByTenantIdAndDoctorIdAndStatusInAndIdNot(
                        tenantId, doctorId, ACTIVE, excludeId);
        if (conflict) {
            throw new ConflictException(
                    "CONSULTATION_IN_PROGRESS",
                    "Doctor already has an active consultation (IN_PROGRESS or PAUSED)"
            );
        }
    }

    private String allocateNumber(final UUID tenantId) {
        for (int attempt = 0; attempt < 5; attempt++) {
            final String candidate = numberGenerator.next();
            if (!consultationRepository.existsByTenantIdAndConsultationNumberIgnoreCase(tenantId, candidate)) {
                return candidate;
            }
        }
        throw new ConflictException("CONSULTATION_NUMBER_COLLISION", "Unable to allocate consultation number");
    }

    private void audit(
            final Consultation consultation,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                consultation.getTenantId(),
                SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY,
                consultation.getId().toString(),
                action,
                oldSnapshot,
                snapshot(consultation),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final Consultation consultation) {
        return "{id=" + consultation.getId()
                + ", status=" + consultation.getStatus()
                + ", appointmentId=" + consultation.getAppointmentId()
                + ", patientId=" + consultation.getPatientId()
                + ", doctorId=" + consultation.getDoctorId()
                + "}";
    }
}
