package com.healthcare.hms.clinical.notes.dto.response;

import com.healthcare.hms.clinical.enums.ClinicalAttachmentKind;
import java.time.Instant;
import java.util.UUID;

/**
 * Attachment metadata (binary content served via download endpoint).
 */
public record ClinicalNoteAttachmentResponse(
        UUID id,
        UUID clinicalNoteId,
        UUID consultationId,
        String fileName,
        String contentType,
        Long sizeBytes,
        ClinicalAttachmentKind attachmentKind,
        String description,
        UUID uploadedByUserId,
        Instant createdAt,
        Long version
) {
}
