package com.healthcare.hms.clinical.entity;

import com.healthcare.hms.clinical.enums.ConsultationStatus;
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
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Tenant-owned clinical encounter (consultation / visit) aggregate root.
 *
 * <p>Cross-cutting guarantees (inherited from {@link TenantOwnedEntity}):
 * UUID primary key, {@code tenant_id}, audit columns, soft delete, optimistic lock.
 *
 * <p>Relationships to Patient, Doctor, Department, Hospital, and Appointment are
 * UUID foreign keys (module-boundary safe — no cross-module {@code @ManyToOne}).
 * Child clinical data ({@link Diagnosis}, {@link ClinicalNote}, {@link VitalSigns},
 * {@link FollowUp}) reference {@code consultation_id}.
 *
 * <p>Consultation number is unique per tenant among non-deleted rows (see Flyway
 * {@code active_consultation_number_slot} generated column).
 */
@Entity
@Table(
        name = "consultations",
        indexes = {
                @Index(name = "idx_consultations_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_consultations_tenant_number", columnList = "tenant_id, consultation_number"),
                @Index(name = "idx_consultations_tenant_patient_date", columnList = "tenant_id, patient_id, consultation_date"),
                @Index(name = "idx_consultations_tenant_doctor_date", columnList = "tenant_id, doctor_id, consultation_date"),
                @Index(name = "idx_consultations_tenant_department_date", columnList = "tenant_id, department_id, consultation_date"),
                @Index(name = "idx_consultations_tenant_hospital_date", columnList = "tenant_id, hospital_id, consultation_date"),
                @Index(name = "idx_consultations_tenant_status_date", columnList = "tenant_id, status, consultation_date"),
                @Index(name = "idx_consultations_tenant_appointment", columnList = "tenant_id, appointment_id"),
                @Index(name = "idx_consultations_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Consultation extends TenantOwnedEntity {

    /**
     * Hospital-facing consultation identifier (e.g. CON-2026-000142).
     * Unique per tenant for live (non-deleted) rows.
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "consultation_number", nullable = false, length = 50)
    private String consultationNumber;

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotNull
    @Column(name = "doctor_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @NotNull
    @Column(name = "department_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID departmentId;

    /**
     * Source appointment when the encounter originated from scheduling.
     * Nullable for walk-in or emergency encounters without a prior booking.
     */
    @Column(name = "appointment_id", updatable = false, columnDefinition = "CHAR(36)")
    private UUID appointmentId;

    @NotNull
    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ConsultationStatus status = ConsultationStatus.DRAFT;

    @Size(max = 2000)
    @Column(name = "chief_complaint", length = 2000)
    private String chiefComplaint;

    /**
     * High-level patient advice documented at encounter closure.
     * Detailed SOAP notes live in {@link ClinicalNote}.
     */
    @Size(max = 2000)
    @Column(name = "advice", length = 2000)
    private String advice;

    @Size(max = 2000)
    @Column(name = "summary", length = 2000)
    private String summary;

    @Size(max = 4000)
    @Column(name = "history_of_present_illness", length = 4000)
    private String historyOfPresentIllness;

    @Size(max = 4000)
    @Column(name = "physical_examination", length = 4000)
    private String physicalExamination;

    @Size(max = 4000)
    @Column(name = "doctor_notes", length = 4000)
    private String doctorNotes;

    @Column(name = "paused_at")
    private Instant pausedAt;

    public String getConsultationNumber() {
        return consultationNumber;
    }

    public void setConsultationNumber(final String consultationNumber) {
        this.consultationNumber = consultationNumber;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(final UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(final UUID doctorId) {
        this.doctorId = doctorId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(final UUID departmentId) {
        this.departmentId = departmentId;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(final UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDate getConsultationDate() {
        return consultationDate;
    }

    public void setConsultationDate(final LocalDate consultationDate) {
        this.consultationDate = consultationDate;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(final Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(final Instant completedAt) {
        this.completedAt = completedAt;
    }

    public ConsultationStatus getStatus() {
        return status;
    }

    public void setStatus(final ConsultationStatus status) {
        this.status = status;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(final String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(final String advice) {
        this.advice = advice;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getHistoryOfPresentIllness() {
        return historyOfPresentIllness;
    }

    public void setHistoryOfPresentIllness(final String historyOfPresentIllness) {
        this.historyOfPresentIllness = historyOfPresentIllness;
    }

    public String getPhysicalExamination() {
        return physicalExamination;
    }

    public void setPhysicalExamination(final String physicalExamination) {
        this.physicalExamination = physicalExamination;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(final String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public Instant getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(final Instant pausedAt) {
        this.pausedAt = pausedAt;
    }

    public void start() {
        if (status != ConsultationStatus.DRAFT) {
            throw new IllegalStateException("Cannot start consultation from status " + status);
        }
        this.status = ConsultationStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    public void pause() {
        if (status != ConsultationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot pause consultation from status " + status);
        }
        this.status = ConsultationStatus.PAUSED;
        this.pausedAt = Instant.now();
    }

    public void resume() {
        if (status != ConsultationStatus.PAUSED) {
            throw new IllegalStateException("Cannot resume consultation from status " + status);
        }
        this.status = ConsultationStatus.IN_PROGRESS;
        this.pausedAt = null;
    }

    public void complete() {
        if (status != ConsultationStatus.IN_PROGRESS && status != ConsultationStatus.PAUSED) {
            throw new IllegalStateException("Cannot complete consultation from status " + status);
        }
        this.status = ConsultationStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.pausedAt = null;
    }

    public void cancel() {
        if (status == ConsultationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed consultation");
        }
        this.status = ConsultationStatus.CANCELLED;
    }

    public boolean isEditable() {
        return status == ConsultationStatus.DRAFT
                || status == ConsultationStatus.IN_PROGRESS
                || status == ConsultationStatus.PAUSED;
    }

    public boolean isActiveEncounter() {
        return status == ConsultationStatus.IN_PROGRESS || status == ConsultationStatus.PAUSED;
    }
}
