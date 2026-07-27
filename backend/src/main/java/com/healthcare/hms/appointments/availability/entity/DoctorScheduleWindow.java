package com.healthcare.hms.appointments.availability.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Working-day hours window within a {@link DoctorSchedule}.
 *
 * <p>Multiple non-overlapping windows per weekday are allowed (e.g. morning + afternoon).
 */
@Entity
@Table(
        name = "doctor_schedule_windows",
        indexes = {
                @Index(name = "idx_doctor_schedule_windows_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_doctor_schedule_windows_schedule", columnList = "tenant_id, schedule_id"),
                @Index(name = "idx_doctor_schedule_windows_day", columnList = "tenant_id, schedule_id, day_of_week"),
                @Index(name = "idx_doctor_schedule_windows_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class DoctorScheduleWindow extends TenantOwnedEntity {

    @NotNull
    @Column(name = "schedule_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID scheduleId;

    @NotNull
    @Column(name = "doctor_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(final UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(final UUID doctorId) {
        this.doctorId = doctorId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(final DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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
}
