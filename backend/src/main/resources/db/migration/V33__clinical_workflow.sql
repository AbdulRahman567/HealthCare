-- Phase 7.1: Clinical Workflow domain — consultations, diagnoses, notes, vitals, follow-ups.
-- No APIs in this phase; persistence foundation only.

CREATE TABLE consultations (
    id                      CHAR(36)      NOT NULL,
    tenant_id               CHAR(36)      NOT NULL,
    consultation_number     VARCHAR(50)   NOT NULL,
    hospital_id             CHAR(36)      NOT NULL,
    patient_id              CHAR(36)      NOT NULL,
    doctor_id               CHAR(36)      NOT NULL,
    department_id           CHAR(36)      NOT NULL,
    appointment_id          CHAR(36)      NULL,
    consultation_date       DATE          NOT NULL,
    started_at              TIMESTAMP(6)  NULL,
    completed_at            TIMESTAMP(6)  NULL,
    status                  VARCHAR(30)   NOT NULL,
    chief_complaint         VARCHAR(2000) NULL,
    advice                  VARCHAR(2000) NULL,
    summary                 VARCHAR(2000) NULL,
    created_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by              CHAR(36)      NULL,
    updated_by              CHAR(36)      NULL,
    deleted                 BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP(6)  NULL,
    deleted_by              CHAR(36)      NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    active_consultation_number_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED,
    active_appointment_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE AND appointment_id IS NOT NULL THEN 'Y' ELSE NULL END
    ) STORED,
    CONSTRAINT pk_consultations PRIMARY KEY (id),
    CONSTRAINT fk_consultations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_consultations_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_consultations_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_consultations_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_consultations_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_consultations_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX uk_consultations_tenant_active_number
    ON consultations (tenant_id, consultation_number, active_consultation_number_slot);

CREATE UNIQUE INDEX uk_consultations_tenant_active_appointment
    ON consultations (tenant_id, appointment_id, active_appointment_slot);

CREATE INDEX idx_consultations_tenant_id ON consultations (tenant_id);
CREATE INDEX idx_consultations_tenant_number ON consultations (tenant_id, consultation_number);
CREATE INDEX idx_consultations_tenant_patient_date ON consultations (tenant_id, patient_id, consultation_date);
CREATE INDEX idx_consultations_tenant_doctor_date ON consultations (tenant_id, doctor_id, consultation_date);
CREATE INDEX idx_consultations_tenant_department_date ON consultations (tenant_id, department_id, consultation_date);
CREATE INDEX idx_consultations_tenant_hospital_date ON consultations (tenant_id, hospital_id, consultation_date);
CREATE INDEX idx_consultations_tenant_status_date ON consultations (tenant_id, status, consultation_date);
CREATE INDEX idx_consultations_tenant_appointment ON consultations (tenant_id, appointment_id);
CREATE INDEX idx_consultations_deleted ON consultations (deleted);

CREATE TABLE consultation_diagnoses (
    id                      CHAR(36)      NOT NULL,
    tenant_id               CHAR(36)      NOT NULL,
    consultation_id         CHAR(36)      NOT NULL,
    patient_id              CHAR(36)      NOT NULL,
    diagnosing_doctor_id    CHAR(36)      NOT NULL,
    diagnosis_name          VARCHAR(200)  NOT NULL,
    icd_code                VARCHAR(32)   NULL,
    diagnosis_type          VARCHAR(20)   NOT NULL,
    diagnosis_status        VARCHAR(20)   NOT NULL,
    severity                VARCHAR(20)   NOT NULL,
    diagnosed_at            TIMESTAMP(6)  NOT NULL,
    sequence_number         INT           NOT NULL,
    clinical_notes          VARCHAR(1000) NULL,
    created_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by              CHAR(36)      NULL,
    updated_by              CHAR(36)      NULL,
    deleted                 BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP(6)  NULL,
    deleted_by              CHAR(36)      NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_consultation_diagnoses PRIMARY KEY (id),
    CONSTRAINT fk_consultation_diagnoses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_consultation_diagnoses_consultation FOREIGN KEY (consultation_id) REFERENCES consultations (id),
    CONSTRAINT fk_consultation_diagnoses_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_consultation_diagnoses_doctor FOREIGN KEY (diagnosing_doctor_id) REFERENCES doctors (id),
    CONSTRAINT chk_consultation_diagnoses_sequence CHECK (sequence_number >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_consultation_diagnoses_tenant_id ON consultation_diagnoses (tenant_id);
CREATE INDEX idx_consultation_diagnoses_consultation ON consultation_diagnoses (tenant_id, consultation_id);
CREATE INDEX idx_consultation_diagnoses_patient ON consultation_diagnoses (tenant_id, patient_id);
CREATE INDEX idx_consultation_diagnoses_doctor ON consultation_diagnoses (tenant_id, diagnosing_doctor_id);
CREATE INDEX idx_consultation_diagnoses_type ON consultation_diagnoses (tenant_id, consultation_id, diagnosis_type);
CREATE INDEX idx_consultation_diagnoses_status ON consultation_diagnoses (tenant_id, patient_id, diagnosis_status);
CREATE INDEX idx_consultation_diagnoses_icd ON consultation_diagnoses (tenant_id, icd_code);
CREATE INDEX idx_consultation_diagnoses_deleted ON consultation_diagnoses (deleted);

CREATE TABLE clinical_notes (
    id                      CHAR(36)      NOT NULL,
    tenant_id               CHAR(36)      NOT NULL,
    consultation_id         CHAR(36)      NOT NULL,
    patient_id              CHAR(36)      NOT NULL,
    author_doctor_id        CHAR(36)      NOT NULL,
    note_type               VARCHAR(20)   NOT NULL,
    content                 VARCHAR(4000) NOT NULL,
    recorded_at             TIMESTAMP(6)  NOT NULL,
    created_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by              CHAR(36)      NULL,
    updated_by              CHAR(36)      NULL,
    deleted                 BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP(6)  NULL,
    deleted_by              CHAR(36)      NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_clinical_notes PRIMARY KEY (id),
    CONSTRAINT fk_clinical_notes_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_clinical_notes_consultation FOREIGN KEY (consultation_id) REFERENCES consultations (id),
    CONSTRAINT fk_clinical_notes_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_clinical_notes_author FOREIGN KEY (author_doctor_id) REFERENCES doctors (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_clinical_notes_tenant_id ON clinical_notes (tenant_id);
CREATE INDEX idx_clinical_notes_consultation ON clinical_notes (tenant_id, consultation_id);
CREATE INDEX idx_clinical_notes_patient ON clinical_notes (tenant_id, patient_id);
CREATE INDEX idx_clinical_notes_author ON clinical_notes (tenant_id, author_doctor_id);
CREATE INDEX idx_clinical_notes_type ON clinical_notes (tenant_id, consultation_id, note_type);
CREATE INDEX idx_clinical_notes_recorded_at ON clinical_notes (tenant_id, consultation_id, recorded_at);
CREATE INDEX idx_clinical_notes_deleted ON clinical_notes (deleted);

CREATE TABLE vital_signs (
    id                          CHAR(36)      NOT NULL,
    tenant_id                   CHAR(36)      NOT NULL,
    consultation_id             CHAR(36)      NOT NULL,
    patient_id                  CHAR(36)      NOT NULL,
    recorded_by_user_id         CHAR(36)      NULL,
    recorded_at                 TIMESTAMP(6)  NOT NULL,
    height_cm                   DECIMAL(5,2)  NULL,
    weight_kg                   DECIMAL(5,2)  NULL,
    temperature_celsius         DECIMAL(4,1)  NULL,
    systolic_bp                 INT           NULL,
    diastolic_bp                INT           NULL,
    pulse_bpm                   INT           NULL,
    respiratory_rate            INT           NULL,
    oxygen_saturation_percent   DECIMAL(4,1)  NULL,
    blood_glucose_mg_dl         DECIMAL(6,2)  NULL,
    bmi                         DECIMAL(4,1)  NULL,
    pain_scale                  INT           NULL,
    notes                       VARCHAR(500)  NULL,
    created_at                  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by                  CHAR(36)      NULL,
    updated_by                  CHAR(36)      NULL,
    deleted                     BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at                  TIMESTAMP(6)  NULL,
    deleted_by                  CHAR(36)      NULL,
    version                     BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_vital_signs PRIMARY KEY (id),
    CONSTRAINT fk_vital_signs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_vital_signs_consultation FOREIGN KEY (consultation_id) REFERENCES consultations (id),
    CONSTRAINT fk_vital_signs_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_vital_signs_recorded_by FOREIGN KEY (recorded_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_vital_signs_systolic_bp CHECK (systolic_bp IS NULL OR (systolic_bp BETWEEN 40 AND 300)),
    CONSTRAINT chk_vital_signs_diastolic_bp CHECK (diastolic_bp IS NULL OR (diastolic_bp BETWEEN 20 AND 200)),
    CONSTRAINT chk_vital_signs_pulse CHECK (pulse_bpm IS NULL OR (pulse_bpm BETWEEN 20 AND 300)),
    CONSTRAINT chk_vital_signs_respiratory CHECK (respiratory_rate IS NULL OR (respiratory_rate BETWEEN 4 AND 80)),
    CONSTRAINT chk_vital_signs_pain_scale CHECK (pain_scale IS NULL OR (pain_scale BETWEEN 0 AND 10))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_vital_signs_tenant_id ON vital_signs (tenant_id);
CREATE INDEX idx_vital_signs_consultation ON vital_signs (tenant_id, consultation_id);
CREATE INDEX idx_vital_signs_patient ON vital_signs (tenant_id, patient_id);
CREATE INDEX idx_vital_signs_recorded_at ON vital_signs (tenant_id, patient_id, recorded_at);
CREATE INDEX idx_vital_signs_deleted ON vital_signs (deleted);

CREATE TABLE consultation_follow_ups (
    id                          CHAR(36)      NOT NULL,
    tenant_id                   CHAR(36)      NOT NULL,
    consultation_id             CHAR(36)      NOT NULL,
    patient_id                  CHAR(36)      NOT NULL,
    doctor_id                   CHAR(36)      NOT NULL,
    scheduled_date              DATE          NOT NULL,
    scheduled_time              TIME(0)       NULL,
    status                      VARCHAR(20)   NOT NULL,
    priority                    VARCHAR(20)   NOT NULL,
    reason                      VARCHAR(500)  NULL,
    instructions                VARCHAR(1000) NULL,
    follow_up_appointment_id    CHAR(36)      NULL,
    created_at                  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by                  CHAR(36)      NULL,
    updated_by                  CHAR(36)      NULL,
    deleted                     BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at                  TIMESTAMP(6)  NULL,
    deleted_by                  CHAR(36)      NULL,
    version                     BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_consultation_follow_ups PRIMARY KEY (id),
    CONSTRAINT fk_consultation_follow_ups_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_consultation_follow_ups_consultation FOREIGN KEY (consultation_id) REFERENCES consultations (id),
    CONSTRAINT fk_consultation_follow_ups_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_consultation_follow_ups_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_consultation_follow_ups_appointment FOREIGN KEY (follow_up_appointment_id) REFERENCES appointments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_consultation_follow_ups_tenant_id ON consultation_follow_ups (tenant_id);
CREATE INDEX idx_consultation_follow_ups_consultation ON consultation_follow_ups (tenant_id, consultation_id);
CREATE INDEX idx_consultation_follow_ups_patient ON consultation_follow_ups (tenant_id, patient_id);
CREATE INDEX idx_consultation_follow_ups_doctor ON consultation_follow_ups (tenant_id, doctor_id);
CREATE INDEX idx_consultation_follow_ups_scheduled ON consultation_follow_ups (tenant_id, scheduled_date);
CREATE INDEX idx_consultation_follow_ups_status ON consultation_follow_ups (tenant_id, status, scheduled_date);
CREATE INDEX idx_consultation_follow_ups_appointment ON consultation_follow_ups (tenant_id, follow_up_appointment_id);
CREATE INDEX idx_consultation_follow_ups_deleted ON consultation_follow_ups (deleted);
