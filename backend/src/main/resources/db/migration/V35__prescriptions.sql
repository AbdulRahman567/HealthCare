-- Phase 7.5: Prescription Management — prescriptions + line items (pharmacy-ready).
-- Medicine master deferred; free-text medicine_name with optional medicine_id for future catalog.

CREATE TABLE prescriptions (
    id                      CHAR(36)      NOT NULL,
    tenant_id               CHAR(36)      NOT NULL,
    prescription_number     VARCHAR(50)   NOT NULL,
    consultation_id         CHAR(36)      NOT NULL,
    hospital_id             CHAR(36)      NOT NULL,
    patient_id              CHAR(36)      NOT NULL,
    doctor_id               CHAR(36)      NOT NULL,
    department_id           CHAR(36)      NOT NULL,
    prescription_date       DATE          NOT NULL,
    status                  VARCHAR(30)   NOT NULL,
    notes                   VARCHAR(2000) NULL,
    issued_at               TIMESTAMP(6)  NULL,
    cancelled_at            TIMESTAMP(6)  NULL,
    cancel_reason           VARCHAR(500)  NULL,
    dispensed_at            TIMESTAMP(6)  NULL,
    pharmacy_reference      VARCHAR(100)  NULL,
    created_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by              CHAR(36)      NULL,
    updated_by              CHAR(36)      NULL,
    deleted                 BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP(6)  NULL,
    deleted_by              CHAR(36)      NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    active_prescription_number_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED,
    CONSTRAINT pk_prescriptions PRIMARY KEY (id),
    CONSTRAINT fk_prescriptions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_prescriptions_consultation FOREIGN KEY (consultation_id) REFERENCES consultations (id),
    CONSTRAINT fk_prescriptions_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_prescriptions_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_prescriptions_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_prescriptions_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX uk_prescriptions_tenant_active_number
    ON prescriptions (tenant_id, prescription_number, active_prescription_number_slot);

CREATE INDEX idx_prescriptions_tenant_id ON prescriptions (tenant_id);
CREATE INDEX idx_prescriptions_tenant_number ON prescriptions (tenant_id, prescription_number);
CREATE INDEX idx_prescriptions_tenant_consultation ON prescriptions (tenant_id, consultation_id);
CREATE INDEX idx_prescriptions_tenant_patient_date ON prescriptions (tenant_id, patient_id, prescription_date);
CREATE INDEX idx_prescriptions_tenant_doctor_date ON prescriptions (tenant_id, doctor_id, prescription_date);
CREATE INDEX idx_prescriptions_tenant_status_date ON prescriptions (tenant_id, status, prescription_date);
CREATE INDEX idx_prescriptions_tenant_pharmacy_ref ON prescriptions (tenant_id, pharmacy_reference);
CREATE INDEX idx_prescriptions_deleted ON prescriptions (deleted);

CREATE TABLE prescription_items (
    id                      CHAR(36)      NOT NULL,
    tenant_id               CHAR(36)      NOT NULL,
    prescription_id         CHAR(36)      NOT NULL,
    patient_id              CHAR(36)      NOT NULL,
    medicine_name           VARCHAR(200)  NOT NULL,
    medicine_name_key       VARCHAR(200)  NOT NULL,
    medicine_id             CHAR(36)      NULL,
    medicine_code           VARCHAR(64)   NULL,
    dosage                  VARCHAR(100)  NOT NULL,
    frequency               VARCHAR(100)  NOT NULL,
    route                   VARCHAR(30)   NOT NULL,
    duration                VARCHAR(100)  NOT NULL,
    instructions            VARCHAR(1000) NULL,
    quantity                INT           NOT NULL,
    refills                 INT           NOT NULL DEFAULT 0,
    sequence_number         INT           NOT NULL,
    before_food             BOOLEAN       NOT NULL DEFAULT FALSE,
    after_food              BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by              CHAR(36)      NULL,
    updated_by              CHAR(36)      NULL,
    deleted                 BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP(6)  NULL,
    deleted_by              CHAR(36)      NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    active_medicine_slot    CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED,
    CONSTRAINT pk_prescription_items PRIMARY KEY (id),
    CONSTRAINT fk_prescription_items_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_prescription_items_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions (id),
    CONSTRAINT fk_prescription_items_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT chk_prescription_items_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_prescription_items_refills CHECK (refills >= 0),
    CONSTRAINT chk_prescription_items_sequence CHECK (sequence_number >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX uk_prescription_items_active_medicine
    ON prescription_items (tenant_id, prescription_id, medicine_name_key, active_medicine_slot);

CREATE INDEX idx_prescription_items_tenant_id ON prescription_items (tenant_id);
CREATE INDEX idx_prescription_items_prescription ON prescription_items (tenant_id, prescription_id);
CREATE INDEX idx_prescription_items_patient ON prescription_items (tenant_id, patient_id);
CREATE INDEX idx_prescription_items_medicine_id ON prescription_items (tenant_id, medicine_id);
CREATE INDEX idx_prescription_items_medicine_code ON prescription_items (tenant_id, medicine_code);
CREATE INDEX idx_prescription_items_deleted ON prescription_items (deleted);
