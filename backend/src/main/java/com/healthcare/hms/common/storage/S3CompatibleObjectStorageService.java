package com.healthcare.hms.common.storage;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * AWS S3 / MinIO (S3-compatible) object storage implementation.
 */
public class S3CompatibleObjectStorageService implements ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(S3CompatibleObjectStorageService.class);

    private final S3Client s3Client;
    private final String bucket;

    public S3CompatibleObjectStorageService(final StorageProperties.S3 properties) {
        this.bucket = properties.getBucket();
        final S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .build());

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint().trim()));
        }

        this.s3Client = builder.build();
        log.info("S3-compatible object storage initialized bucket={} endpoint={}",
                bucket, properties.getEndpoint() == null || properties.getEndpoint().isBlank() ? "aws-default" : properties.getEndpoint());
    }

    @Override
    public String put(final ObjectStoreRequest request) {
        final String key = LocalObjectStorageService.buildKey(
                request.tenantId(), request.category(), request.originalFileName());
        try {
            final PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(request.contentType())
                    .contentLength(request.contentLength())
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromInputStream(request.content(), request.contentLength()));
            return key;
        } catch (final S3Exception ex) {
            throw new ObjectStorageException("Failed to store object key=" + key, ex);
        }
    }

    @Override
    public InputStream get(final String storageKey) {
        try {
            return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(storageKey).build());
        } catch (final NoSuchKeyException ex) {
            throw new ObjectStorageException("Object not found: " + storageKey, ex);
        } catch (final S3Exception ex) {
            throw new ObjectStorageException("Failed to read object key=" + storageKey, ex);
        }
    }

    @Override
    public void delete(final String storageKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(storageKey).build());
        } catch (final S3Exception ex) {
            throw new ObjectStorageException("Failed to delete object key=" + storageKey, ex);
        }
    }

    @Override
    public boolean exists(final String storageKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(storageKey).build());
            return true;
        } catch (final NoSuchKeyException ex) {
            return false;
        } catch (final S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw new ObjectStorageException("Failed to probe object key=" + storageKey, ex);
        }
    }

    @Override
    public Map<String, String> metadata(final String storageKey) {
        try {
            final var head = s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(storageKey).build());
            final Map<String, String> meta = new HashMap<>();
            meta.put("size", String.valueOf(head.contentLength()));
            if (head.contentType() != null) {
                meta.put("contentType", head.contentType());
            }
            return meta;
        } catch (final NoSuchKeyException ex) {
            return Map.of();
        } catch (final S3Exception ex) {
            throw new ObjectStorageException("Failed to read metadata for key=" + storageKey, ex);
        }
    }
}
