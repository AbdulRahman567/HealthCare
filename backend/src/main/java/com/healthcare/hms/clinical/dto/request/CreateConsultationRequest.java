package com.healthcare.hms.clinical.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Create a consultation encounter for an active patient with an active doctor.
 */
public record CreateConsultationRequest(
        @NotNull(message = "Patient id is required")
        UUID patientId,

        @NotNull(message = "Doctor id is required")
        UUID doctorId,

        @NotNull(message = "Department id is required")
        UUID departmentId,

        /** Optional source appointment (must match patient and doctor). */
        UUID appointmentId,

        @Size(max = 2000, message = "Chief complaint must not exceed 2000 characters")
        String chiefComplaint,

        /** When true, transitions DRAFT → IN_PROGRESS immediately after create. */
        boolean startImmediately
) {
}
