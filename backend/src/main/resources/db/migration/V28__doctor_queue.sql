-- Phase 6.4: Daily doctor queue management (one queue per doctor per day).

CREATE TABLE doctor_day_queues (
    id                 CHAR(36)     NOT NULL,
    tenant_id          CHAR(36)     NOT NULL,
    doctor_id          CHAR(36)     NOT NULL,
    hospital_id        CHAR(36)     NOT NULL,
    queue_date         DATE         NOT NULL,
    last_queue_number  INT          NOT NULL DEFAULT 0,
    created_at         TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by         CHAR(36)     NULL,
    updated_by         CHAR(36)     NULL,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at         TIMESTAMP(6) NULL,
    deleted_by         CHAR(36)     NULL,
    version            BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_doctor_day_queues PRIMARY KEY (id),
    CONSTRAINT uk_doctor_day_queues_tenant_doctor_date UNIQUE (tenant_id, doctor_id, queue_date),
    CONSTRAINT fk_doctor_day_queues_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_doctor_day_queues_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_doctor_day_queues_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT chk_doctor_day_queues_last_number CHECK (last_queue_number >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_doctor_day_queues_tenant_id ON doctor_day_queues (tenant_id);
CREATE INDEX idx_doctor_day_queues_doctor_date ON doctor_day_queues (tenant_id, doctor_id, queue_date);
CREATE INDEX idx_doctor_day_queues_hospital_date ON doctor_day_queues (tenant_id, hospital_id, queue_date);
CREATE INDEX idx_doctor_day_queues_deleted ON doctor_day_queues (deleted);

CREATE TABLE queue_entries (
    id                 CHAR(36)     NOT NULL,
    tenant_id          CHAR(36)     NOT NULL,
    queue_id           CHAR(36)     NOT NULL,
    appointment_id     CHAR(36)     NOT NULL,
    patient_id         CHAR(36)     NOT NULL,
    doctor_id          CHAR(36)     NOT NULL,
    hospital_id        CHAR(36)     NOT NULL,
    queue_number       INT          NOT NULL,
    status             VARCHAR(30)  NOT NULL,
    checked_in_at      TIMESTAMP(6) NOT NULL,
    status_changed_at  TIMESTAMP(6) NOT NULL,
    notes              VARCHAR(500) NULL,
    created_at         TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by         CHAR(36)     NULL,
    updated_by         CHAR(36)     NULL,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at         TIMESTAMP(6) NULL,
    deleted_by         CHAR(36)     NULL,
    version            BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_queue_entries PRIMARY KEY (id),
    CONSTRAINT uk_queue_entries_queue_number UNIQUE (queue_id, queue_number),
    CONSTRAINT uk_queue_entries_appointment UNIQUE (appointment_id),
    CONSTRAINT fk_queue_entries_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_queue_entries_queue FOREIGN KEY (queue_id) REFERENCES doctor_day_queues (id),
    CONSTRAINT fk_queue_entries_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_queue_entries_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_queue_entries_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_queue_entries_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT chk_queue_entries_number_positive CHECK (queue_number >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_queue_entries_tenant_id ON queue_entries (tenant_id);
CREATE INDEX idx_queue_entries_queue_status ON queue_entries (tenant_id, queue_id, status);
CREATE INDEX idx_queue_entries_queue_number ON queue_entries (tenant_id, queue_id, queue_number);
CREATE INDEX idx_queue_entries_doctor ON queue_entries (tenant_id, doctor_id);
CREATE INDEX idx_queue_entries_patient ON queue_entries (tenant_id, patient_id);
CREATE INDEX idx_queue_entries_appointment ON queue_entries (appointment_id);
CREATE INDEX idx_queue_entries_deleted ON queue_entries (deleted);
