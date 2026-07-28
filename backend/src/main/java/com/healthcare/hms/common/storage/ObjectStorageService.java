package com.healthcare.hms.common.storage;

import java.io.InputStream;
import java.util.Map;

/**
 * S3-compatible object storage port (AWS S3, MinIO, or local filesystem).
 */
public interface ObjectStorageService {

    /**
     * Stores an object and returns its storage key.
     *
     * @param request object metadata and content stream
     * @return generated storage key (tenant-scoped path)
     */
    String put(ObjectStoreRequest request);

    /**
     * Opens a stream to read an object. Caller must close the stream.
     */
    InputStream get(String storageKey);

    /**
     * Deletes an object if it exists (idempotent).
     */
    void delete(String storageKey);

    /**
     * Returns true when the object exists.
     */
    boolean exists(String storageKey);

    /**
     * Optional metadata lookup (size, content type). Empty map when unavailable.
     */
    Map<String, String> metadata(String storageKey);
}
