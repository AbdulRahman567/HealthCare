package com.healthcare.hms.clinical.notes.dto.response;

import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Clinical note with attachment metadata.
 */
public record ClinicalNoteResponse(
        UUID id,
        UUID consultationId,
        String consultationNumber,
        UUID patientId,
        UUID authorDoctorId,
        String authorDoctorName,
        ClinicalNoteType noteType,
        String title,
        String content,
        Instant recordedAt,
        List<ClinicalNoteAttachmentResponse> attachments,
        Instant createdAt,
        Long version
) {
}
