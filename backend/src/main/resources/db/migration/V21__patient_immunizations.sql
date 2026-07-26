-- Phase 5.5: Patient immunization / vaccination records.
-- Structured vaccine, dose, manufacturer, batch, administration + next-due dates, provider.

CREATE TABLE patient_immunizations (
    id                    CHAR(36)      NOT NULL,
    tenant_id             CHAR(36)      NOT NULL,
    patient_id            CHAR(36)      NOT NULL,
    vaccine_name          VARCHAR(200)  NOT NULL,
    vaccine_code          VARCHAR(64)   NULL,
    dose_number           INT           NOT NULL,
    manufacturer          VARCHAR(200)  NULL,
    batch_number          VARCHAR(100)  NULL,
    administration_date   DATE          NOT NULL,
    next_due_date         DATE          NULL,
    healthcare_provider   VARCHAR(200)  NOT NULL,
    route                 VARCHAR(30)   NULL,
    status                VARCHAR(30)   NOT NULL,
    clinical_notes        VARCHAR(1000) NULL,
    recorded_by_user_id   CHAR(36)      NULL,
    created_at            TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by            CHAR(36)      NULL,
    updated_by            CHAR(36)      NULL,
    deleted               BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMP(6)  NULL,
    deleted_by            CHAR(36)      NULL,
    version               BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_patient_immunizations PRIMARY KEY (id),
    CONSTRAINT fk_patient_immunizations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_patient_immunizations_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_patient_immunizations_recorded_by FOREIGN KEY (recorded_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_patient_immunizations_dose CHECK (dose_number >= 1 AND dose_number <= 50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_patient_immunizations_tenant_id ON patient_immunizations (tenant_id);
CREATE INDEX idx_patient_immunizations_patient_id ON patient_immunizations (tenant_id, patient_id);
CREATE INDEX idx_patient_immunizations_vaccine ON patient_immunizations (tenant_id, patient_id, vaccine_name);
CREATE INDEX idx_patient_immunizations_status ON patient_immunizations (tenant_id, patient_id, status);
CREATE INDEX idx_patient_immunizations_admin_date ON patient_immunizations (tenant_id, patient_id, administration_date);
CREATE INDEX idx_patient_immunizations_next_due ON patient_immunizations (tenant_id, patient_id, next_due_date);
CREATE INDEX idx_patient_immunizations_batch ON patient_immunizations (tenant_id, batch_number);
CREATE INDEX idx_patient_immunizations_deleted ON patient_immunizations (deleted);
