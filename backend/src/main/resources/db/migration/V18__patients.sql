-- Phase 5.1: Patient domain (demographics + identifiers).
-- Unique MRN per tenant among live rows via soft-delete-aware generated slot.

CREATE TABLE patients (
    id                          CHAR(36)      NOT NULL,
    tenant_id                   CHAR(36)      NOT NULL,
    mrn                         VARCHAR(50)   NOT NULL,
    first_name                  VARCHAR(100)  NOT NULL,
    last_name                   VARCHAR(100)  NOT NULL,
    date_of_birth               DATE          NOT NULL,
    gender                      VARCHAR(20)   NOT NULL,
    blood_group                 VARCHAR(20)   NOT NULL,
    national_id                 VARCHAR(50)   NULL,
    phone                       VARCHAR(30)   NULL,
    email                       VARCHAR(255)  NULL,
    address                     VARCHAR(500)  NULL,
    emergency_contact_name      VARCHAR(150)  NULL,
    emergency_contact_phone     VARCHAR(30)   NULL,
    emergency_contact_relation  VARCHAR(50)   NULL,
    marital_status              VARCHAR(20)   NULL,
    status                      VARCHAR(20)   NOT NULL,
    created_at                  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by                  CHAR(36)      NULL,
    updated_by                  CHAR(36)      NULL,
    deleted                     BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at                  TIMESTAMP(6)  NULL,
    deleted_by                  CHAR(36)      NULL,
    version                     BIGINT        NOT NULL DEFAULT 0,
    active_mrn_slot             CHAR(1)       GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED,
    CONSTRAINT pk_patients PRIMARY KEY (id),
    CONSTRAINT fk_patients_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Live rows: one MRN per tenant. Soft-deleted rows free the slot (NULL slot).
CREATE UNIQUE INDEX uk_patients_tenant_active_mrn
    ON patients (tenant_id, mrn, active_mrn_slot);

CREATE INDEX idx_patients_tenant_id ON patients (tenant_id);
CREATE INDEX idx_patients_tenant_mrn ON patients (tenant_id, mrn);
CREATE INDEX idx_patients_tenant_status ON patients (tenant_id, status);
CREATE INDEX idx_patients_tenant_last_first ON patients (tenant_id, last_name, first_name);
CREATE INDEX idx_patients_tenant_phone ON patients (tenant_id, phone);
CREATE INDEX idx_patients_tenant_email ON patients (tenant_id, email);
CREATE INDEX idx_patients_tenant_national_id ON patients (tenant_id, national_id);
CREATE INDEX idx_patients_tenant_dob ON patients (tenant_id, date_of_birth);
CREATE INDEX idx_patients_deleted ON patients (deleted);
