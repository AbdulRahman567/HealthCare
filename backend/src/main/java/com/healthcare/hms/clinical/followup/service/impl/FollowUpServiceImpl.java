package com.healthcare.hms.clinical.followup.service.impl;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.entity.FollowUp;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import com.healthcare.hms.clinical.followup.dto.request.CreateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.request.FollowUpSearchCriteria;
import com.healthcare.hms.clinical.followup.dto.request.UpdateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.request.UpdateFollowUpStatusRequest;
import com.healthcare.hms.clinical.followup.dto.response.FollowUpResponse;
import com.healthcare.hms.clinical.followup.mapper.FollowUpMapper;
import com.healthcare.hms.clinical.followup.service.FollowUpService;
import com.healthcare.hms.clinical.followup.support.FollowUpLabelEnricher;
import com.healthcare.hms.clinical.followup.support.FollowUpReminderScheduler;
import com.healthcare.hms.clinical.followup.validation.FollowUpClinicalRules;
import com.healthcare.hms.clinical.followup.validation.FollowUpStatusTransitions;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.clinical.repository.FollowUpRepository;
import com.healthcare.hms.clinical.repository.FollowUpSpecifications;
import com.healthcare.hms.clinical.support.ConsultationActorScopeSupport;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.patients.support.PatientAccessSupport;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Follow-up planning with due lists, status transitions, reminders, and timeline (Phase 7.7).
 */
@Service
public class FollowUpServiceImpl implements FollowUpService {

    private static final Logger log = LoggerFactory.getLogger(FollowUpServiceImpl.class);
    private static final String ENTITY = "FOLLOW_UP";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_DUE_WITHIN_DAYS = 14;
    private static final Set<String> ALLOWED_SORT = Set.of(
            "scheduledDate", "createdAt", "status", "priority", "nextReminderAt"
    );

    private final FollowUpRepository followUpRepository;
    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final FollowUpMapper followUpMapper;
    private final FollowUpLabelEnricher labelEnricher;
    private final FollowUpReminderScheduler reminderScheduler;
    private final ConsultationActorScopeSupport actorScopeSupport;
    private final AuditLogService auditLogService;

    public FollowUpServiceImpl(
            final FollowUpRepository followUpRepository,
            final ConsultationRepository consultationRepository,
            final AppointmentRepository appointmentRepository,
            final DoctorRepository doctorRepository,
            final PatientRepository patientRepository,
            final FollowUpMapper followUpMapper,
            final FollowUpLabelEnricher labelEnricher,
            final FollowUpReminderScheduler reminderScheduler,
            final ConsultationActorScopeSupport actorScopeSupport,
            final AuditLogService auditLogService
    ) {
        this.followUpRepository = followUpRepository;
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.followUpMapper = followUpMapper;
        this.labelEnricher = labelEnricher;
        this.reminderScheduler = reminderScheduler;
        this.actorScopeSupport = actorScopeSupport;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public FollowUpResponse create(
            final UUID consultationId,
            final CreateFollowUpRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditableConsultation(tenantId, consultationId);

        final UUID doctorId = resolveDoctorId(tenantId, consultation, request.doctorId());
        if (request.followUpAppointmentId() != null) {
            assertAppointmentLinkValid(tenantId, consultation.getPatientId(), doctorId, request.followUpAppointmentId(), null);
        }

        final FollowUp followUp = new FollowUp();
        followUp.setConsultationId(consultation.getId());
        followUp.setPatientId(consultation.getPatientId());
        followUpMapper.applyCreate(request, followUp, doctorId);
        if (request.followUpAppointmentId() != null && request.status() == null) {
            followUp.setStatus(FollowUpStatus.SCHEDULED);
        }
        reminderScheduler.refreshSchedule(followUp);

        final FollowUp saved = followUpRepository.save(followUp);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info(
                "Follow-up created id={} consultationId={} tenantId={} actorId={}",
                saved.getId(), consultationId, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public FollowUpResponse getById(
            final UUID consultationId,
            final UUID followUpId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final FollowUp followUp = requireFollowUp(tenantId, consultationId, followUpId);
        audit(followUp, AuditAction.VIEW, null, ipAddress, userAgent);
        return labelEnricher.enrichOne(tenantId, followUp, consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public FollowUpResponse getByIdGlobal(
            final UUID followUpId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final FollowUp followUp = followUpRepository.findByIdAndTenantId(followUpId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found"));
        actorScopeSupport.assertDoctorAccessible(tenantId, followUp.getDoctorId());
        final Consultation consultation = requireConsultation(tenantId, followUp.getConsultationId());
        audit(followUp, AuditAction.VIEW, null, ipAddress, userAgent);
        return labelEnricher.enrichOne(tenantId, followUp, consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public List<FollowUpResponse> listByConsultation(final UUID consultationId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final List<FollowUp> followUps = followUpRepository
                .findByTenantIdAndConsultationIdOrderByScheduledDateAsc(tenantId, consultationId);
        return labelEnricher.enrich(tenantId, followUps, Map.of(consultation.getId(), consultation));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public FollowUpResponse update(
            final UUID consultationId,
            final UUID followUpId,
            final UpdateFollowUpRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final FollowUp followUp = requireFollowUp(tenantId, consultationId, followUpId);

        final boolean planChange = request.scheduledDate() != null
                || request.scheduledTime() != null
                || request.priority() != null
                || request.reason() != null
                || request.instructions() != null
                || request.clinicalRecommendations() != null
                || request.doctorId() != null
                || request.followUpAppointmentId() != null
                || request.reminderEnabled() != null
                || request.reminderLeadDays() != null;

        if (planChange) {
            assertPlanMutable(consultation, followUp);
            PatientAccessSupport.requireActivePatient(
                    patientRepository, tenantId, consultation.getPatientId());
        }

        if (request.status() != null && request.status() != followUp.getStatus()) {
            assertStatusTransition(followUp.getStatus(), request.status());
        }

        final String old = snapshot(followUp);

        if (request.scheduledDate() != null && !FollowUpClinicalRules.isScheduledDateValid(request.scheduledDate())) {
            throw new BusinessException("INVALID_SCHEDULED_DATE", "Scheduled date must not be in the past");
        }
        if (request.doctorId() != null) {
            assertDoctorExists(tenantId, request.doctorId());
            actorScopeSupport.assertDoctorAccessible(tenantId, request.doctorId());
        }

        final UUID effectiveDoctorId = request.doctorId() != null ? request.doctorId() : followUp.getDoctorId();
        if (request.followUpAppointmentId() != null) {
            assertAppointmentLinkValid(
                    tenantId,
                    consultation.getPatientId(),
                    effectiveDoctorId,
                    request.followUpAppointmentId(),
                    followUpId
            );
        }

        followUpMapper.applyUpdate(request, followUp);
        if (request.followUpAppointmentId() != null
                && followUp.getStatus() == FollowUpStatus.PENDING
                && request.status() == null) {
            followUp.setStatus(FollowUpStatus.SCHEDULED);
        }
        reminderScheduler.refreshSchedule(followUp);

        final FollowUp saved = followUpRepository.save(followUp);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Follow-up updated id={} consultationId={} tenantId={} actorId={}",
                saved.getId(), consultationId, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public FollowUpResponse updateStatus(
            final UUID consultationId,
            final UUID followUpId,
            final UpdateFollowUpStatusRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final FollowUp followUp = requireFollowUp(tenantId, consultationId, followUpId);
        assertStatusTransition(followUp.getStatus(), request.status());

        final String old = snapshot(followUp);
        followUp.setStatus(request.status());
        reminderScheduler.refreshSchedule(followUp);
        final FollowUp saved = followUpRepository.save(followUp);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Follow-up status changed id={} status={} tenantId={} actorId={}",
                saved.getId(), saved.getStatus(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    public void delete(
            final UUID consultationId,
            final UUID followUpId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireEditableConsultation(tenantId, consultationId);
        final FollowUp followUp = requireFollowUp(tenantId, consultationId, followUpId);
        if (FollowUpStatusTransitions.isTerminal(followUp.getStatus())
                && followUp.getStatus() == FollowUpStatus.COMPLETED) {
            throw new BusinessException(
                    "FOLLOW_UP_NOT_DELETABLE",
                    "Completed follow-ups cannot be deleted"
            );
        }
        final String old = snapshot(followUp);
        final UUID actorId = SecurityUtils.requireCurrentUserId();
        followUp.markDeleted(actorId);
        followUpRepository.save(followUp);
        audit(followUp, AuditAction.DELETE, old, ipAddress, userAgent);
        log.info(
                "Follow-up soft-deleted id={} consultationId={} tenantId={} actorId={}",
                followUpId, consultationId, tenantId, actorId
        );
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public PageResponse<FollowUpResponse> patientHistory(
            final UUID patientId,
            final FollowUpStatus status,
            final LocalDate fromDate,
            final LocalDate toDate,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);
        validateDateRange(fromDate, toDate);

        final Page<FollowUp> page = followUpRepository.findAll(
                FollowUpSpecifications.forPatientHistory(tenantId, patientId, status, fromDate, toDate),
                sanitizePageable(pageable)
        );
        return toPageResponse(tenantId, page);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public PageResponse<FollowUpResponse> search(
            final FollowUpSearchCriteria criteria,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();
        validateDateRange(criteria.fromDate(), criteria.toDate());

        if (criteria.patientId() != null) {
            PatientAccessSupport.requirePatient(patientRepository, tenantId, criteria.patientId());
        }

        final UUID scopedDoctorId = actorScopeSupport.resolveScopedDoctorId(tenantId).orElse(null);
        final UUID doctorId;
        if (scopedDoctorId != null) {
            if (criteria.doctorId() != null && !criteria.doctorId().equals(scopedDoctorId)) {
                throw new BusinessException("DOCTOR_SCOPE_VIOLATION", "Cannot search other doctors' follow-ups");
            }
            doctorId = scopedDoctorId;
        } else {
            doctorId = criteria.doctorId();
        }

        final Page<FollowUp> page = followUpRepository.findAll(
                FollowUpSpecifications.search(
                        tenantId,
                        criteria.patientId(),
                        doctorId,
                        criteria.consultationId(),
                        criteria.status(),
                        criteria.priority(),
                        criteria.fromDate(),
                        criteria.toDate(),
                        Boolean.TRUE.equals(criteria.overdueOnly()),
                        Boolean.TRUE.equals(criteria.dueSoonOnly()),
                        criteria.dueWithinDays()
                ),
                sanitizePageable(pageable)
        );
        return toPageResponse(tenantId, page);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public PageResponse<FollowUpResponse> dueList(final Integer withinDays, final Pageable pageable) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();

        final UUID doctorId = actorScopeSupport.resolveScopedDoctorId(tenantId)
                .orElseThrow(() -> new BusinessException(
                        "DOCTOR_SCOPE_REQUIRED",
                        "Due list is available for doctor-scoped actors; use search with doctorId for admins"
                ));

        final int days = withinDays != null && withinDays > 0 ? Math.min(withinDays, 90) : DEFAULT_DUE_WITHIN_DAYS;
        final Page<FollowUp> page = followUpRepository.findAll(
                FollowUpSpecifications.dueForDoctor(
                        tenantId,
                        doctorId,
                        LocalDate.now(),
                        days,
                        EnumSet.of(FollowUpStatus.PENDING, FollowUpStatus.SCHEDULED)
                ),
                sanitizePageable(pageable, Sort.by(Sort.Direction.ASC, "scheduledDate", "priority"))
        );
        return toPageResponse(tenantId, page);
    }

    private PageResponse<FollowUpResponse> toPageResponse(final UUID tenantId, final Page<FollowUp> page) {
        final Map<UUID, Consultation> consultations = loadConsultations(tenantId, page.getContent());
        final List<FollowUpResponse> content = labelEnricher.enrich(tenantId, page.getContent(), consultations);
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

    private Consultation requireConsultation(final UUID tenantId, final UUID consultationId) {
        final Consultation consultation = consultationRepository.findByIdAndTenantId(consultationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found"));
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        return consultation;
    }

    private Consultation requireEditableConsultation(final UUID tenantId, final UUID consultationId) {
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        if (!consultation.isEditable()) {
            throw new BusinessException(
                    "CONSULTATION_NOT_EDITABLE",
                    "Follow-ups can only be planned while consultation is editable (status="
                            + consultation.getStatus() + ")"
            );
        }
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, consultation.getPatientId());
        return consultation;
    }

    private void assertPlanMutable(final Consultation consultation, final FollowUp followUp) {
        if (consultation.isEditable()) {
            return;
        }
        if (followUp.isOpen()) {
            return;
        }
        throw new BusinessException(
                "FOLLOW_UP_NOT_EDITABLE",
                "Plan fields can only be changed while the consultation is editable or the follow-up is still open"
        );
    }

    private static void assertStatusTransition(final FollowUpStatus from, final FollowUpStatus to) {
        if (!FollowUpStatusTransitions.canTransition(from, to)) {
            throw new BusinessException(
                    "INVALID_FOLLOW_UP_TRANSITION",
                    "Cannot transition follow-up from " + from + " to " + to
            );
        }
    }

    private FollowUp requireFollowUp(
            final UUID tenantId,
            final UUID consultationId,
            final UUID followUpId
    ) {
        return followUpRepository.findByIdAndTenantIdAndConsultationId(followUpId, tenantId, consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found"));
    }

    private UUID resolveDoctorId(
            final UUID tenantId,
            final Consultation consultation,
            final UUID requestedDoctorId
    ) {
        if (requestedDoctorId != null) {
            assertDoctorExists(tenantId, requestedDoctorId);
            actorScopeSupport.assertDoctorAccessible(tenantId, requestedDoctorId);
            return requestedDoctorId;
        }
        return consultation.getDoctorId();
    }

    private void assertDoctorExists(final UUID tenantId, final UUID doctorId) {
        doctorRepository.findByTenantIdAndIdIn(tenantId, List.of(doctorId)).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private void assertAppointmentLinkValid(
            final UUID tenantId,
            final UUID patientId,
            final UUID doctorId,
            final UUID appointmentId,
            final UUID excludeFollowUpId
    ) {
        final Appointment appointment = appointmentRepository.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!patientId.equals(appointment.getPatientId())) {
            throw new BusinessException(
                    "APPOINTMENT_PATIENT_MISMATCH",
                    "Follow-up appointment must belong to the same patient"
            );
        }
        if (!doctorId.equals(appointment.getDoctorId())) {
            throw new BusinessException(
                    "APPOINTMENT_DOCTOR_MISMATCH",
                    "Follow-up appointment must belong to the same doctor"
            );
        }

        followUpRepository.findByTenantIdAndFollowUpAppointmentId(tenantId, appointmentId)
                .ifPresent(existing -> {
                    if (excludeFollowUpId == null || !existing.getId().equals(excludeFollowUpId)) {
                        throw new BusinessException(
                                "APPOINTMENT_ALREADY_LINKED",
                                "Appointment is already linked to another follow-up"
                        );
                    }
                });
    }

    private Map<UUID, Consultation> loadConsultations(final UUID tenantId, final List<FollowUp> followUps) {
        final Set<UUID> consultationIds = followUps.stream()
                .map(FollowUp::getConsultationId)
                .collect(Collectors.toSet());
        if (consultationIds.isEmpty()) {
            return Map.of();
        }
        return consultationRepository.findAllById(consultationIds).stream()
                .filter(c -> tenantId.equals(c.getTenantId()))
                .collect(Collectors.toMap(Consultation::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private static void validateDateRange(final LocalDate fromDate, final LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException("INVALID_DATE_RANGE", "fromDate must not be after toDate");
        }
    }

    private static Pageable sanitizePageable(final Pageable pageable) {
        return sanitizePageable(pageable, Sort.by(Sort.Direction.DESC, "scheduledDate"));
    }

    private static Pageable sanitizePageable(final Pageable pageable, final Sort defaultSort) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, defaultSort);
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
            final FollowUp followUp,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                followUp.getTenantId(),
                SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY,
                followUp.getId().toString(),
                action,
                oldSnapshot,
                snapshot(followUp),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final FollowUp followUp) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", followUp.getId());
        fields.put("consultationId", followUp.getConsultationId());
        fields.put("patientId", followUp.getPatientId());
        fields.put("doctorId", followUp.getDoctorId());
        fields.put("scheduledDate", followUp.getScheduledDate());
        fields.put("scheduledTime", followUp.getScheduledTime());
        fields.put("status", followUp.getStatus());
        fields.put("priority", followUp.getPriority());
        fields.put("reason", followUp.getReason() == null ? null : "[redacted]");
        fields.put("instructions", followUp.getInstructions() == null ? null : "[redacted]");
        fields.put("clinicalRecommendations", followUp.getClinicalRecommendations() == null ? null : "[redacted]");
        fields.put("followUpAppointmentId", followUp.getFollowUpAppointmentId());
        fields.put("reminderEnabled", followUp.getReminderEnabled());
        fields.put("reminderLeadDays", followUp.getReminderLeadDays());
        fields.put("nextReminderAt", followUp.getNextReminderAt());
        fields.put("reminderStatus", followUp.getReminderStatus());
        fields.put("deleted", followUp.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
