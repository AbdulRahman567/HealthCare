package com.healthcare.hms.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Object storage configuration ({@code hms.storage.*}).
 *
 * <p>{@code type=local} uses the local filesystem (default for development).
 * {@code type=s3} uses AWS S3 or any S3-compatible endpoint (MinIO).
 */
@ConfigurationProperties(prefix = "hms.storage")
public class StorageProperties {

    /**
     * {@code local} or {@code s3}.
     */
    private String type = "local";

    private final Local local = new Local();
    private final S3 s3 = new S3();

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Local getLocal() {
        return local;
    }

    public S3 getS3() {
        return s3;
    }

    public static class Local {
        /** Root directory for locally stored objects. */
        private String basePath = "./data/object-storage";

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(final String basePath) {
            this.basePath = basePath;
        }
    }

    public static class S3 {
        private String endpoint = "";
        private String region = "us-east-1";
        private String bucket = "hms-clinical";
        private String accessKey = "";
        private String secretKey = "";
        /** Required for MinIO and some S3-compatible stores. */
        private boolean pathStyleAccess = true;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(final String endpoint) {
            this.endpoint = endpoint;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(final String region) {
            this.region = region;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(final String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(final String secretKey) {
            this.secretKey = secretKey;
        }

        public boolean isPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(final boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }
    }
}
