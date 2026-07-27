package com.healthcare.hms.appointments.queue.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * One daily queue per doctor (tenant-scoped).
 *
 * <p>Entries ({@link QueueEntry}) belong to this aggregate and receive automatic
 * queue numbers via {@link #allocateNextNumber()}.
 */
@Entity
@Table(
        name = "doctor_day_queues",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doctor_day_queues_tenant_doctor_date",
                        columnNames = {"tenant_id", "doctor_id", "queue_date"}
                )
        },
        indexes = {
                @Index(name = "idx_doctor_day_queues_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_doctor_day_queues_doctor_date", columnList = "tenant_id, doctor_id, queue_date"),
                @Index(name = "idx_doctor_day_queues_hospital_date", columnList = "tenant_id, hospital_id, queue_date"),
                @Index(name = "idx_doctor_day_queues_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class DoctorDayQueue extends TenantOwnedEntity {

    @NotNull
    @Column(name = "doctor_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @NotNull
    @Column(name = "queue_date", nullable = false, updatable = false)
    private LocalDate queueDate;

    /**
     * Last issued queue number for this doctor/day (monotonic).
     */
    @NotNull
    @Min(0)
    @Column(name = "last_queue_number", nullable = false)
    private Integer lastQueueNumber = 0;

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

    public LocalDate getQueueDate() {
        return queueDate;
    }

    public void setQueueDate(final LocalDate queueDate) {
        this.queueDate = queueDate;
    }

    public Integer getLastQueueNumber() {
        return lastQueueNumber;
    }

    public void setLastQueueNumber(final Integer lastQueueNumber) {
        this.lastQueueNumber = lastQueueNumber;
    }

    /**
     * Atomically advances and returns the next queue number for this day.
     */
    public int allocateNextNumber() {
        this.lastQueueNumber = this.lastQueueNumber + 1;
        return this.lastQueueNumber;
    }
}
