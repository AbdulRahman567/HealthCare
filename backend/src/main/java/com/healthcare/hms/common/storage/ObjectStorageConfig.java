package com.healthcare.hms.common.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires {@link ObjectStorageService} for local filesystem or S3/MinIO.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class ObjectStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "hms.storage.type", havingValue = "local", matchIfMissing = true)
    public ObjectStorageService localObjectStorageService(final StorageProperties properties) {
        return new LocalObjectStorageService(properties.getLocal().getBasePath());
    }

    @Bean
    @ConditionalOnProperty(name = "hms.storage.type", havingValue = "s3")
    public ObjectStorageService s3ObjectStorageService(final StorageProperties properties) {
        final StorageProperties.S3 s3 = properties.getS3();
        if (s3.getAccessKey() == null || s3.getAccessKey().isBlank()
                || s3.getSecretKey() == null || s3.getSecretKey().isBlank()) {
            throw new IllegalStateException(
                    "hms.storage.type=s3 requires HMS_STORAGE_S3_ACCESS_KEY and HMS_STORAGE_S3_SECRET_KEY"
            );
        }
        if (s3.getBucket() == null || s3.getBucket().isBlank()) {
            throw new IllegalStateException("hms.storage.type=s3 requires HMS_STORAGE_S3_BUCKET");
        }
        return new S3CompatibleObjectStorageService(s3);
    }
}
