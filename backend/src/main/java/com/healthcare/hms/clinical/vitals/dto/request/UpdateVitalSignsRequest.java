package com.healthcare.hms.clinical.vitals.dto.request;

import com.healthcare.hms.clinical.vitals.validation.ValidVitalSignsCorrection;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Correct a vitals row entered in error while the parent consultation remains editable.
 */
@ValidVitalSignsCorrection
public record UpdateVitalSignsRequest(
        @DecimalMin(value = "35.0", message = "Temperature must be at least 35.0 °C")
        @DecimalMax(value = "42.0", message = "Temperature must not exceed 42.0 °C")
        BigDecimal temperatureCelsius,

        @Min(value = 20, message = "Heart rate must be at least 20 bpm")
        @Max(value = 250, message = "Heart rate must not exceed 250 bpm")
        Integer heartRateBpm,

        @Min(value = 40, message = "Systolic BP must be at least 40 mmHg")
        @Max(value = 300, message = "Systolic BP must not exceed 300 mmHg")
        Integer systolicBp,

        @Min(value = 20, message = "Diastolic BP must be at least 20 mmHg")
        @Max(value = 200, message = "Diastolic BP must not exceed 200 mmHg")
        Integer diastolicBp,

        @Min(value = 4, message = "Respiratory rate must be at least 4 breaths/min")
        @Max(value = 60, message = "Respiratory rate must not exceed 60 breaths/min")
        Integer respiratoryRate,

        @DecimalMin(value = "50.0", message = "Oxygen saturation must be at least 50%")
        @DecimalMax(value = "100.0", message = "Oxygen saturation must not exceed 100%")
        BigDecimal oxygenSaturationPercent,

        @DecimalMin(value = "30.0", message = "Height must be at least 30 cm")
        @DecimalMax(value = "250.0", message = "Height must not exceed 250 cm")
        BigDecimal heightCm,

        @DecimalMin(value = "0.5", message = "Weight must be at least 0.5 kg")
        @DecimalMax(value = "300.0", message = "Weight must not exceed 300 kg")
        BigDecimal weightKg,

        @Min(value = 0, message = "Pain scale must be between 0 and 10")
        @Max(value = 10, message = "Pain scale must be between 0 and 10")
        Integer painScale,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes,

        Instant recordedAt
) {
}
