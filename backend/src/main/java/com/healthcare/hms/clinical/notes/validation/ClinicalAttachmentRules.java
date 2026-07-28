package com.healthcare.hms.clinical.notes.validation;

import com.healthcare.hms.clinical.enums.ClinicalAttachmentKind;
import java.util.Locale;
import java.util.Set;

/**
 * Upload validation rules for clinical note attachments (ENGINEERING_RULES §13).
 * MIME + extension allow-list, with magic-byte sniffing (Phase 7.9).
 */
public final class ClinicalAttachmentRules {

    public static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    public static final long MAX_PDF_BYTES = 25L * 1024 * 1024;
    public static final int MAX_ATTACHMENTS_PER_NOTE = 20;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final Set<String> ALLOWED_PDF_TYPES = Set.of("application/pdf");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".pdf"
    );

    private ClinicalAttachmentRules() {
    }

    public static ClinicalAttachmentKind resolveKind(final String contentType, final String fileName) {
        final String type = normalizeContentType(contentType);
        if (ALLOWED_PDF_TYPES.contains(type) || fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return ClinicalAttachmentKind.PDF;
        }
        if (ALLOWED_IMAGE_TYPES.contains(type) || hasImageExtension(fileName)) {
            return ClinicalAttachmentKind.IMAGE;
        }
        return ClinicalAttachmentKind.OTHER;
    }

    public static boolean isAllowed(final String contentType, final String fileName) {
        final String type = normalizeContentType(contentType);
        final String lowerName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        final boolean extensionOk = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        final boolean typeOk = ALLOWED_IMAGE_TYPES.contains(type) || ALLOWED_PDF_TYPES.contains(type);
        return extensionOk && typeOk;
    }

    /**
     * Validates declared content type / extension against file magic bytes to reduce spoofed uploads.
     */
    public static boolean matchesMagicBytes(final ClinicalAttachmentKind kind, final byte[] header) {
        if (header == null || header.length < 4) {
            return false;
        }
        return switch (kind) {
            case PDF -> header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46; // %PDF
            case IMAGE -> isJpeg(header) || isPng(header) || isWebp(header);
            case OTHER -> false;
        };
    }

    public static long maxBytesFor(final ClinicalAttachmentKind kind) {
        return kind == ClinicalAttachmentKind.PDF ? MAX_PDF_BYTES : MAX_IMAGE_BYTES;
    }

    public static String normalizeContentType(final String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        final String trimmed = contentType.trim().toLowerCase(Locale.ROOT);
        final int semi = trimmed.indexOf(';');
        return semi >= 0 ? trimmed.substring(0, semi).trim() : trimmed;
    }

    private static boolean hasImageExtension(final String fileName) {
        if (fileName == null) {
            return false;
        }
        final String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".webp");
    }

    private static boolean isJpeg(final byte[] header) {
        return header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(final byte[] header) {
        return header.length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;
    }

    private static boolean isWebp(final byte[] header) {
        return header.length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P';
    }
}
