package com.healthcare.hms.common.storage;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * Request to store a binary object under a tenant-scoped key.
 */
public record ObjectStoreRequest(
        UUID tenantId,
        String category,
        String originalFileName,
        String contentType,
        long contentLength,
        InputStream content
) {
    public ObjectStoreRequest {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(originalFileName, "originalFileName");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(content, "content");
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength must not be negative");
        }
    }
}
