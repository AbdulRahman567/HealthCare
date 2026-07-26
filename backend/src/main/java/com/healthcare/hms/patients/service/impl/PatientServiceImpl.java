package com.healthcare.hms.patients.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.dto.request.PatientSearchCriteria;
import com.healthcare.hms.patients.dto.request.RegisterPatientRequest;
import com.healthcare.hms.patients.dto.request.UpdatePatientRequest;
import com.healthcare.hms.patients.dto.response.PatientResponse;
import com.healthcare.hms.patients.entity.EmergencyContact;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.PatientStatus;
import com.healthcare.hms.patients.mapper.PatientMapper;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.patients.repository.PatientSpecifications;
import com.healthcare.hms.patients.service.PatientService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
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
 * Tenant-isolated patient registration and lifecycle (Phase 5.2).
 *
 * <p>No physical deletion — deactivate / reactivate only.
 */
@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);
    private static final String ENTITY_PATIENT = "PATIENT";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_AGE = 150;
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "mrn",
            "firstName",
            "lastName",
            "dateOfBirth",
            "gender",
            "bloodGroup",
            "phone",
            "email",
            "nationalId",
            "status",
            "createdAt",
            "updatedAt"
    );

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final AuditLogService auditLogService;

    public PatientServiceImpl(
            final PatientRepository patientRepository,
            final PatientMapper patientMapper,
            final AuditLogService auditLogService
    ) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_CREATE)
    public PatientResponse register(
            final RegisterPatientRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final String normalizedMrn = normalizeMrn(request.mrn());
        assertUniqueMrn(tenantId, normalizedMrn, null);
        assertUniqueNationalId(tenantId, request.nationalId(), null);

        final Patient patient = new Patient();
        patient.setStatus(PatientStatus.ACTIVE);
        patientMapper.applyRegister(request, patient);

        final Patient saved = patientRepository.save(patient);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_PATIENT,
                saved.getId().toString(),
                AuditAction.CREATE,
                null,
                snapshot(saved),
                ipAddress,
                userAgent
        );

        log.info(
                "Patient registered id={} tenantId={} actorId={}",
                saved.getId(),
                saved.getTenantId(),
                actorId
        );
        return patientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public PatientResponse getById(final UUID patientId, final String ipAddress, final String userAgent) {
        final Patient patient = requirePatient(patientId);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                patient.getTenantId(),
                actorId,
                ENTITY_PATIENT,
                patient.getId().toString(),
                AuditAction.VIEW,
                null,
                "{id=" + patient.getId() + "}",
                ipAddress,
                userAgent
        );
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public PageResponse<PatientResponse> search(final PatientSearchCriteria criteria, final Pageable pageable) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        validateAgeCriteria(criteria);
        validateDateOfBirthRange(criteria);

        final Pageable safePageable = sanitizePageable(pageable);
        final Page<PatientResponse> page = patientRepository
                .findAll(
                        PatientSpecifications.withFilters(
                                tenantId,
                                criteria.q(),
                                criteria.mrn(),
                                criteria.firstName(),
                                criteria.lastName(),
                                criteria.phone(),
                                criteria.email(),
                                criteria.nationalId(),
                                criteria.status(),
                                criteria.bloodGroup(),
                                criteria.gender(),
                                criteria.dateOfBirth(),
                                criteria.dateOfBirthFrom(),
                                criteria.dateOfBirthTo(),
                                criteria.ageMin(),
                                criteria.ageMax(),
                                criteria.departmentId(),
                                criteria.doctorId(),
                                LocalDate.now()
                        ),
                        safePageable
                )
                .map(patientMapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public PatientResponse update(
            final UUID patientId,
            final UpdatePatientRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final Patient patient = requirePatient(patientId);
        final UUID tenantId = patient.getTenantId();
        final String oldSnapshot = snapshot(patient);

        final String normalizedMrn = normalizeMrn(request.mrn());
        assertUniqueMrn(tenantId, normalizedMrn, patient.getId());
        assertUniqueNationalId(tenantId, request.nationalId(), patient.getId());

        patientMapper.applyUpdate(request, patient);
        final Patient saved = patientRepository.save(patient);

        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_PATIENT,
                saved.getId().toString(),
                AuditAction.UPDATE,
                oldSnapshot,
                snapshot(saved),
                ipAddress,
                userAgent
        );

        log.info(
                "Patient updated id={} tenantId={} actorId={}",
                saved.getId(),
                saved.getTenantId(),
                actorId
        );
        return patientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public PatientResponse deactivate(
            final UUID patientId,
            final String ipAddress,
            final String userAgent
    ) {
        final Patient patient = requirePatient(patientId);
        final String oldSnapshot = snapshot(patient);
        try {
            patient.deactivate();
        } catch (final IllegalStateException exception) {
            throw new BusinessException("PATIENT_INVALID_STATUS_TRANSITION", exception.getMessage());
        }

        final Patient saved = patientRepository.save(patient);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_PATIENT,
                saved.getId().toString(),
                AuditAction.UPDATE,
                oldSnapshot,
                snapshot(saved),
                ipAddress,
                userAgent
        );

        log.info(
                "Patient deactivated id={} tenantId={} actorId={}",
                saved.getId(),
                saved.getTenantId(),
                actorId
        );
        return patientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    public PatientResponse reactivate(
            final UUID patientId,
            final String ipAddress,
            final String userAgent
    ) {
        final Patient patient = requirePatient(patientId);
        final String oldSnapshot = snapshot(patient);
        try {
            patient.reactivate();
        } catch (final IllegalStateException exception) {
            throw new BusinessException("PATIENT_INVALID_STATUS_TRANSITION", exception.getMessage());
        }

        final Patient saved = patientRepository.save(patient);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_PATIENT,
                saved.getId().toString(),
                AuditAction.UPDATE,
                oldSnapshot,
                snapshot(saved),
                ipAddress,
                userAgent
        );

        log.info(
                "Patient reactivated id={} tenantId={} actorId={}",
                saved.getId(),
                saved.getTenantId(),
                actorId
        );
        return patientMapper.toResponse(saved);
    }

    private Patient requirePatient(final UUID patientId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    private static void validateAgeCriteria(final PatientSearchCriteria criteria) {
        final Integer ageMin = criteria.ageMin();
        final Integer ageMax = criteria.ageMax();
        if (ageMin != null && (ageMin < 0 || ageMin > MAX_AGE)) {
            throw new BusinessException("Invalid ageMin: must be between 0 and " + MAX_AGE);
        }
        if (ageMax != null && (ageMax < 0 || ageMax > MAX_AGE)) {
            throw new BusinessException("Invalid ageMax: must be between 0 and " + MAX_AGE);
        }
        if (ageMin != null && ageMax != null && ageMin > ageMax) {
            throw new BusinessException("ageMin must not be greater than ageMax");
        }
    }

    private static void validateDateOfBirthRange(final PatientSearchCriteria criteria) {
        if (criteria.dateOfBirthFrom() != null
                && criteria.dateOfBirthTo() != null
                && criteria.dateOfBirthFrom().isAfter(criteria.dateOfBirthTo())) {
            throw new BusinessException("dateOfBirthFrom must not be after dateOfBirthTo");
        }
    }

    private static Pageable sanitizePageable(final Pageable pageable) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName", "firstName"));
        }

        final Sort safeSort = Sort.by(pageable.getSort().stream()
                .filter(order -> ALLOWED_SORT_PROPERTIES.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList());

        if (safeSort.isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName", "firstName"));
        }
        return PageRequest.of(page, size, safeSort);
    }

    private void assertUniqueMrn(final UUID tenantId, final String mrn, final UUID excludeId) {
        final boolean exists = excludeId == null
                ? patientRepository.existsByTenantIdAndMrnIgnoreCase(tenantId, mrn)
                : patientRepository.existsByTenantIdAndMrnIgnoreCaseAndIdNot(tenantId, mrn, excludeId);
        if (exists) {
            throw new ConflictException("PATIENT_MRN_EXISTS", "MRN is already in use for this hospital");
        }
    }

    private void assertUniqueNationalId(final UUID tenantId, final String nationalId, final UUID excludeId) {
        if (nationalId == null || nationalId.isBlank()) {
            return;
        }
        final String normalized = nationalId.trim();
        final boolean exists = excludeId == null
                ? patientRepository.existsByTenantIdAndNationalIdIgnoreCase(tenantId, normalized)
                : patientRepository.existsByTenantIdAndNationalIdIgnoreCaseAndIdNot(tenantId, normalized, excludeId);
        if (exists) {
            throw new ConflictException(
                    "PATIENT_NATIONAL_ID_EXISTS",
                    "National ID is already in use for this hospital"
            );
        }
    }

    private static String normalizeMrn(final String mrn) {
        return mrn.trim().toUpperCase(Locale.ROOT);
    }

    private static String snapshot(final Patient patient) {
        final EmergencyContact contact = patient.getEmergencyContact();
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", patient.getId());
        fields.put("mrn", patient.getMrn());
        fields.put("firstName", patient.getFirstName());
        fields.put("lastName", patient.getLastName());
        fields.put("dateOfBirth", patient.getDateOfBirth());
        fields.put("gender", patient.getGender());
        fields.put("bloodGroup", patient.getBloodGroup());
        fields.put("nationalId", patient.getNationalId());
        fields.put("phone", patient.getPhone());
        fields.put("email", patient.getEmail());
        fields.put("status", patient.getStatus());
        fields.put(
                "emergencyContact",
                contact == null
                        ? null
                        : contact.getName() + "|" + contact.getPhone() + "|" + contact.getRelation()
        );
        fields.put("deleted", patient.isDeleted());
        return fields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
