-- Phase 6.1: Appointment domain (scheduling aggregate).
-- Unique appointment number per tenant among live rows via soft-delete-aware generated slot.

CREATE TABLE appointments (
    id                   CHAR(36)      NOT NULL,
    tenant_id            CHAR(36)      NOT NULL,
    appointment_number   VARCHAR(50)   NOT NULL,
    hospital_id          CHAR(36)      NOT NULL,
    patient_id           CHAR(36)      NOT NULL,
    doctor_id            CHAR(36)      NOT NULL,
    department_id        CHAR(36)      NOT NULL,
    appointment_date     DATE          NOT NULL,
    start_time           TIME(0)       NOT NULL,
    end_time             TIME(0)       NOT NULL,
    duration_minutes     INT           NOT NULL,
    status               VARCHAR(30)   NOT NULL,
    appointment_type     VARCHAR(30)   NOT NULL,
    visit_type           VARCHAR(30)   NOT NULL,
    notes                VARCHAR(2000) NULL,
    created_at           TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by           CHAR(36)      NULL,
    updated_by           CHAR(36)      NULL,
    deleted              BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at           TIMESTAMP(6)  NULL,
    deleted_by           CHAR(36)      NULL,
    version              BIGINT        NOT NULL DEFAULT 0,
    active_appointment_number_slot CHAR(1) GENERATED ALWAYS AS (
        CASE WHEN deleted = FALSE THEN 'Y' ELSE NULL END
    ) STORED,
    CONSTRAINT pk_appointments PRIMARY KEY (id),
    CONSTRAINT fk_appointments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_appointments_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_appointments_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT chk_appointments_duration_positive CHECK (duration_minutes >= 1),
    CONSTRAINT chk_appointments_time_order CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Live rows: one appointment number per tenant. Soft-deleted rows free the slot (NULL slot).
CREATE UNIQUE INDEX uk_appointments_tenant_active_number
    ON appointments (tenant_id, appointment_number, active_appointment_number_slot);

CREATE INDEX idx_appointments_tenant_id ON appointments (tenant_id);
CREATE INDEX idx_appointments_tenant_number ON appointments (tenant_id, appointment_number);
CREATE INDEX idx_appointments_tenant_date ON appointments (tenant_id, appointment_date);
CREATE INDEX idx_appointments_tenant_doctor_date ON appointments (tenant_id, doctor_id, appointment_date);
CREATE INDEX idx_appointments_tenant_doctor_date_start
    ON appointments (tenant_id, doctor_id, appointment_date, start_time);
CREATE INDEX idx_appointments_tenant_patient_date ON appointments (tenant_id, patient_id, appointment_date);
CREATE INDEX idx_appointments_tenant_department_date
    ON appointments (tenant_id, department_id, appointment_date);
CREATE INDEX idx_appointments_tenant_hospital_date ON appointments (tenant_id, hospital_id, appointment_date);
CREATE INDEX idx_appointments_tenant_status_date ON appointments (tenant_id, status, appointment_date);
CREATE INDEX idx_appointments_tenant_type ON appointments (tenant_id, appointment_type);
CREATE INDEX idx_appointments_tenant_visit_type ON appointments (tenant_id, visit_type);
CREATE INDEX idx_appointments_deleted ON appointments (deleted);
