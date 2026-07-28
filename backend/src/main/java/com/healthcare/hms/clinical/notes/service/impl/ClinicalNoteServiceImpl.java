package com.healthcare.hms.clinical.notes.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.clinical.entity.ClinicalNote;
import com.healthcare.hms.clinical.entity.ClinicalNoteAttachment;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.enums.ClinicalAttachmentKind;
import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import com.healthcare.hms.clinical.notes.dto.request.CreateClinicalNoteRequest;
import com.healthcare.hms.clinical.notes.dto.request.UpdateClinicalNoteRequest;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteAttachmentResponse;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteResponse;
import com.healthcare.hms.clinical.notes.mapper.ClinicalNoteMapper;
import com.healthcare.hms.clinical.notes.service.ClinicalNoteAttachmentDownload;
import com.healthcare.hms.clinical.notes.service.ClinicalNoteService;
import com.healthcare.hms.clinical.notes.support.ClinicalNoteLabelEnricher;
import com.healthcare.hms.clinical.notes.validation.ClinicalAttachmentRules;
import com.healthcare.hms.clinical.repository.ClinicalNoteAttachmentRepository;
import com.healthcare.hms.clinical.repository.ClinicalNoteRepository;
import com.healthcare.hms.clinical.repository.ClinicalNoteSpecifications;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.clinical.support.ConsultationActorScopeSupport;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.common.storage.ObjectStorageException;
import com.healthcare.hms.common.storage.ObjectStorageService;
import com.healthcare.hms.common.storage.ObjectStoreRequest;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.patients.support.PatientAccessSupport;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Clinical notes with SOAP / progress / procedure / discharge types and attachments (Phase 7.6).
 */
@Service
public class ClinicalNoteServiceImpl implements ClinicalNoteService {

    private static final Logger log = LoggerFactory.getLogger(ClinicalNoteServiceImpl.class);
    private static final String ENTITY_NOTE = "CLINICAL_NOTE";
    private static final String ENTITY_ATTACHMENT = "CLINICAL_NOTE_ATTACHMENT";
    private static final String STORAGE_CATEGORY = "clinical-notes";
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT = Set.of("recordedAt", "createdAt", "noteType");

    private final ClinicalNoteRepository clinicalNoteRepository;
    private final ClinicalNoteAttachmentRepository attachmentRepository;
    private final ConsultationRepository consultationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ClinicalNoteMapper clinicalNoteMapper;
    private final ClinicalNoteLabelEnricher labelEnricher;
    private final ConsultationActorScopeSupport actorScopeSupport;
    private final ObjectStorageService objectStorageService;
    private final AuditLogService auditLogService;

    public ClinicalNoteServiceImpl(
            final ClinicalNoteRepository clinicalNoteRepository,
            final ClinicalNoteAttachmentRepository attachmentRepository,
            final ConsultationRepository consultationRepository,
            final DoctorRepository doctorRepository,
            final PatientRepository patientRepository,
            final ClinicalNoteMapper clinicalNoteMapper,
            final ClinicalNoteLabelEnricher labelEnricher,
            final ConsultationActorScopeSupport actorScopeSupport,
            final ObjectStorageService objectStorageService,
            final AuditLogService auditLogService
    ) {
        this.clinicalNoteRepository = clinicalNoteRepository;
        this.attachmentRepository = attachmentRepository;
        this.consultationRepository = consultationRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.clinicalNoteMapper = clinicalNoteMapper;
        this.labelEnricher = labelEnricher;
        this.actorScopeSupport = actorScopeSupport;
        this.objectStorageService = objectStorageService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public ClinicalNoteResponse create(
            final UUID consultationId,
            final CreateClinicalNoteRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditableConsultation(tenantId, consultationId);
        final UUID authorDoctorId = resolveAuthorDoctorId(tenantId, consultation, request.authorDoctorId());
        final Instant recordedAt = request.recordedAt() != null ? request.recordedAt() : Instant.now();

        final ClinicalNote note = new ClinicalNote();
        note.setConsultationId(consultation.getId());
        note.setPatientId(consultation.getPatientId());
        clinicalNoteMapper.applyCreate(request, note, authorDoctorId, recordedAt);

        final ClinicalNote saved = clinicalNoteRepository.save(note);
        auditNote(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info(
                "Clinical note created id={} type={} consultationId={} tenantId={} actorId={}",
                saved.getId(), saved.getNoteType(), consultationId, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public ClinicalNoteResponse getById(
            final UUID consultationId,
            final UUID noteId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final ClinicalNote note = requireNote(tenantId, consultationId, noteId);
        auditNote(note, AuditAction.VIEW, null, ipAddress, userAgent);
        return labelEnricher.enrichOne(tenantId, note, consultation, loadAttachments(tenantId, noteId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public List<ClinicalNoteResponse> listByConsultation(
            final UUID consultationId,
            final ClinicalNoteType noteType
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireConsultation(tenantId, consultationId);
        final List<ClinicalNote> notes = noteType == null
                ? clinicalNoteRepository.findByTenantIdAndConsultationIdOrderByRecordedAtAsc(tenantId, consultationId)
                : clinicalNoteRepository.findByTenantIdAndConsultationIdAndNoteTypeOrderByRecordedAtAsc(
                        tenantId, consultationId, noteType);

        final Map<UUID, List<ClinicalNoteAttachment>> attachments = loadAttachmentsMap(tenantId, notes);
        return labelEnricher.enrich(
                tenantId,
                notes,
                Map.of(consultation.getId(), consultation),
                attachments
        );
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    public ClinicalNoteResponse update(
            final UUID consultationId,
            final UUID noteId,
            final UpdateClinicalNoteRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Consultation consultation = requireEditableConsultation(tenantId, consultationId);
        final ClinicalNote note = requireNote(tenantId, consultationId, noteId);
        final String old = noteSnapshot(note);

        if (request.authorDoctorId() != null) {
            assertDoctorExists(tenantId, request.authorDoctorId());
            actorScopeSupport.assertDoctorAccessible(tenantId, request.authorDoctorId());
        }
        try {
            clinicalNoteMapper.applyUpdate(request, note);
        } catch (final IllegalArgumentException ex) {
            throw new BusinessException("INVALID_NOTE_CONTENT", ex.getMessage());
        }

        final ClinicalNote saved = clinicalNoteRepository.save(note);
        auditNote(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        log.info(
                "Clinical note updated id={} consultationId={} tenantId={} actorId={}",
                saved.getId(), consultationId, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return labelEnricher.enrichOne(tenantId, saved, consultation, loadAttachments(tenantId, noteId));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    public void delete(
            final UUID consultationId,
            final UUID noteId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireEditableConsultation(tenantId, consultationId);
        final ClinicalNote note = requireNote(tenantId, consultationId, noteId);
        final String old = noteSnapshot(note);
        final UUID actorId = SecurityUtils.requireCurrentUserId();

        for (final ClinicalNoteAttachment attachment : loadAttachments(tenantId, noteId)) {
            softDeleteAttachmentInternal(attachment, actorId, ipAddress, userAgent);
        }

        note.markDeleted(actorId);
        clinicalNoteRepository.save(note);
        auditNote(note, AuditAction.DELETE, old, ipAddress, userAgent);
        log.info(
                "Clinical note soft-deleted id={} consultationId={} tenantId={} actorId={}",
                noteId, consultationId, tenantId, actorId
        );
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public PageResponse<ClinicalNoteResponse> patientHistory(
            final UUID patientId,
            final ClinicalNoteType noteType,
            final LocalDate fromDate,
            final LocalDate toDate,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        actorScopeSupport.denyPatientPortalStaffApis();
        PatientAccessSupport.requirePatient(patientRepository, tenantId, patientId);
        validateDateRange(fromDate, toDate);

        final Page<ClinicalNote> page = clinicalNoteRepository.findAll(
                ClinicalNoteSpecifications.forPatientHistory(tenantId, patientId, noteType, fromDate, toDate),
                sanitizePageable(pageable)
        );

        final Map<UUID, Consultation> consultations = loadConsultations(tenantId, page.getContent());
        final Map<UUID, List<ClinicalNoteAttachment>> attachments = loadAttachmentsMap(tenantId, page.getContent());
        final List<ClinicalNoteResponse> content = labelEnricher.enrich(
                tenantId, page.getContent(), consultations, attachments);

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
    public ClinicalNoteAttachmentResponse addAttachment(
            final UUID consultationId,
            final UUID noteId,
            final MultipartFile file,
            final String description,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireEditableConsultation(tenantId, consultationId);
        final ClinicalNote note = requireNote(tenantId, consultationId, noteId);

        if (file == null || file.isEmpty()) {
            throw new BusinessException("ATTACHMENT_EMPTY", "Attachment file is required");
        }

        final String originalFileName = file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename();
        final String contentType = ClinicalAttachmentRules.normalizeContentType(file.getContentType());
        if (!ClinicalAttachmentRules.isAllowed(contentType, originalFileName)) {
            throw new BusinessException(
                    "ATTACHMENT_TYPE_NOT_ALLOWED",
                    "Only PDF and image files (JPG, JPEG, PNG, WEBP) are allowed"
            );
        }

        final ClinicalAttachmentKind kind = ClinicalAttachmentRules.resolveKind(contentType, originalFileName);
        final long maxBytes = ClinicalAttachmentRules.maxBytesFor(kind);
        if (file.getSize() > maxBytes) {
            throw new BusinessException(
                    "ATTACHMENT_TOO_LARGE",
                    "File exceeds maximum size of " + (maxBytes / (1024 * 1024)) + " MB for " + kind
            );
        }

        final byte[] magicHeader;
        try {
            magicHeader = file.getInputStream().readNBytes(12);
        } catch (final IOException ex) {
            throw new BusinessException("ATTACHMENT_READ_FAILED", "Unable to read uploaded file");
        }
        if (!ClinicalAttachmentRules.matchesMagicBytes(kind, magicHeader)) {
            throw new BusinessException(
                    "ATTACHMENT_CONTENT_MISMATCH",
                    "File content does not match the declared image/PDF type"
            );
        }

        final long existing = attachmentRepository.countByTenantIdAndClinicalNoteId(tenantId, noteId);
        if (existing >= ClinicalAttachmentRules.MAX_ATTACHMENTS_PER_NOTE) {
            throw new BusinessException(
                    "ATTACHMENT_LIMIT_REACHED",
                    "A note may have at most " + ClinicalAttachmentRules.MAX_ATTACHMENTS_PER_NOTE + " attachments"
            );
        }

        final String storageKey;
        try (InputStream inputStream = file.getInputStream()) {
            storageKey = objectStorageService.put(new ObjectStoreRequest(
                    tenantId,
                    STORAGE_CATEGORY,
                    originalFileName,
                    contentType,
                    file.getSize(),
                    inputStream
            ));
        } catch (final IOException | ObjectStorageException ex) {
            throw new BusinessException("ATTACHMENT_STORAGE_FAILED", "Failed to store attachment: " + ex.getMessage());
        }

        final ClinicalNoteAttachment attachment = new ClinicalNoteAttachment();
        attachment.setClinicalNoteId(note.getId());
        attachment.setConsultationId(note.getConsultationId());
        attachment.setPatientId(note.getPatientId());
        attachment.setUploadedByUserId(SecurityUtils.requireCurrentUserId());
        attachment.setFileName(sanitizeFileName(originalFileName));
        attachment.setContentType(contentType);
        attachment.setSizeBytes(file.getSize());
        attachment.setStorageKey(storageKey);
        attachment.setAttachmentKind(kind);
        attachment.setDescription(trimToNull(description));

        final ClinicalNoteAttachment saved = attachmentRepository.save(attachment);
        auditAttachment(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info(
                "Clinical note attachment uploaded id={} noteId={} kind={} tenantId={} actorId={}",
                saved.getId(), noteId, kind, tenantId, SecurityUtils.requireCurrentUserId()
        );
        return clinicalNoteMapper.toAttachmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public List<ClinicalNoteAttachmentResponse> listAttachments(final UUID consultationId, final UUID noteId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireConsultation(tenantId, consultationId);
        requireNote(tenantId, consultationId, noteId);
        return loadAttachments(tenantId, noteId).stream()
                .map(clinicalNoteMapper::toAttachmentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public ClinicalNoteAttachmentDownload downloadAttachment(
            final UUID consultationId,
            final UUID noteId,
            final UUID attachmentId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireConsultation(tenantId, consultationId);
        requireNote(tenantId, consultationId, noteId);
        final ClinicalNoteAttachment attachment = requireAttachment(tenantId, noteId, attachmentId);
        auditAttachment(attachment, AuditAction.VIEW, null, ipAddress, userAgent);
        try {
            final InputStream stream = objectStorageService.get(attachment.getStorageKey());
            final Resource resource = new InputStreamResource(stream) {
                @Override
                public String getFilename() {
                    return attachment.getFileName();
                }

                @Override
                public long contentLength() {
                    return attachment.getSizeBytes();
                }
            };
            return new ClinicalNoteAttachmentDownload(
                    resource,
                    attachment.getFileName(),
                    attachment.getContentType(),
                    attachment.getSizeBytes()
            );
        } catch (final ObjectStorageException ex) {
            throw new BusinessException("ATTACHMENT_STORAGE_FAILED", "Failed to download attachment: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.VISIT_READ)
    public ClinicalNoteAttachmentResponse getAttachmentMetadata(
            final UUID consultationId,
            final UUID noteId,
            final UUID attachmentId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireConsultation(tenantId, consultationId);
        requireNote(tenantId, consultationId, noteId);
        final ClinicalNoteAttachment attachment = requireAttachment(tenantId, noteId, attachmentId);
        auditAttachment(attachment, AuditAction.VIEW, null, ipAddress, userAgent);
        return clinicalNoteMapper.toAttachmentResponse(attachment);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    public void deleteAttachment(
            final UUID consultationId,
            final UUID noteId,
            final UUID attachmentId,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        requireEditableConsultation(tenantId, consultationId);
        requireNote(tenantId, consultationId, noteId);
        final ClinicalNoteAttachment attachment = requireAttachment(tenantId, noteId, attachmentId);
        softDeleteAttachmentInternal(attachment, SecurityUtils.requireCurrentUserId(), ipAddress, userAgent);
        log.info(
                "Clinical note attachment soft-deleted id={} noteId={} tenantId={} actorId={}",
                attachmentId, noteId, tenantId, SecurityUtils.requireCurrentUserId()
        );
    }

    private void softDeleteAttachmentInternal(
            final ClinicalNoteAttachment attachment,
            final UUID actorId,
            final String ipAddress,
            final String userAgent
    ) {
        final String old = attachmentSnapshot(attachment);
        try {
            objectStorageService.delete(attachment.getStorageKey());
        } catch (final ObjectStorageException ex) {
            log.warn("Failed to delete storage object key={} — continuing soft-delete", attachment.getStorageKey(), ex);
        }
        attachment.markDeleted(actorId);
        attachmentRepository.save(attachment);
        auditAttachment(attachment, AuditAction.DELETE, old, ipAddress, userAgent);
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
                    "Clinical notes can only be modified while consultation is editable (status="
                            + consultation.getStatus() + ")"
            );
        }
        PatientAccessSupport.requireActivePatient(patientRepository, tenantId, consultation.getPatientId());
        return consultation;
    }

    private ClinicalNote requireNote(final UUID tenantId, final UUID consultationId, final UUID noteId) {
        return clinicalNoteRepository.findByIdAndTenantIdAndConsultationId(noteId, tenantId, consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinical note not found"));
    }

    private ClinicalNoteAttachment requireAttachment(
            final UUID tenantId,
            final UUID noteId,
            final UUID attachmentId
    ) {
        return attachmentRepository.findByIdAndTenantIdAndClinicalNoteId(attachmentId, tenantId, noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    }

    private UUID resolveAuthorDoctorId(
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

    private List<ClinicalNoteAttachment> loadAttachments(final UUID tenantId, final UUID noteId) {
        return attachmentRepository.findByTenantIdAndClinicalNoteIdOrderByCreatedAtAsc(tenantId, noteId);
    }

    private Map<UUID, List<ClinicalNoteAttachment>> loadAttachmentsMap(
            final UUID tenantId,
            final List<ClinicalNote> notes
    ) {
        final Map<UUID, List<ClinicalNoteAttachment>> map = new HashMap<>();
        for (final ClinicalNote note : notes) {
            map.put(note.getId(), loadAttachments(tenantId, note.getId()));
        }
        return map;
    }

    private Map<UUID, Consultation> loadConsultations(final UUID tenantId, final List<ClinicalNote> notes) {
        final Set<UUID> consultationIds = notes.stream()
                .map(ClinicalNote::getConsultationId)
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

    private void auditNote(
            final ClinicalNote note,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                note.getTenantId(),
                SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY_NOTE,
                note.getId().toString(),
                action,
                oldSnapshot,
                noteSnapshot(note),
                ipAddress,
                userAgent
        );
    }

    private void auditAttachment(
            final ClinicalNoteAttachment attachment,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                attachment.getTenantId(),
                SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY_ATTACHMENT,
                attachment.getId().toString(),
                action,
                oldSnapshot,
                attachmentSnapshot(attachment),
                ipAddress,
                userAgent
        );
    }

    private static String noteSnapshot(final ClinicalNote note) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", note.getId());
        fields.put("consultationId", note.getConsultationId());
        fields.put("patientId", note.getPatientId());
        fields.put("authorDoctorId", note.getAuthorDoctorId());
        fields.put("noteType", note.getNoteType());
        fields.put("title", note.getTitle());
        fields.put("content", note.getContent() == null ? null : "[redacted]");
        fields.put("recordedAt", note.getRecordedAt());
        fields.put("deleted", note.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String attachmentSnapshot(final ClinicalNoteAttachment attachment) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", attachment.getId());
        fields.put("clinicalNoteId", attachment.getClinicalNoteId());
        fields.put("fileName", attachment.getFileName());
        fields.put("contentType", attachment.getContentType());
        fields.put("sizeBytes", attachment.getSizeBytes());
        fields.put("attachmentKind", attachment.getAttachmentKind());
        fields.put("storageKey", attachment.getStorageKey());
        fields.put("deleted", attachment.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String sanitizeFileName(final String fileName) {
        final String trimmed = fileName.trim();
        final String base = trimmed.length() > 255 ? trimmed.substring(trimmed.length() - 255) : trimmed;
        return base.replaceAll("[\\\\/]+", "_");
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
