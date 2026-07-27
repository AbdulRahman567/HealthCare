package com.healthcare.hms.appointments.calendar.dto.response;

import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.enums.AppointmentType;
import com.healthcare.hms.appointments.enums.VisitType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Lightweight calendar event — display labels enriched in a fixed batch of queries (no N+1).
 */
public record CalendarEventResponse(
        UUID id,
        String appointmentNumber,
        UUID hospitalId,
        UUID departmentId,
        String departmentName,
        UUID doctorId,
        String doctorName,
        String doctorEmployeeCode,
        UUID patientId,
        String patientName,
        String patientMrn,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer durationMinutes,
        AppointmentStatus status,
        AppointmentType appointmentType,
        VisitType visitType
) {
}
