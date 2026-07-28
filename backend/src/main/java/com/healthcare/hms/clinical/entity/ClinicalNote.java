package com.healthcare.hms.clinical.entity;

import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Structured clinical note attached to a {@link Consultation}.
 *
 * <p>Notes are bounded ({@code VARCHAR(4000)}) — prefer SOAP-typed sections over
 * unbounded free-text blobs.
 */
@Entity
@Table(
        name = "clinical_notes",
        indexes = {
                @Index(name = "idx_clinical_notes_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_clinical_notes_consultation", columnList = "tenant_id, consultation_id"),
                @Index(name = "idx_clinical_notes_patient", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_clinical_notes_author", columnList = "tenant_id, author_doctor_id"),
                @Index(name = "idx_clinical_notes_type", columnList = "tenant_id, consultation_id, note_type"),
                @Index(name = "idx_clinical_notes_recorded_at", columnList = "tenant_id, consultation_id, recorded_at"),
                @Index(name = "idx_clinical_notes_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class ClinicalNote extends TenantOwnedEntity {

    @NotNull
    @Column(name = "consultation_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID consultationId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    /**
     * Attending doctor who authored the note ({@code doctors.id}).
     */
    @NotNull
    @Column(name = "author_doctor_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID authorDoctorId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 30)
    private ClinicalNoteType noteType = ClinicalNoteType.GENERAL;

    /** Optional short title (useful for progress / procedure / discharge notes). */
    @Size(max = 200)
    @Column(name = "title", length = 200)
    private String title;

    @NotBlank
    @Size(max = 4000)
    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

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

    public UUID getAuthorDoctorId() {
        return authorDoctorId;
    }

    public void setAuthorDoctorId(final UUID authorDoctorId) {
        this.authorDoctorId = authorDoctorId;
    }

    public ClinicalNoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(final ClinicalNoteType noteType) {
        this.noteType = noteType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(final Instant recordedAt) {
        this.recordedAt = recordedAt;
    }
}
