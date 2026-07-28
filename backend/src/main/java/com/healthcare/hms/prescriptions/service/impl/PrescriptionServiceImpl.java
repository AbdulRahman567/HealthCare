package com.healthcare.hms.prescriptions.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.enums.ConsultationStatus;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.clinical.support.ConsultationActorScopeSupport;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.patients.support.PatientAccessSupport;
import com.healthcare.hms.prescriptions.dto.request.CancelPrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.request.CreatePrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.request.PrescriptionItemRequest;
import com.healthcare.hms.prescriptions.dto.request.PrescriptionSearchCriteria;
import com.healthcare.hms.prescriptions.dto.request.UpdatePrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionResponse;
import com.healthcare.hms.prescriptions.entity.Prescription;
import com.healthcare.hms.prescriptions.entity.PrescriptionItem;
import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import com.healthcare.hms.prescriptions.mapper.PrescriptionMapper;
import com.healthcare.hms.prescriptions.repository.PrescriptionItemRepository;
import com.healthcare.hms.prescriptions.repository.PrescriptionRepository;
import com.healthcare.hms.prescriptions.repository.PrescriptionSpecifications;
import com.healthcare.hms.prescriptions.service.PrescriptionService;
import com.healthcare.hms.prescriptions.support.PrescriptionLabelEnricher;
import com.healthcare.hms.prescriptions.support.PrescriptionNumberGenerator;
import com.healthcare.hms.prescriptions.validation.PrescriptionClinicalRules;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
 * Digital prescription authoring and lifecycle (Phase 7.5).
 *
 * <p>Prevents duplicate medicines per prescription (case-insensitive). Pharmacy
 * dispensing statuses ({@code PARTIALLY_DISPENSED}, {@code DISPENSED}) and
 * {@code pharmacy_reference} are reserved for future integration.
 */
@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionServiceImpl.class);
    private static final String ENTITY = "PRESCRIPTION";
    private static final int MAX_NUMBER_ATTEMPTS = 5;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT = Set.of(
            "prescriptionDate", "createdAt", "updatedAt", "status", "prescriptionNumber"
    );

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionLabelEnricher labelEnricher;
    private final PrescriptionNumberGenerator numberGenerator;
    private final ConsultationActorScopeSupport actorScopeSupport;
    private final AuditLogService auditLogService;

    public PrescriptionServiceImpl(
            final PrescriptionRepository prescriptionRepository,
            final PrescriptionItemRepository prescriptionItemRepository,
            final ConsultationRepository consultationRepository,
            final PatientRepository patientRepository,
            final PrescriptionMapper prescriptionMapper,
            final PrescriptionLabelEnricher labelEnricher,
            final PrescriptionNumberGenerator numberGenerator,
            final ConsultationActorScopeSupport actorScopeSupport,
            final AuditLogService auditLogService
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.prescriptionMapper = prescriptionMapper;
        this.labelEnricher = labelEnricher;
        this.numberGenerator = numberGenerator;
        this.actorScopeSupport = actorScopeSupport;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PRESCRIPTION_CREATE)
    public PrescriptionResponse create(
            final CreatePrescriptionRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requirePrescribableConsultation(tenantId, request.consultationId());
        actorScopeSupport.assertDoctorAccessible(tenantId, consultation.getDoctorId());

        final Prescription prescription = new Prescription();
        prescription.setPrescriptionNumber(allocateNumber(tenantId));
        prescription.setConsultationId(consultation.getId());
        prescription.setHospitalId(consultation.getHospitalId());
        prescription.setPatientId(consultation.getPatientId());
        prescription.setDoctorId(consultation.getDoctorId());
        prescription.setDepartmentId(consultation.getDepartmentId());
        prescription.setPrescriptionDate(
                request.prescriptionDate() != null ? request.prescriptionDate() : consultation.getConsultationDate()
        );
        prescription.setStatus(PrescriptionStatus.DRAFT);
        prescription.setNotes(trimToNull(request.notes()));

        final Prescription saved = prescriptionRepository.save(prescription);
        final List<PrescriptionItem> items = saveItems(tenantId, saved, request.items());

        if (Boolean.TRUE.equals(request.issueImmediately())) {
            saved.issue();
            prescriptionRepository.save(saved);
        }

        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info(
                "Prescription created id={} number={} consultationId={} tenantId={} actorId={}",
                saved.getId(), saved.getPrescriptionNumber(), consultation.getId(),
                tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, items);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PRESCRIPTION_READ)
    public PrescriptionResponse getById(
            final UUID prescriptionId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Prescription prescription = requirePrescription(tenantId, prescriptionId);
        assertPrescriptionAccessible(tenantId, prescription);
        final List<PrescriptionItem> items = loadItems(tenantId, prescriptionId);
        audit(prescription, AuditAction.VIEW, null, ipAddress, userAgent);
        return labelEnricher.enrichOne(tenantId, prescription, items);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PRESCRIPTION_READ)
    public List<PrescriptionResponse> listByConsultation(final UUID consultationId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireConsultationAccessible(tenantId, consultationId);
        final List<Prescription> prescriptions = prescriptionRepository
                .findByTenantIdAndConsultationIdOrderByPrescriptionDateDescCreatedAtDesc(tenantId, consultationId);
        return enrichList(tenantId, prescriptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PRESCRIPTION_READ)
    public PageResponse<PrescriptionResponse> search(
            final PrescriptionSearchCriteria criteria,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();
        validateDateRange(criteria.fromDate(), criteria.toDate());

        final UUID scopedDoctorId = actorScopeSupport.resolveScopedDoctorId(tenantId).orElse(null);
        final UUID doctorId;
        if (scopedDoctorId != null) {
            if (criteria.doctorId() != null && !criteria.doctorId().equals(scopedDoctorId)) {
                throw new BusinessException("DOCTOR_SCOPE_VIOLATION", "Cannot search other doctors' prescriptions");
            }
            doctorId = scopedDoctorId;
        } else {
            doctorId = criteria.doctorId();
        }

        final Page<Prescription> page = prescriptionRepository.findAll(
                PrescriptionSpecifications.search(
                        tenantId,
                        criteria.patientId(),
                        doctorId,
                        criteria.consultationId(),
                        criteria.status(),
                        criteria.fromDate(),
                        criteria.toDate(),
                        criteria.prescriptionNumber()
                ),
                sanitizePageable(pageable)
        );

        final List<PrescriptionResponse> content = enrichList(tenantId, page.getContent());
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
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PRESCRIPTION_READ)
    public PageResponse<PrescriptionResponse> patientHistory(final UUID patientId, final Pageable pageable) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);

        final Page<Prescription> page = prescriptionRepository.findAll(
                PrescriptionSpecifications.search(tenantId, patientId, null, null, null, null, null, null),
                sanitizePageable(pageable)
        );
        final List<PrescriptionResponse> content = enrichList(tenantId, page.getContent());
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
    @RequirePermission(PermissionConstants.PRESCRIPTION_UPDATE)
    public PrescriptionResponse update(
            final UUID prescriptionId,
            final UpdatePrescriptionRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Prescription prescription = requirePrescription(tenantId, prescriptionId);
        assertPrescriptionAccessible(tenantId, prescription);
        requireDraftMutable(prescription);
        requirePrescribableConsultation(tenantId, prescription.getConsultationId());

        final String old = snapshot(prescription);

        if (request.prescriptionDate() != null) {
            prescription.setPrescriptionDate(request.prescriptionDate());
        }
        if (request.notes() != null) {
            prescription.setNotes(trimToNull(request.notes()));
        }

        List<PrescriptionItem> items;
        if (request.items() != null) {
            softDeleteAllItems(tenantId, prescription);
            items = saveItems(tenantId, prescription, request.items());
        } else {
            items = loadItems(tenantId, prescriptionId);
        }

        final Prescription saved = prescriptionRepository.save(prescription);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Prescription updated id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, items);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PRESCRIPTION_UPDATE)
    public PrescriptionResponse issue(
            final UUID prescriptionId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Prescription prescription = requirePrescription(tenantId, prescriptionId);
        assertPrescriptionAccessible(tenantId, prescription);
        requirePrescribableConsultation(tenantId, prescription.getConsultationId());

        final long itemCount = prescriptionItemRepository.countByTenantIdAndPrescriptionId(tenantId, prescriptionId);
        if (itemCount < 1) {
            throw new BusinessException("PRESCRIPTION_EMPTY", "Cannot issue a prescription with no medicines");
        }

        final String old = snapshot(prescription);
        try {
            prescription.issue();
        } catch (final IllegalStateException ex) {
            throw new BusinessException("INVALID_PRESCRIPTION_TRANSITION", ex.getMessage());
        }

        final Prescription saved = prescriptionRepository.save(prescription);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Prescription issued id={} number={} tenantId={} actorId={}",
                saved.getId(), saved.getPrescriptionNumber(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, loadItems(tenantId, prescriptionId));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PRESCRIPTION_UPDATE)
    public PrescriptionResponse cancel(
            final UUID prescriptionId,
            final CancelPrescriptionRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Prescription prescription = requirePrescription(tenantId, prescriptionId);
        assertPrescriptionAccessible(tenantId, prescription);

        final String old = snapshot(prescription);
        try {
            prescription.cancel(trimToNull(request == null ? null : request.reason()));
        } catch (final IllegalStateException ex) {
            throw new BusinessException("INVALID_PRESCRIPTION_TRANSITION", ex.getMessage());
        }

        final Prescription saved = prescriptionRepository.save(prescription);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Prescription cancelled id={} tenantId={} actorId={}",
                saved.getId(), tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, loadItems(tenantId, prescriptionId));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.PRESCRIPTION_DELETE)
    public void delete(final UUID prescriptionId, final String ipAddress, final String userAgent) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Prescription prescription = requirePrescription(tenantId, prescriptionId);
        assertPrescriptionAccessible(tenantId, prescription);
        requireDraftMutable(prescription);

        final String old = snapshot(prescription);
        final UUID actorId = SecurityUtils.requireCurrentUserId();
        softDeleteAllItems(tenantId, prescription);
        prescription.markDeleted(actorId);
        prescriptionRepository.save(prescription);
        audit(prescription, AuditAction.DELETE, old, ipAddress, userAgent);
        log.info(
                "Prescription soft-deleted id={} tenantId={} actorId={}",
                prescriptionId, tenantId, actorId
        );
    }

    private List<PrescriptionItem> saveItems(
            final UUID tenantId,
            final Prescription prescription,
            final List<PrescriptionItemRequest> requests
    ) {
        assertNoDuplicateMedicines(requests);
        final List<PrescriptionItem> saved = new ArrayList<>(requests.size());
        int sequence = 1;
        for (final PrescriptionItemRequest request : requests) {
            final String key = PrescriptionClinicalRules.normalizeMedicineKey(request.medicineName());
            if (prescriptionItemRepository.existsByTenantIdAndPrescriptionIdAndMedicineNameKey(
                    tenantId, prescription.getId(), key)) {
                throw new BusinessException(
                        "DUPLICATE_MEDICINE",
                        "Medicine already exists on this prescription: " + request.medicineName().trim()
                );
            }
            final PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescription.getId());
            item.setPatientId(prescription.getPatientId());
            final int seq = request.sequenceNumber() != null ? request.sequenceNumber() : sequence;
            prescriptionMapper.applyItem(request, item, seq);
            saved.add(prescriptionItemRepository.save(item));
            sequence++;
        }
        return saved;
    }

    private void softDeleteAllItems(final UUID tenantId, final Prescription prescription) {
        final UUID actorId = SecurityUtils.requireCurrentUserId();
        final List<PrescriptionItem> existing = loadItems(tenantId, prescription.getId());
        for (final PrescriptionItem item : existing) {
            item.markDeleted(actorId);
            prescriptionItemRepository.save(item);
        }
    }

    private List<PrescriptionItem> loadItems(final UUID tenantId, final UUID prescriptionId) {
        return prescriptionItemRepository.findByTenantIdAndPrescriptionIdOrderBySequenceNumberAsc(
                tenantId, prescriptionId);
    }

    private List<PrescriptionResponse> enrichList(final UUID tenantId, final List<Prescription> prescriptions) {
        if (prescriptions.isEmpty()) {
            return List.of();
        }
        final Map<UUID, List<PrescriptionItem>> itemsById = new HashMap<>();
        for (final Prescription prescription : prescriptions) {
            itemsById.put(prescription.getId(), loadItems(tenantId, prescription.getId()));
        }
        return labelEnricher.enrich(tenantId, prescriptions, itemsById);
    }

    private Consultation requirePrescribableConsultation(final UUID tenantId, final UUID consultationId) {
        final Consultation consultation = requireConsultationAccessible(tenantId, consultationId);
        if (consultation.getStatus() == ConsultationStatus.CANCELLED) {
            throw new BusinessException(
                    "CONSULTATION_NOT_PRESCRIBABLE",
                    "Cannot prescribe on a cancelled consultation"
            );
        }
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, consultation.getPatientId());
        return consultation;
    }

    private Consultation requireConsultationAccessible(final UUID tenantId, final UUID consultationId) {
        final Consultation consultation = consultationRepository.findByIdAndTenantId(consultationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found"));
        actorScopeSupport.assertConsultationAccessible(tenantId, consultation);
        return consultation;
    }

    private Prescription requirePrescription(final UUID tenantId, final UUID prescriptionId) {
        return prescriptionRepository.findByIdAndTenantId(prescriptionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
    }

    private void assertPrescriptionAccessible(final UUID tenantId, final Prescription prescription) {
        actorScopeSupport.denyPatientPortalStaffApis();
        actorScopeSupport.assertDoctorAccessible(tenantId, prescription.getDoctorId());
    }

    private static void requireDraftMutable(final Prescription prescription) {
        if (!prescription.isLineItemsMutable()) {
            throw new BusinessException(
                    "PRESCRIPTION_NOT_EDITABLE",
                    "Only DRAFT prescriptions can be modified (status=" + prescription.getStatus() + ")"
            );
        }
    }

    private static void assertNoDuplicateMedicines(final List<PrescriptionItemRequest> items) {
        if (PrescriptionClinicalRules.hasDuplicateMedicines(
                items.stream().map(PrescriptionItemRequest::medicineName).toList())) {
            throw new BusinessException(
                    "DUPLICATE_MEDICINE",
                    "Duplicate medicines are not allowed on the same prescription"
            );
        }
    }

    private String allocateNumber(final UUID tenantId) {
        for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {
            final String candidate = numberGenerator.next();
            if (!prescriptionRepository.existsByTenantIdAndPrescriptionNumberIgnoreCase(tenantId, candidate)) {
                return candidate;
            }
        }
        throw new BusinessException("PRESCRIPTION_NUMBER_ALLOCATION_FAILED", "Could not allocate a unique prescription number");
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
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "prescriptionDate", "createdAt"));
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
            final Prescription prescription,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                prescription.getTenantId(),
                SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY,
                prescription.getId().toString(),
                action,
                oldSnapshot,
                snapshot(prescription),
                ipAddress,
                userAgent
        );
    }

    private static String snapshot(final Prescription prescription) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", prescription.getId());
        fields.put("prescriptionNumber", prescription.getPrescriptionNumber());
        fields.put("consultationId", prescription.getConsultationId());
        fields.put("patientId", prescription.getPatientId());
        fields.put("doctorId", prescription.getDoctorId());
        fields.put("prescriptionDate", prescription.getPrescriptionDate());
        fields.put("status", prescription.getStatus());
        fields.put("notes", prescription.getNotes() == null ? null : "[redacted]");
        fields.put("issuedAt", prescription.getIssuedAt());
        fields.put("cancelledAt", prescription.getCancelledAt());
        fields.put("pharmacyReference", prescription.getPharmacyReference());
        fields.put("deleted", prescription.isDeleted());
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
