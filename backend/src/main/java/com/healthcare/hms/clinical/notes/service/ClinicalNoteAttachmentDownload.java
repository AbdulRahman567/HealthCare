package com.healthcare.hms.clinical.notes.service;

import org.springframework.core.io.Resource;

/**
 * Attachment binary download payload with response headers (Phase 7.9).
 */
public record ClinicalNoteAttachmentDownload(
        Resource resource,
        String fileName,
        String contentType,
        long sizeBytes
) {
}
