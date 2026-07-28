-- Phase 7.6: Clinical notes API support — note title, expanded types, attachments metadata.

ALTER TABLE clinical_notes
    MODIFY COLUMN note_type VARCHAR(30) NOT NULL;

ALTER TABLE clinical_notes
    ADD COLUMN title VARCHAR(200) NULL AFTER note_type;

CREATE TABLE clinical_note_attachments (
    id                      CHAR(36)      NOT NULL,
    tenant_id               CHAR(36)      NOT NULL,
    clinical_note_id        CHAR(36)      NOT NULL,
    consultation_id         CHAR(36)      NOT NULL,
    patient_id              CHAR(36)      NOT NULL,
    uploaded_by_user_id     CHAR(36)      NOT NULL,
    file_name               VARCHAR(255)  NOT NULL,
    content_type            VARCHAR(100)  NOT NULL,
    size_bytes              BIGINT        NOT NULL,
    storage_key             VARCHAR(512)  NOT NULL,
    attachment_kind         VARCHAR(20)   NOT NULL,
    description             VARCHAR(200)  NULL,
    created_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by              CHAR(36)      NULL,
    updated_by              CHAR(36)      NULL,
    deleted                 BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP(6)  NULL,
    deleted_by              CHAR(36)      NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_clinical_note_attachments PRIMARY KEY (id),
    CONSTRAINT fk_clinical_note_attachments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_clinical_note_attachments_note FOREIGN KEY (clinical_note_id) REFERENCES clinical_notes (id),
    CONSTRAINT fk_clinical_note_attachments_consultation FOREIGN KEY (consultation_id) REFERENCES consultations (id),
    CONSTRAINT fk_clinical_note_attachments_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_clinical_note_attachments_uploader FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_clinical_note_attachments_size CHECK (size_bytes >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_clinical_note_attachments_tenant_id ON clinical_note_attachments (tenant_id);
CREATE INDEX idx_clinical_note_attachments_note ON clinical_note_attachments (tenant_id, clinical_note_id);
CREATE INDEX idx_clinical_note_attachments_consultation ON clinical_note_attachments (tenant_id, consultation_id);
CREATE INDEX idx_clinical_note_attachments_patient ON clinical_note_attachments (tenant_id, patient_id);
CREATE INDEX idx_clinical_note_attachments_storage_key ON clinical_note_attachments (tenant_id, storage_key);
CREATE INDEX idx_clinical_note_attachments_deleted ON clinical_note_attachments (deleted);
