-- Phase 5.3: Structured patient medical history (no free-text blobs, no visits).

CREATE TABLE medical_histories (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    patient_id          CHAR(36)      NOT NULL,
    last_reviewed_at    TIMESTAMP(6)  NULL,
    last_reviewed_by    CHAR(36)      NULL,
    created_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by          CHAR(36)      NULL,
    updated_by          CHAR(36)      NULL,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP(6)  NULL,
    deleted_by          CHAR(36)      NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_medical_histories PRIMARY KEY (id),
    CONSTRAINT uk_medical_histories_tenant_patient UNIQUE (tenant_id, patient_id),
    CONSTRAINT fk_medical_histories_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_medical_histories_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_medical_histories_tenant_id ON medical_histories (tenant_id);
CREATE INDEX idx_medical_histories_patient_id ON medical_histories (patient_id);
CREATE INDEX idx_medical_histories_deleted ON medical_histories (deleted);

CREATE TABLE past_diseases (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    patient_id          CHAR(36)      NOT NULL,
    medical_history_id  CHAR(36)      NOT NULL,
    disease_name        VARCHAR(200)  NOT NULL,
    disease_category    VARCHAR(30)   NOT NULL,
    disease_code        VARCHAR(32)   NULL,
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
    CONSTRAINT pk_past_diseases PRIMARY KEY (id),
    CONSTRAINT fk_past_diseases_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_past_diseases_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_past_diseases_medical_history FOREIGN KEY (medical_history_id) REFERENCES medical_histories (id),
    CONSTRAINT fk_past_diseases_recorded_by FOREIGN KEY (recorded_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_past_diseases_tenant_id ON past_diseases (tenant_id);
CREATE INDEX idx_past_diseases_patient_id ON past_diseases (tenant_id, patient_id);
CREATE INDEX idx_past_diseases_medical_history_id ON past_diseases (medical_history_id);
CREATE INDEX idx_past_diseases_diagnosis_date ON past_diseases (tenant_id, patient_id, diagnosis_date);
CREATE INDEX idx_past_diseases_status ON past_diseases (tenant_id, patient_id, condition_status);
CREATE INDEX idx_past_diseases_deleted ON past_diseases (deleted);

CREATE TABLE surgery_histories (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    patient_id          CHAR(36)      NOT NULL,
    medical_history_id  CHAR(36)      NOT NULL,
    procedure_name      VARCHAR(200)  NOT NULL,
    procedure_category  VARCHAR(30)   NOT NULL,
    procedure_code      VARCHAR(32)   NULL,
    performing_facility VARCHAR(200)  NULL,
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
    CONSTRAINT pk_surgery_histories PRIMARY KEY (id),
    CONSTRAINT fk_surgery_histories_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_surgery_histories_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_surgery_histories_medical_history FOREIGN KEY (medical_history_id) REFERENCES medical_histories (id),
    CONSTRAINT fk_surgery_histories_recorded_by FOREIGN KEY (recorded_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_surgery_histories_tenant_id ON surgery_histories (tenant_id);
CREATE INDEX idx_surgery_histories_patient_id ON surgery_histories (tenant_id, patient_id);
CREATE INDEX idx_surgery_histories_medical_history_id ON surgery_histories (medical_history_id);
CREATE INDEX idx_surgery_histories_diagnosis_date ON surgery_histories (tenant_id, patient_id, diagnosis_date);
CREATE INDEX idx_surgery_histories_status ON surgery_histories (tenant_id, patient_id, condition_status);
CREATE INDEX idx_surgery_histories_deleted ON surgery_histories (deleted);

CREATE TABLE chronic_conditions (
    id                  CHAR(36)      NOT NULL,
    tenant_id           CHAR(36)      NOT NULL,
    patient_id          CHAR(36)      NOT NULL,
    medical_history_id  CHAR(36)      NOT NULL,
    condition_name      VARCHAR(200)  NOT NULL,
    disease_category    VARCHAR(30)   NOT NULL,
    condition_code      VARCHAR(32)   NULL,
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
    CONSTRAINT pk_chronic_conditions PRIMARY KEY (id),
    CONSTRAINT fk_chronic_conditions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_chronic_conditions_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_chronic_conditions_medical_history FOREIGN KEY (medical_history_id) REFERENCES medical_histories (id),
    CONSTRAINT fk_chronic_conditions_recorded_by FOREIGN KEY (recorded_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_chronic_conditions_tenant_id ON chronic_conditions (tenant_id);
CREATE INDEX idx_chronic_conditions_patient_id ON chronic_conditions (tenant_id, patient_id);
CREATE INDEX idx_chronic_conditions_medical_history_id ON chronic_conditions (medical_history_id);
CREATE INDEX idx_chronic_conditions_diagnosis_date ON chronic_conditions (tenant_id, patient_id, diagnosis_date);
CREATE INDEX idx_chronic_conditions_status ON chronic_conditions (tenant_id, patient_id, condition_status);
CREATE INDEX idx_chronic_conditions_deleted ON chronic_conditions (deleted);
