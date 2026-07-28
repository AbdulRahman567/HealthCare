package com.healthcare.hms.common.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local filesystem object storage for development (and fallback when S3 is unavailable).
 */
public class LocalObjectStorageService implements ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalObjectStorageService.class);

    private final Path basePath;

    public LocalObjectStorageService(final String basePath) {
        this.basePath = Path.of(basePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.basePath);
        } catch (final IOException ex) {
            throw new ObjectStorageException("Failed to initialize local object storage at " + this.basePath, ex);
        }
        log.info("Local object storage initialized at {}", this.basePath);
    }

    @Override
    public String put(final ObjectStoreRequest request) {
        final String key = buildKey(request.tenantId(), request.category(), request.originalFileName());
        final Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(request.content(), target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (final IOException ex) {
            throw new ObjectStorageException("Failed to store object key=" + key, ex);
        }
    }

    @Override
    public InputStream get(final String storageKey) {
        final Path path = resolve(storageKey);
        try {
            if (!Files.exists(path)) {
                throw new ObjectStorageException("Object not found: " + storageKey);
            }
            return Files.newInputStream(path);
        } catch (final IOException ex) {
            throw new ObjectStorageException("Failed to read object key=" + storageKey, ex);
        }
    }

    @Override
    public void delete(final String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (final IOException ex) {
            throw new ObjectStorageException("Failed to delete object key=" + storageKey, ex);
        }
    }

    @Override
    public boolean exists(final String storageKey) {
        return Files.exists(resolve(storageKey));
    }

    @Override
    public Map<String, String> metadata(final String storageKey) {
        final Path path = resolve(storageKey);
        try {
            if (!Files.exists(path)) {
                return Map.of();
            }
            return Map.of(
                    "size", String.valueOf(Files.size(path)),
                    "contentType", Files.probeContentType(path) == null ? "application/octet-stream" : Files.probeContentType(path)
            );
        } catch (final IOException ex) {
            throw new ObjectStorageException("Failed to read metadata for key=" + storageKey, ex);
        }
    }

    private Path resolve(final String storageKey) {
        final Path resolved = basePath.resolve(storageKey).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new ObjectStorageException("Invalid storage key path traversal attempt");
        }
        return resolved;
    }

    static String buildKey(final UUID tenantId, final String category, final String originalFileName) {
        final String safeCategory = sanitizeSegment(category);
        final String extension = extractExtension(originalFileName);
        final String fileId = UUID.randomUUID().toString().replace("-", "");
        return tenantId + "/" + safeCategory + "/" + fileId + extension;
    }

    private static String sanitizeSegment(final String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
    }

    private static String extractExtension(final String fileName) {
        final int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        final String ext = fileName.substring(idx).toLowerCase(Locale.ROOT);
        if (!ext.matches("\\.[a-z0-9]{1,10}")) {
            return "";
        }
        return ext;
    }
}
