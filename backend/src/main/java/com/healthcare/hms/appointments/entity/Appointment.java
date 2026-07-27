package com.healthcare.hms.appointments.entity;

import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.enums.AppointmentType;
import com.healthcare.hms.appointments.enums.VisitType;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Tenant-owned patient appointment (scheduling aggregate root).
 *
 * <p>Cross-cutting guarantees (inherited from {@link TenantOwnedEntity} /
 * {@link com.healthcare.hms.common.persistence.BaseEntity}): UUID primary key,
 * {@code tenant_id}, audit columns, soft delete, optimistic lock ({@code version}).
 *
 * <p>Appointment number is unique per tenant among non-deleted rows (see Flyway
 * {@code active_appointment_number_slot} generated column). Soft-deleted rows
 * free the number for reuse when a mistaken booking is corrected.
 *
 * <p>Relationships to Patient, Doctor, Department, and Hospital are UUID FKs
 * (module-boundary safe). Booking lifecycle (create / update / cancel / reschedule /
 * confirm) is enforced in the service layer (Phase 6.3).
 */
@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_appointments_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_appointments_tenant_number", columnList = "tenant_id, appointment_number"),
                @Index(name = "idx_appointments_tenant_date", columnList = "tenant_id, appointment_date"),
                @Index(name = "idx_appointments_tenant_doctor_date", columnList = "tenant_id, doctor_id, appointment_date"),
                @Index(
                        name = "idx_appointments_tenant_doctor_date_start",
                        columnList = "tenant_id, doctor_id, appointment_date, start_time"),
                @Index(name = "idx_appointments_tenant_patient_date", columnList = "tenant_id, patient_id, appointment_date"),
                @Index(
                        name = "idx_appointments_tenant_department_date",
                        columnList = "tenant_id, department_id, appointment_date"),
                @Index(name = "idx_appointments_tenant_hospital_date", columnList = "tenant_id, hospital_id, appointment_date"),
                @Index(name = "idx_appointments_tenant_status_date", columnList = "tenant_id, status, appointment_date"),
                @Index(name = "idx_appointments_tenant_type", columnList = "tenant_id, appointment_type"),
                @Index(name = "idx_appointments_tenant_visit_type", columnList = "tenant_id, visit_type"),
                @Index(name = "idx_appointments_tenant_visit_date", columnList = "tenant_id, visit_type, appointment_date"),
                @Index(
                        name = "idx_appointments_tenant_status_visit_date",
                        columnList = "tenant_id, status, visit_type, appointment_date"),
                @Index(name = "idx_appointments_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Appointment extends TenantOwnedEntity {

    /**
     * Hospital-facing appointment identifier (e.g. APT-2026-000142).
     * Unique per tenant for live (non-deleted) rows.
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "appointment_number", nullable = false, length = 50)
    private String appointmentNumber;

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
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Slot length in minutes. Service layer (later) validates consistency with
     * {@code endTime - startTime}.
     */
    @NotNull
    @Min(1)
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false, length = 30)
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 30)
    private VisitType visitType = VisitType.NEW;

    @Size(max = 2000)
    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Size(max = 500)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(final String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
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

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(final LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(final Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(final AppointmentStatus status) {
        this.status = status;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(final AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public VisitType getVisitType() {
        return visitType;
    }

    public void setVisitType(final VisitType visitType) {
        this.visitType = visitType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(final Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(final Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(final String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public boolean isBookableSlot() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }

    /**
     * Confirm attendance. Occupies the slot the same as {@link AppointmentStatus#SCHEDULED}.
     */
    public void confirm() {
        if (status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot confirm appointment from status " + status);
        }
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    /**
     * Mark appointment completed after the encounter.
     */
    public void complete() {
        if (!isBookableSlot()) {
            throw new IllegalStateException("Cannot complete appointment from status " + status);
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    /**
     * Cancel a bookable appointment (scheduled or confirmed).
     */
    public void cancel(final String reason) {
        if (!isBookableSlot()) {
            throw new IllegalStateException("Cannot cancel appointment from status " + status);
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        this.cancellationReason = reason;
    }

    /**
     * After reschedule, confirmation is cleared — patient must re-confirm the new slot.
     */
    public void clearConfirmationAfterReschedule() {
        if (status == AppointmentStatus.CONFIRMED) {
            this.status = AppointmentStatus.SCHEDULED;
        }
        this.confirmedAt = null;
        this.cancelledAt = null;
        this.cancellationReason = null;
    }

    /**
     * Mark appointment missed (no-show).
     */
    public void markMissed() {
        if (!isBookableSlot()) {
            throw new IllegalStateException("Cannot mark missed from status " + status);
        }
        this.status = AppointmentStatus.MISSED;
    }
}
