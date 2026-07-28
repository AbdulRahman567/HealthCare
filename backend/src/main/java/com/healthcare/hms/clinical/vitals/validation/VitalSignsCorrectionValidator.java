package com.healthcare.hms.clinical.vitals.validation;

import com.healthcare.hms.clinical.vitals.dto.request.UpdateVitalSignsRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Cross-field clinical validation for vital-signs corrections.
 */
public class VitalSignsCorrectionValidator implements ConstraintValidator<ValidVitalSignsCorrection, UpdateVitalSignsRequest> {

    @Override
    public boolean isValid(final UpdateVitalSignsRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        if (!VitalSignsClinicalRules.hasAnyMeasurement(
                request.temperatureCelsius(),
                request.heartRateBpm(),
                request.systolicBp(),
                request.diastolicBp(),
                request.respiratoryRate(),
                request.oxygenSaturationPercent(),
                request.heightCm(),
                request.weightKg(),
                request.painScale()
        ) && request.notes() == null && request.recordedAt() == null) {
            addViolation(context, "At least one field must be provided for correction");
            valid = false;
        }

        if (!VitalSignsClinicalRules.isBloodPressurePairValid(request.systolicBp(), request.diastolicBp())) {
            addViolation(context, "Systolic blood pressure must be greater than diastolic when both are provided");
            valid = false;
        }

        return valid;
    }

    private static void addViolation(final ConstraintValidatorContext context, final String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
