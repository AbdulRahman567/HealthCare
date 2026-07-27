package com.healthcare.hms.appointments.queue.entity;

import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Single patient position on a {@link DoctorDayQueue}.
 *
 * <p>Chronological order for the day is {@code queueNumber} ascending (assigned at check-in).
 */
@Entity
@Table(
        name = "queue_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_queue_entries_queue_number",
                        columnNames = {"queue_id", "queue_number"}
                )
        },
        indexes = {
                @Index(name = "idx_queue_entries_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_queue_entries_queue_status", columnList = "tenant_id, queue_id, status"),
                @Index(name = "idx_queue_entries_queue_number", columnList = "tenant_id, queue_id, queue_number"),
                @Index(name = "idx_queue_entries_doctor", columnList = "tenant_id, doctor_id"),
                @Index(name = "idx_queue_entries_patient", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_queue_entries_appointment", columnList = "appointment_id"),
                @Index(
                        name = "idx_queue_entries_tenant_status_appointment",
                        columnList = "tenant_id, status, appointment_id"),
                @Index(name = "idx_queue_entries_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class QueueEntry extends TenantOwnedEntity {

    @NotNull
    @Column(name = "queue_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID queueId;

    @NotNull
    @Column(name = "appointment_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID appointmentId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotNull
    @Column(name = "doctor_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @NotNull
    @Min(1)
    @Column(name = "queue_number", nullable = false, updatable = false)
    private Integer queueNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QueueEntryStatus status = QueueEntryStatus.CHECKED_IN;

    @NotNull
    @Column(name = "checked_in_at", nullable = false, updatable = false)
    private Instant checkedInAt;

    @Column(name = "status_changed_at", nullable = false)
    private Instant statusChangedAt;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    public UUID getQueueId() {
        return queueId;
    }

    public void setQueueId(final UUID queueId) {
        this.queueId = queueId;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(final UUID appointmentId) {
        this.appointmentId = appointmentId;
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

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(final UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public Integer getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(final Integer queueNumber) {
        this.queueNumber = queueNumber;
    }

    public QueueEntryStatus getStatus() {
        return status;
    }

    public void setStatus(final QueueEntryStatus status) {
        this.status = status;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(final Instant checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public Instant getStatusChangedAt() {
        return statusChangedAt;
    }

    public void setStatusChangedAt(final Instant statusChangedAt) {
        this.statusChangedAt = statusChangedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public boolean isTerminal() {
        return status == QueueEntryStatus.COMPLETED
                || status == QueueEntryStatus.MISSED
                || status == QueueEntryStatus.CANCELLED;
    }

    public void markWaiting() {
        transition(QueueEntryStatus.CHECKED_IN, QueueEntryStatus.WAITING);
    }

    public void startConsultation() {
        transition(QueueEntryStatus.WAITING, QueueEntryStatus.IN_CONSULTATION);
    }

    public void complete() {
        transition(QueueEntryStatus.IN_CONSULTATION, QueueEntryStatus.COMPLETED);
    }

    public void markMissed() {
        if (status != QueueEntryStatus.CHECKED_IN && status != QueueEntryStatus.WAITING) {
            throw new IllegalStateException("Cannot mark missed from status " + status);
        }
        applyStatus(QueueEntryStatus.MISSED);
    }

    public void cancel() {
        if (isTerminal()) {
            throw new IllegalStateException("Cannot cancel from status " + status);
        }
        applyStatus(QueueEntryStatus.CANCELLED);
    }

    private void transition(final QueueEntryStatus from, final QueueEntryStatus to) {
        if (status != from) {
            throw new IllegalStateException("Cannot transition to " + to + " from status " + status);
        }
        applyStatus(to);
    }

    private void applyStatus(final QueueEntryStatus to) {
        this.status = to;
        this.statusChangedAt = Instant.now();
    }
}
