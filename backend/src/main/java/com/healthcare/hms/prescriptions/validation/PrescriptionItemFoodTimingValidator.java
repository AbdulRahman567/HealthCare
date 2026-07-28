package com.healthcare.hms.prescriptions.validation;

import com.healthcare.hms.prescriptions.dto.request.PrescriptionItemRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PrescriptionItemFoodTimingValidator
        implements ConstraintValidator<ValidPrescriptionItemFoodTiming, PrescriptionItemRequest> {

    @Override
    public boolean isValid(final PrescriptionItemRequest value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return PrescriptionClinicalRules.isFoodTimingValid(value.beforeFood(), value.afterFood());
    }
}
