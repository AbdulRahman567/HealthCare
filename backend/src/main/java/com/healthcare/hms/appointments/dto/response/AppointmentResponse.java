package com.healthcare.hms.appointments.dto.response;

import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.enums.AppointmentType;
import com.healthcare.hms.appointments.enums.VisitType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        String appointmentNumber,
        UUID hospitalId,
        UUID patientId,
        String patientName,
        String patientMrn,
        UUID doctorId,
        UUID departmentId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer durationMinutes,
        AppointmentStatus status,
        AppointmentType appointmentType,
        VisitType visitType,
        String notes,
        Instant confirmedAt,
        Instant cancelledAt,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
