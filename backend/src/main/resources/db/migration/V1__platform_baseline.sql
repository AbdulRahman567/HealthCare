CREATE TABLE IF NOT EXISTS platform_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    platform_key VARCHAR(100) NOT NULL UNIQUE,
    platform_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO platform_metadata (platform_key, platform_value)
VALUES ('schema_version', '1.0')
ON DUPLICATE KEY UPDATE platform_value = VALUES(platform_value);
