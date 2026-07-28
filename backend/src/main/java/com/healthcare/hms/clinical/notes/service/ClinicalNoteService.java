package com.healthcare.hms.clinical.notes.service;

import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import com.healthcare.hms.clinical.notes.dto.request.CreateClinicalNoteRequest;
import com.healthcare.hms.clinical.notes.dto.request.UpdateClinicalNoteRequest;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteAttachmentResponse;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteResponse;
import com.healthcare.hms.common.api.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Clinical notes and attachments (Phase 7.6).
 */
public interface ClinicalNoteService {

    ClinicalNoteResponse create(
            UUID consultationId,
            CreateClinicalNoteRequest request,
            String ipAddress,
            String userAgent
    );

    ClinicalNoteResponse getById(UUID consultationId, UUID noteId, String ipAddress, String userAgent);

    List<ClinicalNoteResponse> listByConsultation(UUID consultationId, ClinicalNoteType noteType);

    ClinicalNoteResponse update(
            UUID consultationId,
            UUID noteId,
            UpdateClinicalNoteRequest request,
            String ipAddress,
            String userAgent
    );

    void delete(UUID consultationId, UUID noteId, String ipAddress, String userAgent);

    PageResponse<ClinicalNoteResponse> patientHistory(
            UUID patientId,
            ClinicalNoteType noteType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );

    ClinicalNoteAttachmentResponse addAttachment(
            UUID consultationId,
            UUID noteId,
            MultipartFile file,
            String description,
            String ipAddress,
            String userAgent
    );

    List<ClinicalNoteAttachmentResponse> listAttachments(UUID consultationId, UUID noteId);

    /**
     * Streams attachment bytes and records a VIEW audit.
     */
    ClinicalNoteAttachmentDownload downloadAttachment(
            UUID consultationId,
            UUID noteId,
            UUID attachmentId,
            String ipAddress,
            String userAgent
    );

    ClinicalNoteAttachmentResponse getAttachmentMetadata(
            UUID consultationId,
            UUID noteId,
            UUID attachmentId,
            String ipAddress,
            String userAgent
    );

    void deleteAttachment(
            UUID consultationId,
            UUID noteId,
            UUID attachmentId,
            String ipAddress,
            String userAgent
    );
}
