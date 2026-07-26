-- Phase 5.4: Safety-critical patient allergy management.
-- Structured type/severity/reaction; clinical flags; banner + critical alert indexes.

CREATE TABLE patient_allergies (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    patient_id          CHAR(36)      NOT NULL,
    allergen_name       VARCHAR(200)  NOT NULL,
    allergen_code       VARCHAR(64)   NULL,
    allergy_type        VARCHAR(30)   NOT NULL,
    severity            VARCHAR(30)   NOT NULL,
    reaction            VARCHAR(30)   NOT NULL,
    status              VARCHAR(30)   NOT NULL,
    onset_date          DATE          NULL,
    clinical_notes      VARCHAR(1000) NULL,
    verified            BOOLEAN       NOT NULL DEFAULT FALSE,
    patient_reported    BOOLEAN       NOT NULL DEFAULT TRUE,
    critical_alert      BOOLEAN       NOT NULL DEFAULT FALSE,
    show_on_banner      BOOLEAN       NOT NULL DEFAULT TRUE,
    recorded_by_user_id CHAR(36)      NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_patient_allergies PRIMARY KEY (id),
    CONSTRAINT fk_patient_allergies_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_patient_allergies_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_patient_allergies_recorded_by FOREIGN KEY (recorded_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_patient_allergies_tenant_id ON patient_allergies (tenant_id);
CREATE INDEX idx_patient_allergies_patient_id ON patient_allergies (tenant_id, patient_id);
CREATE INDEX idx_patient_allergies_type ON patient_allergies (tenant_id, patient_id, allergy_type);
CREATE INDEX idx_patient_allergies_severity ON patient_allergies (tenant_id, patient_id, severity);
CREATE INDEX idx_patient_allergies_status ON patient_allergies (tenant_id, patient_id, status);
CREATE INDEX idx_patient_allergies_critical ON patient_allergies (tenant_id, patient_id, critical_alert);
CREATE INDEX idx_patient_allergies_banner ON patient_allergies (tenant_id, patient_id, show_on_banner);
CREATE INDEX idx_patient_allergies_deleted ON patient_allergies (deleted);
