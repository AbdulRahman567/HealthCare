-- Phase 2.6: Hospital settings (profile, logo, locale, contact, address, working hours).
-- Settings live on the tenant-owned hospitals row (default hospital created at registration).

ALTER TABLE hospitals
    ADD COLUMN description      VARCHAR(1000) NULL AFTER address,
    ADD COLUMN logo_url         VARCHAR(500)  NULL AFTER description,
    ADD COLUMN timezone         VARCHAR(100)  NOT NULL DEFAULT 'UTC' AFTER logo_url,
    ADD COLUMN currency         VARCHAR(3)    NOT NULL DEFAULT 'USD' AFTER timezone,
    ADD COLUMN language         VARCHAR(10)   NOT NULL DEFAULT 'en' AFTER currency,
    ADD COLUMN website          VARCHAR(500)  NULL AFTER language,
    ADD COLUMN secondary_phone  VARCHAR(30)   NULL AFTER website,
    ADD COLUMN city             VARCHAR(100)  NULL AFTER secondary_phone,
    ADD COLUMN state_province   VARCHAR(100)  NULL AFTER city,
    ADD COLUMN country          VARCHAR(100)  NULL AFTER state_province,
    ADD COLUMN postal_code      VARCHAR(20)   NULL AFTER country,
    ADD COLUMN working_hours    JSON          NULL AFTER postal_code;
