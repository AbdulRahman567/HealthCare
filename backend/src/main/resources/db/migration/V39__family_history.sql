-- Phase 8: Family history (longitudinal medical records gap).

CREATE TABLE family_histories (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    patient_id          CHAR(36)      NOT NULL,
    medical_history_id  CHAR(36)      NOT NULL,
    disease_name        VARCHAR(200)  NOT NULL,
    disease_category    VARCHAR(30)   NOT NULL,
    disease_code        VARCHAR(32)   NULL,
    family_relation     VARCHAR(30)   NOT NULL,
    diagnosis_date      DATE          NOT NULL,
    recovery_date       DATE          NULL,
    severity            VARCHAR(20)   NOT NULL,
    condition_status    VARCHAR(20)   NOT NULL,
    clinical_notes      VARCHAR(1000) NULL,
    recorded_by_user_id CHAR(36)      NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_family_histories PRIMARY KEY (id),
    CONSTRAINT fk_family_histories_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_family_histories_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_family_histories_medical_history FOREIGN KEY (medical_history_id) REFERENCES medical_histories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_family_histories_tenant_id ON family_histories (tenant_id);
CREATE INDEX idx_family_histories_patient_id ON family_histories (tenant_id, patient_id);
CREATE INDEX idx_family_histories_medical_history_id ON family_histories (medical_history_id);
CREATE INDEX idx_family_histories_diagnosis_date ON family_histories (tenant_id, patient_id, diagnosis_date);
CREATE INDEX idx_family_histories_relation ON family_histories (tenant_id, patient_id, family_relation);
CREATE INDEX idx_family_histories_deleted ON family_histories (deleted);
