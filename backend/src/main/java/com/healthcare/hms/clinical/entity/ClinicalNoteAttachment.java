package com.healthcare.hms.clinical.entity;

import com.healthcare.hms.clinical.enums.ClinicalAttachmentKind;
import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * File attachment metadata for a {@link ClinicalNote}.
 *
 * <p>Binary content lives in object storage ({@code storage_key}); only metadata is persisted.
 */
@Entity
@Table(
        name = "clinical_note_attachments",
        indexes = {
                @Index(name = "idx_clinical_note_attachments_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_clinical_note_attachments_note", columnList = "tenant_id, clinical_note_id"),
                @Index(name = "idx_clinical_note_attachments_consultation", columnList = "tenant_id, consultation_id"),
                @Index(name = "idx_clinical_note_attachments_patient", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_clinical_note_attachments_storage_key", columnList = "tenant_id, storage_key"),
                @Index(name = "idx_clinical_note_attachments_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class ClinicalNoteAttachment extends TenantOwnedEntity {

    @NotNull
    @Column(name = "clinical_note_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID clinicalNoteId;

    @NotNull
    @Column(name = "consultation_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID consultationId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotNull
    @Column(name = "uploaded_by_user_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID uploadedByUserId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @NotNull
    @Min(1)
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @NotBlank
    @Size(max = 512)
    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_kind", nullable = false, length = 20)
    private ClinicalAttachmentKind attachmentKind = ClinicalAttachmentKind.OTHER;

    @Size(max = 200)
    @Column(name = "description", length = 200)
    private String description;

    public UUID getClinicalNoteId() {
        return clinicalNoteId;
    }

    public void setClinicalNoteId(final UUID clinicalNoteId) {
        this.clinicalNoteId = clinicalNoteId;
    }

    public UUID getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(final UUID consultationId) {
        this.consultationId = consultationId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(final UUID uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(final Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(final String storageKey) {
        this.storageKey = storageKey;
    }

    public ClinicalAttachmentKind getAttachmentKind() {
        return attachmentKind;
    }

    public void setAttachmentKind(final ClinicalAttachmentKind attachmentKind) {
        this.attachmentKind = attachmentKind;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
