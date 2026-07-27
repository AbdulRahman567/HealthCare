package com.healthcare.hms.appointments.dto.request;

import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.enums.VisitType;
import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Appointment directory search / filter criteria (Phase 6.6).
 *
 * <p>All filters are applied in the database via JPA Specifications —
 * never in-memory after a full load.
 */
public record AppointmentSearchCriteria(
        /** Exact or prefix match on hospital-facing appointment number. */
        String appointmentNumber,
        UUID patientId,
        /** Patient first / last / full name (contains, case-insensitive). */
        String patientName,
        UUID doctorId,
        /** Doctor display name via linked user (contains, case-insensitive). */
        String doctorName,
        UUID departmentId,
        /** Department name (contains, case-insensitive). */
        String departmentName,
        AppointmentStatus status,
        VisitType visitType,
        /** Inclusive appointment date lower bound. */
        LocalDate fromDate,
        /** Inclusive appointment date upper bound. */
        LocalDate toDate,
        /** Latest non-deleted queue entry status for the appointment. */
        QueueEntryStatus queueStatus
) {
}
