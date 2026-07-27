package com.healthcare.hms.appointments.availability.entity;

import com.healthcare.hms.appointments.availability.enums.ScheduleRecurrenceType;
import com.healthcare.hms.appointments.availability.enums.ScheduleStatus;
import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Recurring doctor availability schedule (working days/hours template).
 *
 * <p>Child rows: {@link DoctorScheduleWindow} (working hours per weekday) and
 * {@link DoctorScheduleBreak} (breaks). Date range {@code effectiveFrom}–
 * {@code effectiveTo} (nullable end = open-ended) supports future recurring
 * schedules across successive periods without overlapping ACTIVE ranges.
 */
@Entity
@Table(
        name = "doctor_schedules",
        indexes = {
                @Index(name = "idx_doctor_schedules_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_doctor_schedules_doctor", columnList = "tenant_id, doctor_id"),
                @Index(name = "idx_doctor_schedules_doctor_status", columnList = "tenant_id, doctor_id, status"),
                @Index(name = "idx_doctor_schedules_effective", columnList = "tenant_id, doctor_id, effective_from, effective_to"),
                @Index(name = "idx_doctor_schedules_hospital", columnList = "tenant_id, hospital_id"),
                @Index(name = "idx_doctor_schedules_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class DoctorSchedule extends TenantOwnedEntity {

    @NotNull
    @Column(name = "doctor_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @Size(max = 150)
    @Column(name = "name", length = 150)
    private String name;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    /** Inclusive end; {@code null} means open-ended (supports future recurrence). */
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @NotNull
    @Min(1)
    @Max(500)
    @Column(name = "max_appointments_per_day", nullable = false)
    private Integer maxAppointmentsPerDay = 20;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 30)
    private ScheduleRecurrenceType recurrenceType = ScheduleRecurrenceType.WEEKLY;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(final LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(final LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Integer getMaxAppointmentsPerDay() {
        return maxAppointmentsPerDay;
    }

    public void setMaxAppointmentsPerDay(final Integer maxAppointmentsPerDay) {
        this.maxAppointmentsPerDay = maxAppointmentsPerDay;
    }

    public ScheduleRecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(final ScheduleRecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(final ScheduleStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public void deactivate() {
        this.status = ScheduleStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == ScheduleStatus.ACTIVE;
    }
}
