-- Phase 5.7: Patient search — affiliation columns + composite indexes for filtered lists.
-- primary_department_id / primary_doctor_id are nullable (populated by later assignment workflows).

ALTER TABLE patients
    ADD COLUMN primary_department_id CHAR(36) NULL AFTER status,
    ADD COLUMN primary_doctor_id CHAR(36) NULL AFTER primary_department_id;

ALTER TABLE patients
    ADD CONSTRAINT fk_patients_primary_department
        FOREIGN KEY (primary_department_id) REFERENCES departments (id),
    ADD CONSTRAINT fk_patients_primary_doctor
        FOREIGN KEY (primary_doctor_id) REFERENCES doctors (id);

CREATE INDEX idx_patients_tenant_gender ON patients (tenant_id, gender);
CREATE INDEX idx_patients_tenant_blood_group ON patients (tenant_id, blood_group);
CREATE INDEX idx_patients_tenant_status_name ON patients (tenant_id, status, last_name, first_name);
CREATE INDEX idx_patients_tenant_department ON patients (tenant_id, primary_department_id);
CREATE INDEX idx_patients_tenant_doctor ON patients (tenant_id, primary_doctor_id);
CREATE INDEX idx_patients_tenant_created_at ON patients (tenant_id, created_at);
