/**
 * Object storage abstraction for PHI-safe file persistence (AWS S3 / MinIO / local).
 *
 * <p>Only object keys and metadata are stored in the database — binary payloads live
 * in the configured object store. See {@link com.healthcare.hms.common.storage.ObjectStorageService}.
 */
package com.healthcare.hms.common.storage;
