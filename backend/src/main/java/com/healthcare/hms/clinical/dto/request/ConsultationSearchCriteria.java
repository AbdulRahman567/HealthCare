package com.healthcare.hms.clinical.dto.request;

import com.healthcare.hms.clinical.enums.ConsultationStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Search filters for consultation directory queries.
 */
public record ConsultationSearchCriteria(
        String consultationNumber,
        UUID patientId,
        String patientName,
        UUID doctorId,
        String doctorName,
        UUID departmentId,
        ConsultationStatus status,
        LocalDate fromDate,
        LocalDate toDate,
        UUID appointmentId
) {
}
