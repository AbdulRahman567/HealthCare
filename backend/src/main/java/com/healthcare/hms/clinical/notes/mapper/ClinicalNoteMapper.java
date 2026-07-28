package com.healthcare.hms.clinical.notes.mapper;

import com.healthcare.hms.clinical.entity.ClinicalNote;
import com.healthcare.hms.clinical.entity.ClinicalNoteAttachment;
import com.healthcare.hms.clinical.notes.dto.request.CreateClinicalNoteRequest;
import com.healthcare.hms.clinical.notes.dto.request.UpdateClinicalNoteRequest;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteAttachmentResponse;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClinicalNoteMapper {

    public void applyCreate(
            final CreateClinicalNoteRequest request,
            final ClinicalNote entity,
            final UUID authorDoctorId,
            final Instant recordedAt
    ) {
        entity.setNoteType(request.noteType());
        entity.setTitle(trimToNull(request.title()));
        entity.setContent(request.content().trim());
        entity.setAuthorDoctorId(authorDoctorId);
        entity.setRecordedAt(recordedAt);
    }

    public void applyUpdate(final UpdateClinicalNoteRequest request, final ClinicalNote entity) {
        if (request.noteType() != null) {
            entity.setNoteType(request.noteType());
        }
        if (request.title() != null) {
            entity.setTitle(trimToNull(request.title()));
        }
        if (request.content() != null) {
            if (request.content().isBlank()) {
                throw new IllegalArgumentException("Note content must not be blank");
            }
            entity.setContent(request.content().trim());
        }
        if (request.authorDoctorId() != null) {
            entity.setAuthorDoctorId(request.authorDoctorId());
        }
        if (request.recordedAt() != null) {
            entity.setRecordedAt(request.recordedAt());
        }
    }

    public ClinicalNoteAttachmentResponse toAttachmentResponse(final ClinicalNoteAttachment entity) {
        return new ClinicalNoteAttachmentResponse(
                entity.getId(),
                entity.getClinicalNoteId(),
                entity.getConsultationId(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getAttachmentKind(),
                entity.getDescription(),
                entity.getUploadedByUserId(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    public ClinicalNoteResponse toResponse(
            final ClinicalNote note,
            final List<ClinicalNoteAttachmentResponse> attachments,
            final String consultationNumber,
            final String authorDoctorName
    ) {
        return new ClinicalNoteResponse(
                note.getId(),
                note.getConsultationId(),
                consultationNumber,
                note.getPatientId(),
                note.getAuthorDoctorId(),
                authorDoctorName,
                note.getNoteType(),
                note.getTitle(),
                note.getContent(),
                note.getRecordedAt(),
                attachments,
                note.getCreatedAt(),
                note.getVersion()
        );
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
