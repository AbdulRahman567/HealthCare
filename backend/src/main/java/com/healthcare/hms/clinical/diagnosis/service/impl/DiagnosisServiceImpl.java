package com.healthcare.hms.clinical.diagnosis.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.clinical.diagnosis.dto.request.CreateDiagnosisRequest;
import com.healthcare.hms.clinical.diagnosis.dto.request.UpdateDiagnosisRequest;
import com.healthcare.hms.clinical.diagnosis.dto.response.DiagnosisResponse;
import com.healthcare.hms.clinical.diagnosis.mapper.DiagnosisMapper;
import com.healthcare.hms.clinical.diagnosis.service.DiagnosisService;
import com.healthcare.hms.clinical.diagnosis.support.DiagnosisLabelEnricher;
import com.healthcare.hms.clinical.diagnosis.validation.DiagnosisClinicalRules;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.entity.Diagnosis;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.clinical.repository.DiagnosisRepository;
import com.healthcare.hms.clinical.repository.DiagnosisSpecifications;
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
import java.time.Instant;
import java.time.LocalDate;
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
 * Structured encounter diagnosis management (Phase 7.4).
 */
@Service
public class DiagnosisServiceImpl implements DiagnosisService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisServiceImpl.class);
    private static final String ENTITY = "DIAGNOSIS";
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT = Set.of("diagnosedAt", "createdAt", "sequenceNumber");

    private final DiagnosisRepository diagnosisRepository;
    private final ConsultationRepository consultationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DiagnosisMapper diagnosisMapper;
    private final DiagnosisLabelEnricher labelEnricher;
    private final ConsultationActorScopeSupport actorScopeSupport;
    private final AuditLogService auditLogService;

    public DiagnosisServiceImpl(
            final DiagnosisRepository diagnosisRepository,
            final ConsultationRepository consultationRepository,
            final DoctorRepository doctorRepository,
            final PatientRepository patientRepository,
            final DiagnosisMapper diagnosisMapper,
            final DiagnosisLabelEnricher labelEnricher,
            final ConsultationActorScopeSupport actorScopeSupport,
            final AuditLogService auditLogService
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.consultationRepository = consultationRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.diagnosisMapper = diagnosisMapper;
        this.labelEnricher = labelEnricher;
        this.actorScopeSupport = actorScopeSupport;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public DiagnosisResponse create(
            final UUID consultationId,
            final CreateDiagnosisRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditableConsultation(tenantId, consultationId);

        assertPrimaryDiagnosisUnique(tenantId, consultationId, request.diagnosisType(), null);

        final UUID diagnosingDoctorId = resolveDiagnosingDoctorId(tenantId, consultation, request.diagnosingDoctorId());
        final int sequenceNumber = request.sequenceNumber() != null
                ? request.sequenceNumber()
                : nextSequenceNumber(tenantId, consultationId);
        final Instant diagnosedAt = request.diagnosedAt() != null ? request.diagnosedAt() : Instant.now();

        final Diagnosis diagnosis = new Diagnosis();
        diagnosis.setConsultationId(consultation.getId());
        diagnosis.setPatientId(consultation.getPatientId());
        diagnosisMapper.applyCreate(request, diagnosis, diagnosingDoctorId, sequenceNumber, diagnosedAt);

        final Diagnosis saved = diagnosisRepository.save(diagnosis);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info(
                "Diagnosis created id={} consultationId={} tenantId={} actorId={}",
                saved.getId(), consultationId, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public DiagnosisResponse getById(
            final UUID consultationId,
            final UUID diagnosisId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final Diagnosis diagnosis = requireDiagnosis(tenantId, consultationId, diagnosisId);
        audit(diagnosis, AuditAction.VIEW, null, ipAddress, userAgent);
        return labelEnricher.enrichOne(tenantId, diagnosis, consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public List<DiagnosisResponse> listByConsultation(final UUID consultationId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final List<Diagnosis> diagnoses = diagnosisRepository
                .findByTenantIdAndConsultationIdOrderBySequenceNumberAsc(tenantId, consultationId);
        return labelEnricher.enrich(tenantId, diagnoses, Map.of(consultation.getId(), consultation));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public DiagnosisResponse update(
            final UUID consultationId,
            final UUID diagnosisId,
            final UpdateDiagnosisRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditableConsultation(tenantId, consultationId);
        final Diagnosis diagnosis = requireDiagnosis(tenantId, consultationId, diagnosisId);
        final String old = snapshot(diagnosis);

        if (request.diagnosisType() != null) {
            assertPrimaryDiagnosisUnique(tenantId, consultationId, request.diagnosisType(), diagnosisId);
        }
        if (request.diagnosingDoctorId() != null) {
            assertDoctorExists(tenantId, request.diagnosingDoctorId());
            actorScopeSupport.assertDoctorAccessible(tenantId, request.diagnosingDoctorId());
        }

        diagnosisMapper.applyUpdate(request, diagnosis);
        final Diagnosis saved = diagnosisRepository.save(diagnosis);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Diagnosis updated id={} consultationId={} tenantId={} actorId={}",
                saved.getId(), consultationId, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    public void delete(
            final UUID consultationId,
            final UUID diagnosisId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireEditableConsultation(tenantId, consultationId);
        final Diagnosis diagnosis = requireDiagnosis(tenantId, consultationId, diagnosisId);
        final String old = snapshot(diagnosis);
        final UUID actorId = SecurityUtils.requireCurrentUserId();
        diagnosis.markDeleted(actorId);
        diagnosisRepository.save(diagnosis);
        audit(diagnosis, AuditAction.DELETE, old, ipAddress, userAgent);
        log.info(
                "Diagnosis soft-deleted id={} consultationId={} tenantId={} actorId={}",
                diagnosisId, consultationId, tenantId, actorId
        );
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public PageResponse<DiagnosisResponse> patientHistory(
            final UUID patientId,
            final DiagnosisType diagnosisType,
            final DiagnosisStatus diagnosisStatus,
            final LocalDate fromDate,
            final LocalDate toDate,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);
        validateDateRange(fromDate, toDate);

        final Page<Diagnosis> page = diagnosisRepository.findAll(
                DiagnosisSpecifications.forPatientHistory(
                        tenantId, patientId, diagnosisType, diagnosisStatus, fromDate, toDate),
                sanitizePageable(pageable)
        );

        final Map<UUID, Consultation> consultations = loadConsultations(tenantId, page.getContent());
        final List<DiagnosisResponse> content = labelEnricher.enrich(tenantId, page.getContent(), consultations);

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
                    "Diagnoses can only be modified while consultation is editable (status="
                            + consultation.getStatus() + ")"
            );
        }
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, consultation.getPatientId());
        return consultation;
    }

    private Diagnosis requireDiagnosis(
            final UUID tenantId,
            final UUID consultationId,
            final UUID diagnosisId
    ) {
        return diagnosisRepository.findByIdAndTenantIdAndConsultationId(diagnosisId, tenantId, consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis not found"));
    }

    private UUID resolveDiagnosingDoctorId(
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

    private void assertPrimaryDiagnosisUnique(
            final UUID tenantId,
            final UUID consultationId,
            final DiagnosisType type,
            final UUID excludeId
    ) {
        if (!DiagnosisClinicalRules.isPrimaryType(type)) {
            return;
        }
        final boolean exists = excludeId == null
                ? diagnosisRepository.existsByTenantIdAndConsultationIdAndDiagnosisType(
                        tenantId, consultationId, DiagnosisType.PRIMARY)
                : diagnosisRepository.existsByTenantIdAndConsultationIdAndDiagnosisTypeAndIdNot(
                        tenantId, consultationId, DiagnosisType.PRIMARY, excludeId);
        if (exists) {
            throw new BusinessException(
                    "PRIMARY_DIAGNOSIS_EXISTS",
                    "Only one primary diagnosis is allowed per consultation"
            );
        }
    }

    private int nextSequenceNumber(final UUID tenantId, final UUID consultationId) {
        return (int) diagnosisRepository.countByTenantIdAndConsultationId(tenantId, consultationId) + 1;
    }

    private Map<UUID, Consultation> loadConsultations(final UUID tenantId, final List<Diagnosis> diagnoses) {
        final Set<UUID> consultationIds = diagnoses.stream()
                .map(Diagnosis::getConsultationId)
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
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "diagnosedAt"));
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
            final Diagnosis diagnosis,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                diagnosis.getTenantId(),
                SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY,
                diagnosis.getId().toString(),
                action,
                oldSnapshot,
                snapshot(diagnosis),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final Diagnosis diagnosis) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", diagnosis.getId());
        fields.put("consultationId", diagnosis.getConsultationId());
        fields.put("patientId", diagnosis.getPatientId());
        fields.put("diagnosingDoctorId", diagnosis.getDiagnosingDoctorId());
        fields.put("diagnosisName", diagnosis.getDiagnosisName() == null ? null : "[redacted]");
        fields.put("icdCode", diagnosis.getIcdCode() == null ? null : "[redacted]");
        fields.put("diagnosisType", diagnosis.getDiagnosisType());
        fields.put("diagnosisStatus", diagnosis.getDiagnosisStatus());
        fields.put("severity", diagnosis.getSeverity());
        fields.put("diagnosedAt", diagnosis.getDiagnosedAt());
        fields.put("sequenceNumber", diagnosis.getSequenceNumber());
        fields.put("clinicalNotes", diagnosis.getClinicalNotes() == null ? null : "[redacted]");
        fields.put("deleted", diagnosis.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
