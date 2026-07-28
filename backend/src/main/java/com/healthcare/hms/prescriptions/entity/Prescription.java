package com.healthcare.hms.prescriptions.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
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
 * Tenant-owned digital prescription aggregate root.
 *
 * <p>Linked to a {@code consultation_id} (visit). Line items live in
 * {@link PrescriptionItem}. Cross-module links are UUID FKs only.
 */
@Entity
@Table(
        name = "prescriptions",
        indexes = {
                @Index(name = "idx_prescriptions_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_prescriptions_tenant_number", columnList = "tenant_id, prescription_number"),
                @Index(name = "idx_prescriptions_tenant_consultation", columnList = "tenant_id, consultation_id"),
                @Index(name = "idx_prescriptions_tenant_patient_date", columnList = "tenant_id, patient_id, prescription_date"),
                @Index(name = "idx_prescriptions_tenant_doctor_date", columnList = "tenant_id, doctor_id, prescription_date"),
                @Index(name = "idx_prescriptions_tenant_status_date", columnList = "tenant_id, status, prescription_date"),
                @Index(name = "idx_prescriptions_tenant_pharmacy_ref", columnList = "tenant_id, pharmacy_reference"),
                @Index(name = "idx_prescriptions_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Prescription extends TenantOwnedEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "prescription_number", nullable = false, length = 50)
    private String prescriptionNumber;

    @NotNull
    @Column(name = "consultation_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID consultationId;

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

    @NotNull
    @Column(name = "prescription_date", nullable = false)
    private LocalDate prescriptionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PrescriptionStatus status = PrescriptionStatus.DRAFT;

    @Size(max = 2000)
    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Size(max = 500)
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    /** Pharmacy fill timestamp (future integration). */
    @Column(name = "dispensed_at")
    private Instant dispensedAt;

    /** External pharmacy / dispensing system reference (future integration). */
    @Size(max = 100)
    @Column(name = "pharmacy_reference", length = 100)
    private String pharmacyReference;

    public String getPrescriptionNumber() {
        return prescriptionNumber;
    }

    public void setPrescriptionNumber(final String prescriptionNumber) {
        this.prescriptionNumber = prescriptionNumber;
    }

    public UUID getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(final UUID consultationId) {
        this.consultationId = consultationId;
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

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(final LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(final PrescriptionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(final Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(final Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(final String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Instant getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(final Instant dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public String getPharmacyReference() {
        return pharmacyReference;
    }

    public void setPharmacyReference(final String pharmacyReference) {
        this.pharmacyReference = pharmacyReference;
    }

    public boolean isDraft() {
        return status == PrescriptionStatus.DRAFT;
    }

    public boolean isLineItemsMutable() {
        return status == PrescriptionStatus.DRAFT;
    }

    public void issue() {
        if (status != PrescriptionStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT prescriptions can be issued (status=" + status + ")");
        }
        this.status = PrescriptionStatus.ISSUED;
        this.issuedAt = Instant.now();
    }

    public void cancel(final String reason) {
        if (status == PrescriptionStatus.DISPENSED || status == PrescriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel prescription in status=" + status);
        }
        this.status = PrescriptionStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        this.cancelReason = reason;
    }
}
