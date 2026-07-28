package com.healthcare.hms.patients.history.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.history.dto.request.UpsertChronicConditionRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertFamilyHistoryRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertPastDiseaseRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertSurgeryHistoryRequest;
import com.healthcare.hms.patients.history.dto.response.ChronicConditionResponse;
import com.healthcare.hms.patients.history.dto.response.FamilyHistoryResponse;
import com.healthcare.hms.patients.history.dto.response.MedicalHistoryResponse;
import com.healthcare.hms.patients.history.dto.response.PastDiseaseResponse;
import com.healthcare.hms.patients.history.dto.response.SurgeryHistoryResponse;
import com.healthcare.hms.patients.history.entity.ChronicCondition;
import com.healthcare.hms.patients.history.entity.ClinicalHistoryEntry;
import com.healthcare.hms.patients.history.entity.FamilyHistory;
import com.healthcare.hms.patients.history.entity.MedicalHistory;
import com.healthcare.hms.patients.history.entity.PastDisease;
import com.healthcare.hms.patients.history.entity.SurgeryHistory;
import com.healthcare.hms.patients.history.enums.ClinicalConditionStatus;
import com.healthcare.hms.patients.history.mapper.MedicalHistoryMapper;
import com.healthcare.hms.patients.history.repository.ChronicConditionRepository;
import com.healthcare.hms.patients.history.repository.FamilyHistoryRepository;
import com.healthcare.hms.patients.history.repository.MedicalHistoryRepository;
import com.healthcare.hms.patients.history.repository.PastDiseaseRepository;
import com.healthcare.hms.patients.history.repository.SurgeryHistoryRepository;
import com.healthcare.hms.patients.history.service.MedicalHistoryService;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.patients.support.PatientAccessSupport;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tenant-isolated structured medical history (Phase 5.3).
 */
@Service
public class MedicalHistoryServiceImpl implements MedicalHistoryService {

    private static final Logger log = LoggerFactory.getLogger(MedicalHistoryServiceImpl.class);
    private static final String ENTITY_MEDICAL_HISTORY = "MEDICAL_HISTORY";
    private static final String ENTITY_PAST_DISEASE = "PAST_DISEASE";
    private static final String ENTITY_SURGERY_HISTORY = "SURGERY_HISTORY";
    private static final String ENTITY_CHRONIC_CONDITION = "CHRONIC_CONDITION";
    private static final String ENTITY_FAMILY_HISTORY = "FAMILY_HISTORY";

    private final PatientRepository patientRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PastDiseaseRepository pastDiseaseRepository;
    private final SurgeryHistoryRepository surgeryHistoryRepository;
    private final ChronicConditionRepository chronicConditionRepository;
    private final FamilyHistoryRepository familyHistoryRepository;
    private final MedicalHistoryMapper medicalHistoryMapper;
    private final AuditLogService auditLogService;

    public MedicalHistoryServiceImpl(
            final PatientRepository patientRepository,
            final MedicalHistoryRepository medicalHistoryRepository,
            final PastDiseaseRepository pastDiseaseRepository,
            final SurgeryHistoryRepository surgeryHistoryRepository,
            final ChronicConditionRepository chronicConditionRepository,
            final FamilyHistoryRepository familyHistoryRepository,
            final MedicalHistoryMapper medicalHistoryMapper,
            final AuditLogService auditLogService
    ) {
        this.patientRepository = patientRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.pastDiseaseRepository = pastDiseaseRepository;
        this.surgeryHistoryRepository = surgeryHistoryRepository;
        this.chronicConditionRepository = chronicConditionRepository;
        this.familyHistoryRepository = familyHistoryRepository;
        this.medicalHistoryMapper = medicalHistoryMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public MedicalHistoryResponse getByPatientId(final UUID patientId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);
        final MedicalHistory history = medicalHistoryRepository
                .findByTenantIdAndPatientId(tenantId, patientId)
                .orElseGet(() -> emptyHistoryShell(patientId));

        if (history.getId() == null) {
            return new MedicalHistoryResponse(
                    null,
                    patientId,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    null,
                    null,
                    null
            );
        }

        return medicalHistoryMapper.toMedicalHistoryResponse(
                history,
                pastDiseaseRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId),
                surgeryHistoryRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId),
                chronicConditionRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId),
                familyHistoryRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId)
        );
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public PastDiseaseResponse addPastDisease(
            final UUID patientId,
            final UpsertPastDiseaseRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        assertClinicalDates(request.conditionStatus(), request.recoveryDate());
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final MedicalHistory history = requireOrCreateHistory(tenantId, patientId);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        final PastDisease entry = new PastDisease();
        entry.setPatientId(patientId);
        entry.setMedicalHistoryId(history.getId());
        entry.setRecordedByUserId(actorId);
        medicalHistoryMapper.applyPastDisease(request, entry);

        final PastDisease saved = pastDiseaseRepository.save(entry);
        history.markReviewed(actorId);
        medicalHistoryRepository.save(history);

        audit(tenantId, actorId, ENTITY_PAST_DISEASE, saved.getId(), AuditAction.CREATE, null, snapshot(saved), ipAddress, userAgent);
        log.info("Past disease added id={} patientId={} tenantId={}", saved.getId(), patientId, tenantId);
        return medicalHistoryMapper.toPastDiseaseResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public PastDiseaseResponse updatePastDisease(
            final UUID patientId,
            final UUID entryId,
            final UpsertPastDiseaseRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        assertClinicalDates(request.conditionStatus(), request.recoveryDate());
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final PastDisease entry = requirePastDisease(tenantId, patientId, entryId);
        final String oldSnapshot = snapshot(entry);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        medicalHistoryMapper.applyPastDisease(request, entry);
        final PastDisease saved = pastDiseaseRepository.save(entry);
        markHistoryReviewed(tenantId, patientId, actorId);

        audit(tenantId, actorId, ENTITY_PAST_DISEASE, saved.getId(), AuditAction.UPDATE, oldSnapshot, snapshot(saved), ipAddress, userAgent);
        return medicalHistoryMapper.toPastDiseaseResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    public void removePastDisease(
            final UUID patientId,
            final UUID entryId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final PastDisease entry = requirePastDisease(tenantId, patientId, entryId);
        final String oldSnapshot = snapshot(entry);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        entry.markDeleted(actorId);
        pastDiseaseRepository.save(entry);
        markHistoryReviewed(tenantId, patientId, actorId);
        audit(tenantId, actorId, ENTITY_PAST_DISEASE, entry.getId(), AuditAction.DELETE, oldSnapshot, snapshot(entry), ipAddress, userAgent);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public SurgeryHistoryResponse addSurgery(
            final UUID patientId,
            final UpsertSurgeryHistoryRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        assertClinicalDates(request.conditionStatus(), request.recoveryDate());
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final MedicalHistory history = requireOrCreateHistory(tenantId, patientId);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        final SurgeryHistory entry = new SurgeryHistory();
        entry.setPatientId(patientId);
        entry.setMedicalHistoryId(history.getId());
        entry.setRecordedByUserId(actorId);
        medicalHistoryMapper.applySurgery(request, entry);

        final SurgeryHistory saved = surgeryHistoryRepository.save(entry);
        history.markReviewed(actorId);
        medicalHistoryRepository.save(history);

        audit(tenantId, actorId, ENTITY_SURGERY_HISTORY, saved.getId(), AuditAction.CREATE, null, snapshot(saved), ipAddress, userAgent);
        log.info("Surgery history added id={} patientId={} tenantId={}", saved.getId(), patientId, tenantId);
        return medicalHistoryMapper.toSurgeryHistoryResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public SurgeryHistoryResponse updateSurgery(
            final UUID patientId,
            final UUID entryId,
            final UpsertSurgeryHistoryRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        assertClinicalDates(request.conditionStatus(), request.recoveryDate());
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final SurgeryHistory entry = requireSurgery(tenantId, patientId, entryId);
        final String oldSnapshot = snapshot(entry);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        medicalHistoryMapper.applySurgery(request, entry);
        final SurgeryHistory saved = surgeryHistoryRepository.save(entry);
        markHistoryReviewed(tenantId, patientId, actorId);

        audit(tenantId, actorId, ENTITY_SURGERY_HISTORY, saved.getId(), AuditAction.UPDATE, oldSnapshot, snapshot(saved), ipAddress, userAgent);
        return medicalHistoryMapper.toSurgeryHistoryResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    public void removeSurgery(
            final UUID patientId,
            final UUID entryId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final SurgeryHistory entry = requireSurgery(tenantId, patientId, entryId);
        final String oldSnapshot = snapshot(entry);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        entry.markDeleted(actorId);
        surgeryHistoryRepository.save(entry);
        markHistoryReviewed(tenantId, patientId, actorId);
        audit(tenantId, actorId, ENTITY_SURGERY_HISTORY, entry.getId(), AuditAction.DELETE, oldSnapshot, snapshot(entry), ipAddress, userAgent);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public ChronicConditionResponse addChronicCondition(
            final UUID patientId,
            final UpsertChronicConditionRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        assertClinicalDates(request.conditionStatus(), request.recoveryDate());
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final MedicalHistory history = requireOrCreateHistory(tenantId, patientId);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        final ChronicCondition entry = new ChronicCondition();
        entry.setPatientId(patientId);
        entry.setMedicalHistoryId(history.getId());
        entry.setRecordedByUserId(actorId);
        medicalHistoryMapper.applyChronicCondition(request, entry);

        final ChronicCondition saved = chronicConditionRepository.save(entry);
        history.markReviewed(actorId);
        medicalHistoryRepository.save(history);

        audit(tenantId, actorId, ENTITY_CHRONIC_CONDITION, saved.getId(), AuditAction.CREATE, null, snapshot(saved), ipAddress, userAgent);
        log.info("Chronic condition added id={} patientId={} tenantId={}", saved.getId(), patientId, tenantId);
        return medicalHistoryMapper.toChronicConditionResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public ChronicConditionResponse updateChronicCondition(
            final UUID patientId,
            final UUID entryId,
            final UpsertChronicConditionRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        assertClinicalDates(request.conditionStatus(), request.recoveryDate());
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final ChronicCondition entry = requireChronicCondition(tenantId, patientId, entryId);
        final String oldSnapshot = snapshot(entry);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        medicalHistoryMapper.applyChronicCondition(request, entry);
        final ChronicCondition saved = chronicConditionRepository.save(entry);
        markHistoryReviewed(tenantId, patientId, actorId);

        audit(tenantId, actorId, ENTITY_CHRONIC_CONDITION, saved.getId(), AuditAction.UPDATE, oldSnapshot, snapshot(saved), ipAddress, userAgent);
        return medicalHistoryMapper.toChronicConditionResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    public void removeChronicCondition(
            final UUID patientId,
            final UUID entryId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final ChronicCondition entry = requireChronicCondition(tenantId, patientId, entryId);
        final String oldSnapshot = snapshot(entry);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        entry.markDeleted(actorId);
        chronicConditionRepository.save(entry);
        markHistoryReviewed(tenantId, patientId, actorId);
        audit(tenantId, actorId, ENTITY_CHRONIC_CONDITION, entry.getId(), AuditAction.DELETE, oldSnapshot, snapshot(entry), ipAddress, userAgent);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public FamilyHistoryResponse addFamilyHistory(
            final UUID patientId,
            final UpsertFamilyHistoryRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        assertClinicalDates(request.conditionStatus(), request.recoveryDate());
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final MedicalHistory history = requireOrCreateHistory(tenantId, patientId);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        final FamilyHistory entry = new FamilyHistory();
        entry.setPatientId(patientId);
        entry.setMedicalHistoryId(history.getId());
        entry.setRecordedByUserId(actorId);
        medicalHistoryMapper.applyFamilyHistory(request, entry);

        final FamilyHistory saved = familyHistoryRepository.save(entry);
        history.markReviewed(actorId);
        medicalHistoryRepository.save(history);

        audit(tenantId, actorId, ENTITY_FAMILY_HISTORY, saved.getId(), AuditAction.CREATE, null, snapshot(saved), ipAddress, userAgent);
        log.info("Family history added id={} patientId={} tenantId={}", saved.getId(), patientId, tenantId);
        return medicalHistoryMapper.toFamilyHistoryResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public FamilyHistoryResponse updateFamilyHistory(
            final UUID patientId,
            final UUID entryId,
            final UpsertFamilyHistoryRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        assertClinicalDates(request.conditionStatus(), request.recoveryDate());
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final FamilyHistory entry = requireFamilyHistory(tenantId, patientId, entryId);
        final String oldSnapshot = snapshot(entry);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        medicalHistoryMapper.applyFamilyHistory(request, entry);
        final FamilyHistory saved = familyHistoryRepository.save(entry);
        markHistoryReviewed(tenantId, patientId, actorId);

        audit(tenantId, actorId, ENTITY_FAMILY_HISTORY, saved.getId(), AuditAction.UPDATE, oldSnapshot, snapshot(saved), ipAddress, userAgent);
        return medicalHistoryMapper.toFamilyHistoryResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    public void removeFamilyHistory(
            final UUID patientId,
            final UUID entryId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final FamilyHistory entry = requireFamilyHistory(tenantId, patientId, entryId);
        final String oldSnapshot = snapshot(entry);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        entry.markDeleted(actorId);
        familyHistoryRepository.save(entry);
        markHistoryReviewed(tenantId, patientId, actorId);
        audit(tenantId, actorId, ENTITY_FAMILY_HISTORY, entry.getId(), AuditAction.DELETE, oldSnapshot, snapshot(entry), ipAddress, userAgent);
    }

    private MedicalHistory requireOrCreateHistory(final UUID tenantId, final UUID patientId) {
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        return medicalHistoryRepository.findByTenantIdAndPatientId(tenantId, patientId)
                .orElseGet(() -> {
                    final MedicalHistory created = new MedicalHistory();
                    created.setPatientId(patientId);
                    final MedicalHistory saved = medicalHistoryRepository.save(created);
                    final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
                    auditLogService.record(
                            saved.getTenantId() != null ? saved.getTenantId() : tenantId,
                            actorId,
                            ENTITY_MEDICAL_HISTORY,
                            saved.getId().toString(),
                            AuditAction.CREATE,
                            null,
                            "{patientId=" + patientId + "}",
                            null,
                            null
                    );
                    return saved;
                });
    }

    private void markHistoryReviewed(final UUID tenantId, final UUID patientId, final UUID actorId) {
        medicalHistoryRepository.findByTenantIdAndPatientId(tenantId, patientId).ifPresent(history -> {
            history.markReviewed(actorId);
            medicalHistoryRepository.save(history);
        });
    }

    private PastDisease requirePastDisease(final UUID tenantId, final UUID patientId, final UUID entryId) {
        return pastDiseaseRepository.findByIdAndTenantIdAndPatientId(entryId, tenantId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Past disease not found"));
    }

    private SurgeryHistory requireSurgery(final UUID tenantId, final UUID patientId, final UUID entryId) {
        return surgeryHistoryRepository.findByIdAndTenantIdAndPatientId(entryId, tenantId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Surgery history not found"));
    }

    private ChronicCondition requireChronicCondition(final UUID tenantId, final UUID patientId, final UUID entryId) {
        return chronicConditionRepository.findByIdAndTenantIdAndPatientId(entryId, tenantId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Chronic condition not found"));
    }

    private FamilyHistory requireFamilyHistory(final UUID tenantId, final UUID patientId, final UUID entryId) {
        return familyHistoryRepository.findByIdAndTenantIdAndPatientId(entryId, tenantId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Family history not found"));
    }

    private static MedicalHistory emptyHistoryShell(final UUID patientId) {
        final MedicalHistory shell = new MedicalHistory();
        shell.setPatientId(patientId);
        return shell;
    }

    private static void assertClinicalDates(final ClinicalConditionStatus status, final LocalDate recoveryDate) {
        if (status == ClinicalConditionStatus.RECOVERED && recoveryDate == null) {
            throw new BusinessException(
                    "RECOVERY_DATE_REQUIRED",
                    "Recovery date is required when condition status is RECOVERED"
            );
        }
    }

    private void audit(
            final UUID tenantId,
            final UUID actorId,
            final String entityType,
            final UUID entityId,
            final AuditAction action,
            final String oldSnapshot,
            final String newSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                tenantId,
                actorId,
                entityType,
                entityId.toString(),
                action,
                oldSnapshot,
                newSnapshot,
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final ClinicalHistoryEntry entry) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", entry.getId());
        fields.put("patientId", entry.getPatientId());
        fields.put("diagnosisDate", entry.getDiagnosisDate());
        fields.put("recoveryDate", entry.getRecoveryDate());
        fields.put("severity", entry.getSeverity());
        fields.put("conditionStatus", entry.getConditionStatus());
        fields.put("deleted", entry.isDeleted());
        if (entry instanceof PastDisease pastDisease) {
            fields.put("diseaseName", pastDisease.getDiseaseName());
            fields.put("diseaseCategory", pastDisease.getDiseaseCategory());
            fields.put("diseaseCode", pastDisease.getDiseaseCode());
        } else if (entry instanceof SurgeryHistory surgery) {
            fields.put("procedureName", surgery.getProcedureName());
            fields.put("procedureCategory", surgery.getProcedureCategory());
            fields.put("procedureCode", surgery.getProcedureCode());
        } else if (entry instanceof ChronicCondition chronic) {
            fields.put("conditionName", chronic.getConditionName());
            fields.put("diseaseCategory", chronic.getDiseaseCategory());
            fields.put("conditionCode", chronic.getConditionCode());
        } else if (entry instanceof FamilyHistory family) {
            fields.put("diseaseName", family.getDiseaseName());
            fields.put("diseaseCategory", family.getDiseaseCategory());
            fields.put("diseaseCode", family.getDiseaseCode());
            fields.put("familyRelation", family.getFamilyRelation());
        }
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
