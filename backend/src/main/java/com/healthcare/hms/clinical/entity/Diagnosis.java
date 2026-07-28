package com.healthcare.hms.clinical.entity;

import com.healthcare.hms.clinical.enums.DiagnosisSeverity;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
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
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Structured diagnosis recorded during a {@link Consultation}.
 *
 * <p>Distinct from patient longitudinal history entries — this captures the
 * encounter-specific diagnostic assertion with ICD-like coding support.
 */
@Entity
@Table(
        name = "consultation_diagnoses",
        indexes = {
                @Index(name = "idx_consultation_diagnoses_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_consultation_diagnoses_consultation", columnList = "tenant_id, consultation_id"),
                @Index(name = "idx_consultation_diagnoses_patient", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_consultation_diagnoses_doctor", columnList = "tenant_id, diagnosing_doctor_id"),
                @Index(name = "idx_consultation_diagnoses_type", columnList = "tenant_id, consultation_id, diagnosis_type"),
                @Index(name = "idx_consultation_diagnoses_status", columnList = "tenant_id, patient_id, diagnosis_status"),
                @Index(name = "idx_consultation_diagnoses_icd", columnList = "tenant_id, icd_code"),
                @Index(name = "idx_consultation_diagnoses_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Diagnosis extends TenantOwnedEntity {

    @NotNull
    @Column(name = "consultation_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID consultationId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotNull
    @Column(name = "diagnosing_doctor_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID diagnosingDoctorId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "diagnosis_name", nullable = false, length = 200)
    private String diagnosisName;

    /** Optional ICD-10 / local coding system identifier. */
    @Size(max = 32)
    @Column(name = "icd_code", length = 32)
    private String icdCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "diagnosis_type", nullable = false, length = 20)
    private DiagnosisType diagnosisType = DiagnosisType.PRIMARY;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "diagnosis_status", nullable = false, length = 20)
    private DiagnosisStatus diagnosisStatus = DiagnosisStatus.PROVISIONAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private DiagnosisSeverity severity = DiagnosisSeverity.UNKNOWN;

    @NotNull
    @Column(name = "diagnosed_at", nullable = false)
    private Instant diagnosedAt;

    /**
     * Display order within the consultation (primary diagnosis should be {@code 1}).
     */
    @NotNull
    @Min(1)
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber = 1;

    @Size(max = 1000)
    @Column(name = "clinical_notes", length = 1000)
    private String clinicalNotes;

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

    public UUID getDiagnosingDoctorId() {
        return diagnosingDoctorId;
    }

    public void setDiagnosingDoctorId(final UUID diagnosingDoctorId) {
        this.diagnosingDoctorId = diagnosingDoctorId;
    }

    public String getDiagnosisName() {
        return diagnosisName;
    }

    public void setDiagnosisName(final String diagnosisName) {
        this.diagnosisName = diagnosisName;
    }

    public String getIcdCode() {
        return icdCode;
    }

    public void setIcdCode(final String icdCode) {
        this.icdCode = icdCode;
    }

    public DiagnosisType getDiagnosisType() {
        return diagnosisType;
    }

    public void setDiagnosisType(final DiagnosisType diagnosisType) {
        this.diagnosisType = diagnosisType;
    }

    public DiagnosisStatus getDiagnosisStatus() {
        return diagnosisStatus;
    }

    public void setDiagnosisStatus(final DiagnosisStatus diagnosisStatus) {
        this.diagnosisStatus = diagnosisStatus;
    }

    public DiagnosisSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(final DiagnosisSeverity severity) {
        this.severity = severity;
    }

    public Instant getDiagnosedAt() {
        return diagnosedAt;
    }

    public void setDiagnosedAt(final Instant diagnosedAt) {
        this.diagnosedAt = diagnosedAt;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(final Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(final String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }
}
