-- Phase 6.2: Doctor availability — recurring schedules, working windows, breaks, unavailability.

CREATE TABLE doctor_schedules (
    id                        CHAR(36)      NOT NULL,
    tenant_id                 CHAR(36)      NOT NULL,
    doctor_id                 CHAR(36)      NOT NULL,
    hospital_id               CHAR(36)      NOT NULL,
    name                      VARCHAR(150)  NULL,
    effective_from            DATE          NOT NULL,
    effective_to              DATE          NULL,
    max_appointments_per_day  INT           NOT NULL,
    recurrence_type           VARCHAR(30)   NOT NULL,
    status                    VARCHAR(30)   NOT NULL,
    notes                     VARCHAR(1000) NULL,
    created_at                TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by                CHAR(36)      NULL,
    updated_by                CHAR(36)      NULL,
    deleted                   BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at                TIMESTAMP(6)  NULL,
    deleted_by                CHAR(36)      NULL,
    version                   BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_doctor_schedules PRIMARY KEY (id),
    CONSTRAINT fk_doctor_schedules_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_doctor_schedules_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_doctor_schedules_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT chk_doctor_schedules_max_appts CHECK (max_appointments_per_day >= 1 AND max_appointments_per_day <= 500),
    CONSTRAINT chk_doctor_schedules_effective_range CHECK (effective_to IS NULL OR effective_to >= effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_doctor_schedules_tenant_id ON doctor_schedules (tenant_id);
CREATE INDEX idx_doctor_schedules_doctor ON doctor_schedules (tenant_id, doctor_id);
CREATE INDEX idx_doctor_schedules_doctor_status ON doctor_schedules (tenant_id, doctor_id, status);
CREATE INDEX idx_doctor_schedules_effective ON doctor_schedules (tenant_id, doctor_id, effective_from, effective_to);
CREATE INDEX idx_doctor_schedules_hospital ON doctor_schedules (tenant_id, hospital_id);
CREATE INDEX idx_doctor_schedules_deleted ON doctor_schedules (deleted);

CREATE TABLE doctor_schedule_windows (
    id            CHAR(36)     NOT NULL,
    tenant_id     CHAR(36)     NOT NULL,
    schedule_id   CHAR(36)     NOT NULL,
    doctor_id     CHAR(36)     NOT NULL,
    day_of_week   VARCHAR(15)  NOT NULL,
    start_time    TIME(0)      NOT NULL,
    end_time      TIME(0)      NOT NULL,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by    CHAR(36)     NULL,
    updated_by    CHAR(36)     NULL,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP(6) NULL,
    deleted_by    CHAR(36)     NULL,
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_doctor_schedule_windows PRIMARY KEY (id),
    CONSTRAINT fk_doctor_schedule_windows_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_doctor_schedule_windows_schedule FOREIGN KEY (schedule_id) REFERENCES doctor_schedules (id),
    CONSTRAINT fk_doctor_schedule_windows_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT chk_doctor_schedule_windows_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_doctor_schedule_windows_tenant_id ON doctor_schedule_windows (tenant_id);
CREATE INDEX idx_doctor_schedule_windows_schedule ON doctor_schedule_windows (tenant_id, schedule_id);
CREATE INDEX idx_doctor_schedule_windows_day ON doctor_schedule_windows (tenant_id, schedule_id, day_of_week);
CREATE INDEX idx_doctor_schedule_windows_deleted ON doctor_schedule_windows (deleted);

CREATE TABLE doctor_schedule_breaks (
    id            CHAR(36)     NOT NULL,
    tenant_id     CHAR(36)     NOT NULL,
    schedule_id   CHAR(36)     NOT NULL,
    doctor_id     CHAR(36)     NOT NULL,
    day_of_week   VARCHAR(15)  NOT NULL,
    start_time    TIME(0)      NOT NULL,
    end_time      TIME(0)      NOT NULL,
    label         VARCHAR(100) NULL,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by    CHAR(36)     NULL,
    updated_by    CHAR(36)     NULL,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP(6) NULL,
    deleted_by    CHAR(36)     NULL,
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_doctor_schedule_breaks PRIMARY KEY (id),
    CONSTRAINT fk_doctor_schedule_breaks_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_doctor_schedule_breaks_schedule FOREIGN KEY (schedule_id) REFERENCES doctor_schedules (id),
    CONSTRAINT fk_doctor_schedule_breaks_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT chk_doctor_schedule_breaks_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_doctor_schedule_breaks_tenant_id ON doctor_schedule_breaks (tenant_id);
CREATE INDEX idx_doctor_schedule_breaks_schedule ON doctor_schedule_breaks (tenant_id, schedule_id);
CREATE INDEX idx_doctor_schedule_breaks_day ON doctor_schedule_breaks (tenant_id, schedule_id, day_of_week);
CREATE INDEX idx_doctor_schedule_breaks_deleted ON doctor_schedule_breaks (deleted);

CREATE TABLE doctor_unavailabilities (
    id                    CHAR(36)     NOT NULL,
    tenant_id             CHAR(36)     NOT NULL,
    doctor_id             CHAR(36)     NOT NULL,
    hospital_id           CHAR(36)     NOT NULL,
    unavailability_type   VARCHAR(30)  NOT NULL,
    start_date            DATE         NOT NULL,
    end_date              DATE         NOT NULL,
    all_day               BOOLEAN      NOT NULL DEFAULT TRUE,
    start_time            TIME(0)      NULL,
    end_time              TIME(0)      NULL,
    reason                VARCHAR(500) NULL,
    created_at            TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by            CHAR(36)     NULL,
    updated_by            CHAR(36)     NULL,
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMP(6) NULL,
    deleted_by            CHAR(36)     NULL,
    version               BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_doctor_unavailabilities PRIMARY KEY (id),
    CONSTRAINT fk_doctor_unavailabilities_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_doctor_unavailabilities_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_doctor_unavailabilities_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT chk_doctor_unavailabilities_date_range CHECK (end_date >= start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_doctor_unavailabilities_tenant_id ON doctor_unavailabilities (tenant_id);
CREATE INDEX idx_doctor_unavailabilities_doctor ON doctor_unavailabilities (tenant_id, doctor_id);
CREATE INDEX idx_doctor_unavailabilities_type ON doctor_unavailabilities (tenant_id, doctor_id, unavailability_type);
CREATE INDEX idx_doctor_unavailabilities_range ON doctor_unavailabilities (tenant_id, doctor_id, start_date, end_date);
CREATE INDEX idx_doctor_unavailabilities_hospital ON doctor_unavailabilities (tenant_id, hospital_id);
CREATE INDEX idx_doctor_unavailabilities_deleted ON doctor_unavailabilities (deleted);
