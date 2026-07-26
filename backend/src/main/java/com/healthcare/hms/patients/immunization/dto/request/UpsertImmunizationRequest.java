package com.healthcare.hms.patients.immunization.dto.request;

import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.enums.VaccineRoute;
import com.healthcare.hms.patients.immunization.validation.ValidImmunizationDateRange;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Create / update immunization payload (structured clinical fields only).
 */
@ValidImmunizationDateRange
public record UpsertImmunizationRequest(
        @NotBlank(message = "Vaccine name is required")
        @Size(max = 200, message = "Vaccine name must not exceed 200 characters")
        String vaccineName,

        @Size(max = 64, message = "Vaccine code must not exceed 64 characters")
        String vaccineCode,

        @NotNull(message = "Dose number is required")
        @Min(value = 1, message = "Dose number must be at least 1")
        @Max(value = 50, message = "Dose number must not exceed 50")
        Integer doseNumber,

        @Size(max = 200, message = "Manufacturer must not exceed 200 characters")
        String manufacturer,

        @Size(max = 100, message = "Batch number must not exceed 100 characters")
        String batchNumber,

        @NotNull(message = "Administration date is required")
        @PastOrPresent(message = "Administration date must be in the past or present")
        LocalDate administrationDate,

        LocalDate nextDueDate,

        @NotBlank(message = "Healthcare provider is required")
        @Size(max = 200, message = "Healthcare provider must not exceed 200 characters")
        String healthcareProvider,

        VaccineRoute route,

        ImmunizationStatus status,

        @Size(max = 1000, message = "Clinical notes must not exceed 1000 characters")
        String clinicalNotes
) {
}
