package com.healthcare.hms.clinical.vitals.validation;

import com.healthcare.hms.clinical.vitals.dto.request.RecordVitalSignsRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Cross-field clinical validation for new vital-signs recordings.
 */
public class VitalSignsMeasurementValidator implements ConstraintValidator<ValidVitalSignsMeasurement, RecordVitalSignsRequest> {

    @Override
    public boolean isValid(final RecordVitalSignsRequest request, final ConstraintValidatorContext context) {
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
        )) {
            addViolation(context, "At least one vital sign measurement is required");
            valid = false;
        }

        if (!VitalSignsClinicalRules.isBloodPressurePairValid(request.systolicBp(), request.diastolicBp())) {
            addViolation(context, "Systolic blood pressure must be greater than diastolic when both are provided");
            valid = false;
        }

        if (!VitalSignsClinicalRules.isBloodPressureComplete(request.systolicBp(), request.diastolicBp())) {
            addViolation(context, "Blood pressure requires both systolic and diastolic values");
            valid = false;
        }

        return valid;
    }

    private static void addViolation(final ConstraintValidatorContext context, final String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
