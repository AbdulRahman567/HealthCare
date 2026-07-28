package com.healthcare.hms.clinical.service.impl;

import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import com.healthcare.hms.appointments.queue.repository.QueueEntryRepository;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.clinical.dto.request.CompleteConsultationRequest;
import com.healthcare.hms.clinical.dto.request.ConsultationSearchCriteria;
import com.healthcare.hms.clinical.dto.request.CreateConsultationRequest;
import com.healthcare.hms.clinical.dto.request.UpdateConsultationDocumentationRequest;
import com.healthcare.hms.clinical.dto.response.ClinicalSummaryResponse;
import com.healthcare.hms.clinical.dto.response.ConsultationResponse;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.enums.ConsultationStatus;
import com.healthcare.hms.clinical.mapper.ConsultationMapper;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.clinical.repository.ConsultationSpecifications;
import com.healthcare.hms.clinical.service.ConsultationService;
import com.healthcare.hms.clinical.support.ConsultationAccessSupport;
import com.healthcare.hms.clinical.support.ConsultationActorScopeSupport;
import com.healthcare.hms.clinical.support.ConsultationLabelEnricher;
import com.healthcare.hms.clinical.support.ConsultationNumberGenerator;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
 * Consultation lifecycle and clinical documentation orchestration (Phase 7.2).
 */
@Service
public class ConsultationServiceImpl implements ConsultationService {

    private static final Logger log = LoggerFactory.getLogger(ConsultationServiceImpl.class);
    private static final String ENTITY = "CONSULTATION";
    private static final int MAX_NUMBER_ATTEMPTS = 5;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_SEARCH_TEXT_LENGTH = 100;
    private static final Set<ConsultationStatus> ACTIVE_DOCTOR_STATUSES = Set.of(
            ConsultationStatus.IN_PROGRESS,
            ConsultationStatus.PAUSED
    );
    private static final Set<String> ALLOWED_SORT = Set.of(
            "consultationDate",
            "consultationNumber",
            "status",
            "startedAt",
            "completedAt",
            "createdAt",
            "updatedAt",
            "patientId",
            "doctorId",
            "departmentId"
    );

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final ConsultationMapper consultationMapper;
    private final ConsultationAccessSupport accessSupport;
    private final ConsultationActorScopeSupport actorScopeSupport;
    private final ConsultationNumberGenerator numberGenerator;
    private final ConsultationLabelEnricher labelEnricher;
    private final AuditLogService auditLogService;

    public ConsultationServiceImpl(
            final ConsultationRepository consultationRepository,
            final AppointmentRepository appointmentRepository,
            final QueueEntryRepository queueEntryRepository,
            final ConsultationMapper consultationMapper,
            final ConsultationAccessSupport accessSupport,
            final ConsultationActorScopeSupport actorScopeSupport,
            final ConsultationNumberGenerator numberGenerator,
            final ConsultationLabelEnricher labelEnricher,
            final AuditLogService auditLogService
    ) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.queueEntryRepository = queueEntryRepository;
        this.consultationMapper = consultationMapper;
        this.accessSupport = accessSupport;
        this.actorScopeSupport = actorScopeSupport;
        this.numberGenerator = numberGenerator;
        this.labelEnricher = labelEnricher;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_CREATE)
    public ConsultationResponse create(
            final CreateConsultationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.assertDoctorAccessible(tenantId, request.doctorId());
        accessSupport.requireActivePatient(tenantId, request.patientId());
        final Doctor doctor = accessSupport.requireConsultingDoctor(tenantId, request.doctorId());
        accessSupport.requireActiveDepartment(tenantId, request.departmentId(), doctor);
        accessSupport.requireHospital(tenantId, doctor.getHospitalId());

        if (request.appointmentId() != null) {
            accessSupport.requireConsultableAppointment(
                    tenantId,
                    request.appointmentId(),
                    request.patientId(),
                    request.doctorId()
            );
        }

        final Consultation consultation = new Consultation();
        consultation.setHospitalId(doctor.getHospitalId());
        consultation.setConsultationNumber(allocateNumber(tenantId));
        consultation.setConsultationDate(LocalDate.now());
        consultation.setStatus(ConsultationStatus.DRAFT);
        consultationMapper.applyCreate(request, consultation);

        Consultation saved = consultationRepository.save(consultation);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);

        if (request.startImmediately()) {
            saved = startInternal(tenantId, saved, ipAddress, userAgent);
        }

        log.info(
                "Consultation created id={} doctorId={} tenantId={} actorId={}",
                saved.getId(), saved.getDoctorId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public ConsultationResponse getById(
            final UUID consultationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = require(tenantId, consultationId);
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        audit(consultation, AuditAction.VIEW, null, ipAddress, userAgent);
        return labelEnricher.enrichOne(tenantId, consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public ClinicalSummaryResponse getClinicalSummary(
            final UUID consultationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = require(tenantId, consultationId);
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        audit(consultation, AuditAction.VIEW, "clinicalSummary", ipAddress, userAgent);
        return consultationMapper.toClinicalSummary(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public PageResponse<ConsultationResponse> search(
            final ConsultationSearchCriteria criteria,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();
        validateSearchCriteria(criteria);
        final ConsultationSearchCriteria scoped = actorScopeSupport.constrainSearch(tenantId, criteria);
        final Pageable safePageable = sanitizePageable(pageable);

        final Page<Consultation> page = consultationRepository.findAll(
                ConsultationSpecifications.withFilters(tenantId, scoped),
                safePageable
        );
        final List<ConsultationResponse> content = labelEnricher.enrich(tenantId, page.getContent());
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

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public ConsultationResponse updateDocumentation(
            final UUID consultationId,
            final UpdateConsultationDocumentationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditable(tenantId, consultationId);
        final String old = snapshot(consultation);
        consultationMapper.applyDocumentation(request, consultation);
        final Consultation saved = consultationRepository.save(consultation);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Consultation documentation updated id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public ConsultationResponse start(
            final UUID consultationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = require(tenantId, consultationId);
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        final Consultation saved = startInternal(tenantId, consultation, ipAddress, userAgent);
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public ConsultationResponse pause(
            final UUID consultationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditable(tenantId, consultationId);
        final String old = snapshot(consultation);
        try {
            consultation.pause();
        } catch (final IllegalStateException ex) {
            throw new BusinessException("CONSULTATION_INVALID_TRANSITION", ex.getMessage());
        }
        final Consultation saved = consultationRepository.save(consultation);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Consultation paused id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public ConsultationResponse resume(
            final UUID consultationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = require(tenantId, consultationId);
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        assertNoOtherActiveConsultation(tenantId, consultation.getDoctorId(), consultation.getId());
        final String old = snapshot(consultation);
        try {
            consultation.resume();
        } catch (final IllegalStateException ex) {
            throw new BusinessException("CONSULTATION_INVALID_TRANSITION", ex.getMessage());
        }
        final Consultation saved = consultationRepository.save(consultation);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Consultation resumed id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public ConsultationResponse complete(
            final UUID consultationId,
            final CompleteConsultationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = require(tenantId, consultationId);
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        if (!consultation.isEditable()) {
            throw new BusinessException(
                    "CONSULTATION_NOT_EDITABLE",
                    "Only draft, in-progress, or paused consultations can be completed"
            );
        }
        final String old = snapshot(consultation);
        consultationMapper.applyComplete(request, consultation);
        try {
            consultation.complete();
        } catch (final IllegalStateException ex) {
            throw new BusinessException("CONSULTATION_INVALID_TRANSITION", ex.getMessage());
        }
        final Consultation saved = consultationRepository.save(consultation);
        completeLinkedAppointment(tenantId, saved);
        completeLinkedQueueEntry(tenantId, saved);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Consultation completed id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public ConsultationResponse cancel(
            final UUID consultationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = require(tenantId, consultationId);
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        if (!consultation.isEditable()) {
            throw new BusinessException(
                    "CONSULTATION_NOT_EDITABLE",
                    "Only draft, in-progress, or paused consultations can be cancelled"
            );
        }
        final String old = snapshot(consultation);
        try {
            consultation.cancel();
        } catch (final IllegalStateException ex) {
            throw new BusinessException("CONSULTATION_INVALID_TRANSITION", ex.getMessage());
        }
        final Consultation saved = consultationRepository.save(consultation);
        cancelLinkedQueueEntry(tenantId, saved);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Consultation cancelled id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved);
    }

    private Consultation startInternal(
            final UUID tenantId,
            final Consultation consultation,
            final String ipAddress,
            final String userAgent
    ) {
        if (consultation.getStatus() == ConsultationStatus.IN_PROGRESS) {
            return consultation;
        }
        if (!consultation.isEditable() || consultation.getStatus() != ConsultationStatus.DRAFT) {
            throw new BusinessException(
                    "CONSULTATION_INVALID_TRANSITION",
                    "Only DRAFT consultations can be started (status=" + consultation.getStatus() + ")"
            );
        }
        assertNoOtherActiveConsultation(tenantId, consultation.getDoctorId(), consultation.getId());
        final String old = snapshot(consultation);
        try {
            consultation.start();
        } catch (final IllegalStateException ex) {
            throw new BusinessException("CONSULTATION_INVALID_TRANSITION", ex.getMessage());
        }
        final Consultation saved = consultationRepository.save(consultation);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Consultation started id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return saved;
    }

    private void completeLinkedAppointment(final UUID tenantId, final Consultation consultation) {
        if (consultation.getAppointmentId() == null) {
            return;
        }
        appointmentRepository.findByIdAndTenantId(consultation.getAppointmentId(), tenantId)
                .ifPresent(appointment -> {
                    if (appointment.isBookableSlot()) {
                        appointment.complete();
                        appointmentRepository.save(appointment);
                    }
                });
    }

    private void completeLinkedQueueEntry(final UUID tenantId, final Consultation consultation) {
        if (consultation.getAppointmentId() == null) {
            return;
        }
        queueEntryRepository.findByTenantIdAndAppointmentId(tenantId, consultation.getAppointmentId())
                .ifPresent(entry -> {
                    if (entry.getStatus() == QueueEntryStatus.IN_CONSULTATION) {
                        entry.complete();
                        queueEntryRepository.save(entry);
                    }
                });
    }

    private void cancelLinkedQueueEntry(final UUID tenantId, final Consultation consultation) {
        if (consultation.getAppointmentId() == null) {
            return;
        }
        queueEntryRepository.findByTenantIdAndAppointmentId(tenantId, consultation.getAppointmentId())
                .ifPresent(entry -> {
                    if (entry.getStatus() == QueueEntryStatus.IN_CONSULTATION) {
                        entry.cancel();
                        queueEntryRepository.save(entry);
                    }
                });
    }

    private void assertNoOtherActiveConsultation(
            final UUID tenantId,
            final UUID doctorId,
            final UUID excludeId
    ) {
        final boolean conflict = consultationRepository.existsByTenantIdAndDoctorIdAndStatusInAndIdNot(
                tenantId,
                doctorId,
                ACTIVE_DOCTOR_STATUSES,
                excludeId
        );
        if (conflict) {
            throw new ConflictException(
                    "CONSULTATION_IN_PROGRESS",
                    "Doctor already has an active consultation (IN_PROGRESS or PAUSED)"
            );
        }
    }

    private Consultation require(final UUID tenantId, final UUID consultationId) {
        return consultationRepository.findByIdAndTenantId(consultationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found"));
    }

    private Consultation requireEditable(final UUID tenantId, final UUID consultationId) {
        final Consultation consultation = require(tenantId, consultationId);
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        if (!consultation.isEditable()) {
            throw new BusinessException(
                    "CONSULTATION_NOT_EDITABLE",
                    "Consultation is not editable in status " + consultation.getStatus()
            );
        }
        accessSupport.requireActivePatient(tenantId, consultation.getPatientId());
        return consultation;
    }

    private String allocateNumber(final UUID tenantId) {
        for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {
            final String candidate = numberGenerator.next();
            if (!consultationRepository.existsByTenantIdAndConsultationNumberIgnoreCase(tenantId, candidate)) {
                return candidate;
            }
        }
        throw new ConflictException(
                "CONSULTATION_NUMBER_EXHAUSTED",
                "Unable to allocate a unique consultation number"
        );
    }

    private void validateSearchCriteria(final ConsultationSearchCriteria criteria) {
        if (criteria.consultationNumber() != null && criteria.consultationNumber().length() > MAX_SEARCH_TEXT_LENGTH) {
            throw new BusinessException("INVALID_SEARCH", "Consultation number filter is too long");
        }
        if (criteria.patientName() != null && criteria.patientName().length() > MAX_SEARCH_TEXT_LENGTH) {
            throw new BusinessException("INVALID_SEARCH", "Patient name filter is too long");
        }
        if (criteria.doctorName() != null && criteria.doctorName().length() > MAX_SEARCH_TEXT_LENGTH) {
            throw new BusinessException("INVALID_SEARCH", "Doctor name filter is too long");
        }
        if (criteria.fromDate() != null && criteria.toDate() != null && criteria.fromDate().isAfter(criteria.toDate())) {
            throw new BusinessException("INVALID_DATE_RANGE", "fromDate must not be after toDate");
        }
    }

    private static Pageable sanitizePageable(final Pageable pageable) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "consultationDate").and(Sort.by(Sort.Direction.DESC, "startedAt"))
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
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", consultation.getId());
        fields.put("consultationNumber", consultation.getConsultationNumber());
        fields.put("patientId", consultation.getPatientId());
        fields.put("doctorId", consultation.getDoctorId());
        fields.put("departmentId", consultation.getDepartmentId());
        fields.put("appointmentId", consultation.getAppointmentId());
        fields.put("consultationDate", consultation.getConsultationDate());
        fields.put("status", consultation.getStatus());
        fields.put("startedAt", consultation.getStartedAt());
        fields.put("pausedAt", consultation.getPausedAt());
        fields.put("completedAt", consultation.getCompletedAt());
        fields.put("chiefComplaint", consultation.getChiefComplaint() == null ? null : "[redacted]");
        fields.put("deleted", consultation.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
