package com.healthcare.hms.patients.immunization.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.immunization.dto.request.UpsertImmunizationRequest;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationDueResponse;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationResponse;
import com.healthcare.hms.patients.immunization.entity.Immunization;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.mapper.ImmunizationMapper;
import com.healthcare.hms.patients.immunization.repository.ImmunizationRepository;
import com.healthcare.hms.patients.immunization.service.ImmunizationService;
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
 * Tenant-isolated immunization management with due-date follow-up surface.
 */
@Service
public class ImmunizationServiceImpl implements ImmunizationService {

    private static final Logger log = LoggerFactory.getLogger(ImmunizationServiceImpl.class);
    private static final String ENTITY_IMMUNIZATION = "IMMUNIZATION";

    private final ImmunizationRepository immunizationRepository;
    private final PatientRepository patientRepository;
    private final ImmunizationMapper immunizationMapper;
    private final AuditLogService auditLogService;

    public ImmunizationServiceImpl(
            final ImmunizationRepository immunizationRepository,
            final PatientRepository patientRepository,
            final ImmunizationMapper immunizationMapper,
            final AuditLogService auditLogService
    ) {
        this.immunizationRepository = immunizationRepository;
        this.patientRepository = patientRepository;
        this.immunizationMapper = immunizationMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public List<ImmunizationResponse> list(final UUID patientId, final ImmunizationStatus status) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);

        final List<Immunization> records = status == null
                ? immunizationRepository.findByTenantIdAndPatientIdOrderByAdministrationDateDescVaccineNameAscDoseNumberAsc(
                        tenantId, patientId
                )
                : immunizationRepository.findByTenantIdAndPatientIdAndStatusOrderByAdministrationDateDescVaccineNameAscDoseNumberAsc(
                        tenantId, patientId, status
                );
        return records.stream().map(immunizationMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public ImmunizationResponse getById(final UUID patientId, final UUID immunizationId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return immunizationMapper.toResponse(requireImmunization(tenantId, patientId, immunizationId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public ImmunizationDueResponse getDue(final UUID patientId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);

        final LocalDate today = LocalDate.now();
        final List<ImmunizationResponse> due = immunizationRepository
                .findByTenantIdAndPatientIdAndStatusAndNextDueDateLessThanEqualOrderByNextDueDateAscVaccineNameAsc(
                        tenantId, patientId, ImmunizationStatus.ADMINISTERED, today
                )
                .stream()
                .map(immunizationMapper::toResponse)
                .toList();

        return new ImmunizationDueResponse(patientId, due.size(), due);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public ImmunizationResponse create(
            final UUID patientId,
            final UpsertImmunizationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        final Immunization immunization = new Immunization();
        immunization.setPatientId(patientId);
        immunization.setRecordedByUserId(actorId);
        immunizationMapper.apply(request, immunization);

        final Immunization saved = immunizationRepository.save(immunization);
        audit(tenantId, actorId, saved.getId(), AuditAction.CREATE, null, snapshot(saved), ipAddress, userAgent);
        log.info(
                "Immunization created id={} dose={} patientId={} tenantId={} actorId={}",
                saved.getId(),
                saved.getDoseNumber(),
                patientId,
                tenantId,
                actorId
        );
        return immunizationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public ImmunizationResponse update(
            final UUID patientId,
            final UUID immunizationId,
            final UpsertImmunizationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final Immunization immunization = requireImmunization(tenantId, patientId, immunizationId);
        final String oldSnapshot = snapshot(immunization);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        immunizationMapper.apply(request, immunization);
        final Immunization saved = immunizationRepository.save(immunization);
        audit(tenantId, actorId, saved.getId(), AuditAction.UPDATE, oldSnapshot, snapshot(saved), ipAddress, userAgent);
        return immunizationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    public void softDelete(
            final UUID patientId,
            final UUID immunizationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, patientId);
        final Immunization immunization = requireImmunization(tenantId, patientId, immunizationId);
        final String oldSnapshot = snapshot(immunization);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        immunization.markDeleted(actorId);
        immunizationRepository.save(immunization);
        audit(
                tenantId,
                actorId,
                immunization.getId(),
                AuditAction.DELETE,
                oldSnapshot,
                snapshot(immunization),
                ipAddress,
                userAgent
        );
    }

    private Immunization requireImmunization(final UUID tenantId, final UUID patientId, final UUID immunizationId) {
        return immunizationRepository.findByIdAndTenantIdAndPatientId(immunizationId, tenantId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Immunization not found"));
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
                ENTITY_IMMUNIZATION,
                entityId.toString(),
                action,
                oldSnapshot,
                newSnapshot,
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final Immunization immunization) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", immunization.getId());
        fields.put("patientId", immunization.getPatientId());
        fields.put("vaccineName", immunization.getVaccineName());
        fields.put("doseNumber", immunization.getDoseNumber());
        fields.put("manufacturer", immunization.getManufacturer());
        fields.put("batchNumber", immunization.getBatchNumber());
        fields.put("administrationDate", immunization.getAdministrationDate());
        fields.put("nextDueDate", immunization.getNextDueDate());
        fields.put("healthcareProvider", immunization.getHealthcareProvider());
        fields.put("status", immunization.getStatus());
        fields.put("deleted", immunization.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
