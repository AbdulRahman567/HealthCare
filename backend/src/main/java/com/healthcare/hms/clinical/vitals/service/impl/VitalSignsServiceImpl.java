package com.healthcare.hms.clinical.vitals.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.entity.VitalSigns;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.clinical.repository.VitalSignsRepository;
import com.healthcare.hms.clinical.repository.VitalSignsSpecifications;
import com.healthcare.hms.clinical.support.ConsultationActorScopeSupport;
import com.healthcare.hms.clinical.vitals.dto.request.RecordVitalSignsRequest;
import com.healthcare.hms.clinical.vitals.dto.request.UpdateVitalSignsRequest;
import com.healthcare.hms.clinical.vitals.dto.response.VitalSignsResponse;
import com.healthcare.hms.clinical.vitals.mapper.VitalSignsMapper;
import com.healthcare.hms.clinical.vitals.service.VitalSignsService;
import com.healthcare.hms.clinical.vitals.support.VitalSignsLabelEnricher;
import com.healthcare.hms.clinical.vitals.validation.VitalSignsClinicalRules;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
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
 * Append-only vital-signs time series with consultation linkage (Phase 7.3).
 *
 * <p>Each recording creates a new row — history is preserved for trend analysis across
 * encounters and doctors (healthcare-domain requirement).
 */
@Service
public class VitalSignsServiceImpl implements VitalSignsService {

    private static final Logger log = LoggerFactory.getLogger(VitalSignsServiceImpl.class);
    private static final String ENTITY = "VITAL_SIGNS";
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT = Set.of("recordedAt", "createdAt");

    private final VitalSignsRepository vitalSignsRepository;
    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final VitalSignsMapper vitalSignsMapper;
    private final VitalSignsLabelEnricher labelEnricher;
    private final ConsultationActorScopeSupport actorScopeSupport;
    private final AuditLogService auditLogService;

    public VitalSignsServiceImpl(
            final VitalSignsRepository vitalSignsRepository,
            final ConsultationRepository consultationRepository,
            final PatientRepository patientRepository,
            final VitalSignsMapper vitalSignsMapper,
            final VitalSignsLabelEnricher labelEnricher,
            final ConsultationActorScopeSupport actorScopeSupport,
            final AuditLogService auditLogService
    ) {
        this.vitalSignsRepository = vitalSignsRepository;
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.vitalSignsMapper = vitalSignsMapper;
        this.labelEnricher = labelEnricher;
        this.actorScopeSupport = actorScopeSupport;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public VitalSignsResponse record(
            final UUID consultationId,
            final RecordVitalSignsRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditableConsultation(tenantId, consultationId);

        final Instant recordedAt = request.recordedAt() != null ? request.recordedAt() : Instant.now();
        final VitalSigns vitalSigns = new VitalSigns();
        vitalSigns.setConsultationId(consultation.getId());
        vitalSigns.setPatientId(consultation.getPatientId());
        vitalSigns.setRecordedByUserId(SecurityUtils.requireCurrentUserId());
        vitalSignsMapper.applyRecord(request, vitalSigns, recordedAt);

        final VitalSigns saved = vitalSignsRepository.save(vitalSigns);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info(
                "Vital signs recorded id={} consultationId={} tenantId={} actorId={}",
                saved.getId(), consultationId, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public VitalSignsResponse getById(
            final UUID consultationId,
            final UUID vitalSignsId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final VitalSigns vitalSigns = requireVitalSigns(tenantId, consultationId, vitalSignsId);
        audit(vitalSigns, AuditAction.VIEW, null, ipAddress, userAgent);
        return labelEnricher.enrichOne(tenantId, vitalSigns, consultation);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public List<VitalSignsResponse> listByConsultation(final UUID consultationId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final List<VitalSigns> readings = vitalSignsRepository
                .findByTenantIdAndConsultationIdOrderByRecordedAtAsc(tenantId, consultationId);
        return labelEnricher.enrich(tenantId, readings, Map.of(consultation.getId(), consultation));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public VitalSignsResponse update(
            final UUID consultationId,
            final UUID vitalSignsId,
            final UpdateVitalSignsRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditableConsultation(tenantId, consultationId);
        final VitalSigns vitalSigns = requireVitalSigns(tenantId, consultationId, vitalSignsId);
        final String old = snapshot(vitalSigns);

        vitalSignsMapper.applyUpdate(request, vitalSigns);
        assertValidMergedMeasurements(vitalSigns);
        final VitalSigns saved = vitalSignsRepository.save(vitalSigns);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Vital signs corrected id={} consultationId={} tenantId={} actorId={}",
                saved.getId(), consultationId, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    public void delete(
            final UUID consultationId,
            final UUID vitalSignsId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireEditableConsultation(tenantId, consultationId);
        final VitalSigns vitalSigns = requireVitalSigns(tenantId, consultationId, vitalSignsId);
        final String old = snapshot(vitalSigns);
        final UUID actorId = SecurityUtils.requireCurrentUserId();
        vitalSigns.markDeleted(actorId);
        vitalSignsRepository.save(vitalSigns);
        audit(vitalSigns, AuditAction.DELETE, old, ipAddress, userAgent);
        log.info(
                "Vital signs soft-deleted id={} consultationId={} tenantId={} actorId={}",
                vitalSignsId, consultationId, tenantId, actorId
        );
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public PageResponse<VitalSignsResponse> patientHistory(
            final UUID patientId,
            final LocalDate fromDate,
            final LocalDate toDate,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);
        validateDateRange(fromDate, toDate);

        final Page<VitalSigns> page = vitalSignsRepository.findAll(
                VitalSignsSpecifications.forPatientHistory(tenantId, patientId, fromDate, toDate),
                sanitizePageable(pageable)
        );

        final Map<UUID, Consultation> consultations = loadConsultations(tenantId, page.getContent());
        final List<VitalSignsResponse> content = labelEnricher.enrich(tenantId, page.getContent(), consultations);

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
                    "Vital signs can only be recorded while consultation is editable (status="
                            + consultation.getStatus() + ")"
            );
        }
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, consultation.getPatientId());
        return consultation;
    }

    private VitalSigns requireVitalSigns(
            final UUID tenantId,
            final UUID consultationId,
            final UUID vitalSignsId
    ) {
        return vitalSignsRepository.findByIdAndTenantIdAndConsultationId(vitalSignsId, tenantId, consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vital signs record not found"));
    }

    private Map<UUID, Consultation> loadConsultations(final UUID tenantId, final List<VitalSigns> readings) {
        final Set<UUID> consultationIds = readings.stream()
                .map(VitalSigns::getConsultationId)
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

    private static void assertValidMergedMeasurements(final VitalSigns vitalSigns) {
        if (!VitalSignsClinicalRules.isBloodPressureComplete(vitalSigns.getSystolicBp(), vitalSigns.getDiastolicBp())) {
            throw new BusinessException(
                    "INCOMPLETE_BLOOD_PRESSURE",
                    "Blood pressure requires both systolic and diastolic values"
            );
        }
        if (!VitalSignsClinicalRules.isBloodPressurePairValid(vitalSigns.getSystolicBp(), vitalSigns.getDiastolicBp())) {
            throw new BusinessException(
                    "INVALID_BLOOD_PRESSURE",
                    "Systolic blood pressure must be greater than diastolic"
            );
        }
        if (!VitalSignsClinicalRules.hasAnyMeasurement(
                vitalSigns.getTemperatureCelsius(),
                vitalSigns.getPulseBpm(),
                vitalSigns.getSystolicBp(),
                vitalSigns.getDiastolicBp(),
                vitalSigns.getRespiratoryRate(),
                vitalSigns.getOxygenSaturationPercent(),
                vitalSigns.getHeightCm(),
                vitalSigns.getWeightKg(),
                vitalSigns.getPainScale()
        )) {
            throw new BusinessException(
                    "VITAL_SIGNS_EMPTY",
                    "At least one vital sign measurement must remain after correction"
            );
        }
    }

    private static Pageable sanitizePageable(final Pageable pageable) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedAt"));
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
            final VitalSigns vitalSigns,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                vitalSigns.getTenantId(),
                SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY,
                vitalSigns.getId().toString(),
                action,
                oldSnapshot,
                snapshot(vitalSigns),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final VitalSigns vitalSigns) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", vitalSigns.getId());
        fields.put("consultationId", vitalSigns.getConsultationId());
        fields.put("patientId", vitalSigns.getPatientId());
        fields.put("recordedAt", vitalSigns.getRecordedAt());
        fields.put("temperatureCelsius", vitalSigns.getTemperatureCelsius());
        fields.put("heartRateBpm", vitalSigns.getPulseBpm());
        fields.put("systolicBp", vitalSigns.getSystolicBp());
        fields.put("diastolicBp", vitalSigns.getDiastolicBp());
        fields.put("respiratoryRate", vitalSigns.getRespiratoryRate());
        fields.put("oxygenSaturationPercent", vitalSigns.getOxygenSaturationPercent());
        fields.put("heightCm", vitalSigns.getHeightCm());
        fields.put("weightKg", vitalSigns.getWeightKg());
        fields.put("bmi", vitalSigns.getBmi());
        fields.put("painScale", vitalSigns.getPainScale());
        fields.put("deleted", vitalSigns.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
