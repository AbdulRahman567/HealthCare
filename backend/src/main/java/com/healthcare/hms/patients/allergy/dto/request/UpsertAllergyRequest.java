package com.healthcare.hms.patients.allergy.dto.request;

import com.healthcare.hms.patients.allergy.enums.AllergyStatus;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import com.healthcare.hms.patients.allergy.enums.Reaction;
import com.healthcare.hms.patients.allergy.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Create / update allergy payload (structured clinical fields only).
 */
public record UpsertAllergyRequest(
        @NotBlank(message = "Allergen name is required")
        @Size(max = 200, message = "Allergen name must not exceed 200 characters")
        String allergenName,

        @Size(max = 64, message = "Allergen code must not exceed 64 characters")
        String allergenCode,

        @NotNull(message = "Allergy type is required")
        AllergyType allergyType,

        @NotNull(message = "Severity is required")
        Severity severity,

        @NotNull(message = "Reaction is required")
        Reaction reaction,

        AllergyStatus status,

        @PastOrPresent(message = "Onset date must be in the past or today")
        LocalDate onsetDate,

        @Size(max = 1000, message = "Clinical notes must not exceed 1000 characters")
        String clinicalNotes,

        Boolean verified,

        Boolean patientReported,

        Boolean criticalAlert,

        Boolean showOnBanner
) {
}
