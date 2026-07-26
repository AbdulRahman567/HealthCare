package com.healthcare.hms.patients.history.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.patients.history.enums.ClinicalConditionStatus;
import com.healthcare.hms.patients.history.enums.ClinicalSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Shared structured clinical fields for patient history entries.
 *
 * <p>Notes are bounded ({@code VARCHAR(1000)}) — not unbounded free-text blobs.
 * Disease/procedure identity lives on concrete subtypes with category enums.
 */
@MappedSuperclass
public abstract class ClinicalHistoryEntry extends TenantOwnedEntity {

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotNull
    @Column(name = "medical_history_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID medicalHistoryId;

    @NotNull
    @PastOrPresent
    @Column(name = "diagnosis_date", nullable = false)
    private LocalDate diagnosisDate;

    @PastOrPresent
    @Column(name = "recovery_date")
    private LocalDate recoveryDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private ClinicalSeverity severity = ClinicalSeverity.UNKNOWN;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", nullable = false, length = 20)
    private ClinicalConditionStatus conditionStatus = ClinicalConditionStatus.ONGOING;

    /**
     * Bounded clinical notes (max 1000). Prefer structured fields over prose.
     */
    @Size(max = 1000)
    @Column(name = "clinical_notes", length = 1000)
    private String clinicalNotes;

    /**
     * Staff user who recorded the entry ({@code users.id}), when known.
     */
    @Column(name = "recorded_by_user_id", columnDefinition = "CHAR(36)")
    private UUID recordedByUserId;

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getMedicalHistoryId() {
        return medicalHistoryId;
    }

    public void setMedicalHistoryId(final UUID medicalHistoryId) {
        this.medicalHistoryId = medicalHistoryId;
    }

    public LocalDate getDiagnosisDate() {
        return diagnosisDate;
    }

    public void setDiagnosisDate(final LocalDate diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }

    public LocalDate getRecoveryDate() {
        return recoveryDate;
    }

    public void setRecoveryDate(final LocalDate recoveryDate) {
        this.recoveryDate = recoveryDate;
    }

    public ClinicalSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(final ClinicalSeverity severity) {
        this.severity = severity;
    }

    public ClinicalConditionStatus getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(final ClinicalConditionStatus conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(final String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public UUID getRecordedByUserId() {
        return recordedByUserId;
    }

    public void setRecordedByUserId(final UUID recordedByUserId) {
        this.recordedByUserId = recordedByUserId;
    }
}
