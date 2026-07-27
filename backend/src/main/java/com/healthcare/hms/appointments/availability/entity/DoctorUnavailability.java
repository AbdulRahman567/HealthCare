package com.healthcare.hms.appointments.availability.entity;

import com.healthcare.hms.appointments.availability.enums.UnavailabilityType;
import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Doctor leave, holiday, or emergency unavailability block.
 *
 * <p>All-day ranges use dates only. Timed blocks require {@code startDate == endDate}
 * with start/end times (typical for emergency partial-day blocks).
 */
@Entity
@Table(
        name = "doctor_unavailabilities",
        indexes = {
                @Index(name = "idx_doctor_unavailabilities_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_doctor_unavailabilities_doctor", columnList = "tenant_id, doctor_id"),
                @Index(name = "idx_doctor_unavailabilities_type", columnList = "tenant_id, doctor_id, unavailability_type"),
                @Index(name = "idx_doctor_unavailabilities_range", columnList = "tenant_id, doctor_id, start_date, end_date"),
                @Index(name = "idx_doctor_unavailabilities_hospital", columnList = "tenant_id, hospital_id"),
                @Index(name = "idx_doctor_unavailabilities_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class DoctorUnavailability extends TenantOwnedEntity {

    @NotNull
    @Column(name = "doctor_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unavailability_type", nullable = false, length = 30)
    private UnavailabilityType unavailabilityType;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "all_day", nullable = false)
    private boolean allDay = true;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;

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

    public UnavailabilityType getUnavailabilityType() {
        return unavailabilityType;
    }

    public void setUnavailabilityType(final UnavailabilityType unavailabilityType) {
        this.unavailabilityType = unavailabilityType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(final boolean allDay) {
        this.allDay = allDay;
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

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }
}
