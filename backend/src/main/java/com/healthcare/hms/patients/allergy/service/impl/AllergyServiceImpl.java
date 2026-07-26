package com.healthcare.hms.patients.allergy.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.patients.allergy.dto.request.UpsertAllergyRequest;
import com.healthcare.hms.patients.allergy.dto.response.AllergyBannerResponse;
import com.healthcare.hms.patients.allergy.dto.response.AllergyCriticalAlertResponse;
import com.healthcare.hms.patients.allergy.dto.response.AllergyResponse;
import com.healthcare.hms.patients.allergy.entity.Allergy;
import com.healthcare.hms.patients.allergy.enums.AllergyStatus;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import com.healthcare.hms.patients.allergy.enums.Reaction;
import com.healthcare.hms.patients.allergy.enums.Severity;
import com.healthcare.hms.patients.allergy.mapper.AllergyMapper;
import com.healthcare.hms.patients.allergy.repository.AllergyRepository;
import com.healthcare.hms.patients.allergy.service.AllergyService;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.patients.support.PatientAccessSupport;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.authorization.AuthorizationService;
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

/**
 * Tenant-isolated allergy management with banner / critical alert surfaces.
 */
@Service
public class AllergyServiceImpl implements AllergyService {

    private static final Logger log = LoggerFactory.getLogger(AllergyServiceImpl.class);
    private static final String ENTITY_ALLERGY = "ALLERGY";

    private final AllergyRepository allergyRepository;
    private final PatientRepository patientRepository;
    private final AllergyMapper allergyMapper;
    private final AuditLogService auditLogService;
    private final AuthorizationService authorizationService;

    public AllergyServiceImpl(
            final AllergyRepository allergyRepository,
            final PatientRepository patientRepository,
            final AllergyMapper allergyMapper,
            final AuditLogService auditLogService,
            final AuthorizationService authorizationService
    ) {
        this.allergyRepository = allergyRepository;
        this.patientRepository = patientRepository;
        this.allergyMapper = allergyMapper;
        this.auditLogService = auditLogService;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public List<AllergyResponse> list(final UUID patientId, final AllergyType allergyType) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);

        final List<Allergy> allergies = allergyType == null
                ? allergyRepository.findByTenantIdAndPatientIdOrderBySeverityDescAllergenNameAsc(tenantId, patientId)
                : allergyRepository.findByTenantIdAndPatientIdAndAllergyTypeOrderBySeverityDescAllergenNameAsc(
                        tenantId, patientId, allergyType
                );
        return allergies.stream().map(allergyMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public AllergyResponse getById(final UUID patientId, final UUID allergyId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return allergyMapper.toResponse(requireAllergy(tenantId, patientId, allergyId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public AllergyBannerResponse getBannerAlerts(final UUID patientId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);

        final List<AllergyResponse> banner = allergyRepository
                .findByTenantIdAndPatientIdAndStatusAndShowOnBannerTrueOrderBySeverityDescAllergenNameAsc(
                        tenantId, patientId, AllergyStatus.ACTIVE
                )
                .stream()
                .map(allergyMapper::toResponse)
                .toList();

        final long criticalCount = allergyRepository.countByTenantIdAndPatientIdAndStatusAndCriticalAlertTrue(
                tenantId, patientId, AllergyStatus.ACTIVE
        );
        final boolean hasDrug = allergyRepository.existsByTenantIdAndPatientIdAndStatusAndAllergyType(
                tenantId, patientId, AllergyStatus.ACTIVE, AllergyType.DRUG
        );

        return new AllergyBannerResponse(
                patientId,
                criticalCount > 0,
                criticalCount,
                hasDrug,
                !hasDrug,
                banner
        );
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public AllergyCriticalAlertResponse getCriticalAlerts(final UUID patientId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);

        final List<AllergyResponse> critical = allergyRepository
                .findByTenantIdAndPatientIdAndStatusAndCriticalAlertTrueOrderBySeverityDescAllergenNameAsc(
                        tenantId, patientId, AllergyStatus.ACTIVE
                )
                .stream()
                .map(allergyMapper::toResponse)
                .toList();

        return new AllergyCriticalAlertResponse(patientId, critical.size(), critical);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public AllergyResponse create(
            final UUID patientId,
            final UpsertAllergyRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        final Allergy allergy = new Allergy();
        allergy.setPatientId(patientId);
        allergy.setRecordedByUserId(actorId);
        allergyMapper.apply(request, allergy);

        final Allergy saved = allergyRepository.save(allergy);
        audit(tenantId, actorId, saved.getId(), AuditAction.CREATE, null, snapshot(saved), ipAddress, userAgent);
        log.info(
                "Allergy created id={} patientId={} tenantId={} actorId={}",
                saved.getId(),
                patientId,
                tenantId,
                actorId
        );
        return allergyMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public AllergyResponse update(
            final UUID patientId,
            final UUID allergyId,
            final UpsertAllergyRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final Allergy allergy = requireAllergy(tenantId, patientId, allergyId);
        assertMayAlterSafetyCriticalAllergy(allergy, request);
        final String oldSnapshot = snapshot(allergy);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        allergyMapper.apply(request, allergy);
        final Allergy saved = allergyRepository.save(allergy);
        audit(tenantId, actorId, saved.getId(), AuditAction.UPDATE, oldSnapshot, snapshot(saved), ipAddress, userAgent);
        return allergyMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    public void softDelete(
            final UUID patientId,
            final UUID allergyId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final Allergy allergy = requireAllergy(tenantId, patientId, allergyId);
        if (allergy.isLifeThreatening() || allergy.isCriticalAlert()) {
            // Already gated by PATIENT_DELETE; keep explicit guard for clarity in audits/tests.
            authorizationService.requirePermission(PermissionConstants.PATIENT_DELETE);
        }
        final String oldSnapshot = snapshot(allergy);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        allergy.markDeleted(actorId);
        allergyRepository.save(allergy);
        audit(tenantId, actorId, allergy.getId(), AuditAction.DELETE, oldSnapshot, snapshot(allergy), ipAddress, userAgent);
    }

    /**
     * Downgrading or deactivating a previously life-threatening / critical allergy
     * requires {@code PATIENT_DELETE} (Hospital Admin+) so receptionists cannot hide
     * safety-critical chart alerts via UPDATE.
     */
    private void assertMayAlterSafetyCriticalAllergy(final Allergy existing, final UpsertAllergyRequest request) {
        if (!existing.isLifeThreatening() && !existing.isCriticalAlert()) {
            return;
        }

        final AllergyStatus nextStatus = request.status() != null ? request.status() : existing.getStatus();
        final Severity nextSeverity = request.severity() != null ? request.severity() : existing.getSeverity();
        final Reaction nextReaction = request.reaction() != null ? request.reaction() : existing.getReaction();
        final boolean nextLifeThreatening =
                nextSeverity == Severity.LIFE_THREATENING || nextReaction == Reaction.ANAPHYLAXIS;
        final boolean deactivating = nextStatus != AllergyStatus.ACTIVE;
        final boolean downgrading = existing.isLifeThreatening() && !nextLifeThreatening;
        // Omitted criticalAlert preserves existing (mapper patch semantics). Explicit false clears.
        final boolean clearingCriticalFlag =
                existing.isCriticalAlert()
                        && Boolean.FALSE.equals(request.criticalAlert())
                        && !nextLifeThreatening;

        if (deactivating || downgrading || clearingCriticalFlag) {
            if (!authorizationService.hasPermission(PermissionConstants.PATIENT_DELETE)) {
                throw new BusinessException(
                        "ALLERGY_CRITICAL_CHANGE_FORBIDDEN",
                        "Downgrading or deactivating a critical allergy requires PATIENT_DELETE"
                );
            }
        }
    }

    private Allergy requireAllergy(final UUID tenantId, final UUID patientId, final UUID allergyId) {
        return allergyRepository.findByIdAndTenantIdAndPatientId(allergyId, tenantId, patientId)
                .orElseThrow(() -> new com.healthcare.hms.common.exception.ResourceNotFoundException("Allergy not found"));
    }

    private void audit(
            final UUID tenantId,
            final UUID actorId,
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
                ENTITY_ALLERGY,
                entityId.toString(),
                action,
                oldSnapshot,
                newSnapshot,
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final Allergy allergy) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", allergy.getId());
        fields.put("patientId", allergy.getPatientId());
        fields.put("allergenName", allergy.getAllergenName());
        fields.put("allergyType", allergy.getAllergyType());
        fields.put("severity", allergy.getSeverity());
        fields.put("reaction", allergy.getReaction());
        fields.put("status", allergy.getStatus());
        fields.put("criticalAlert", allergy.isCriticalAlert());
        fields.put("showOnBanner", allergy.isShowOnBanner());
        fields.put("verified", allergy.isVerified());
        fields.put("deleted", allergy.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
