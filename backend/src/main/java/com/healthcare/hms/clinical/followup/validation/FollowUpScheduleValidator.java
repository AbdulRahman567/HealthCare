package com.healthcare.hms.clinical.followup.validation;

import com.healthcare.hms.clinical.followup.dto.request.CreateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.request.UpdateFollowUpRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Cross-field validator for follow-up scheduling dates on create/update DTOs.
 */
public class FollowUpScheduleValidator implements ConstraintValidator<ValidFollowUpSchedule, Object> {

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof CreateFollowUpRequest request) {
            return FollowUpClinicalRules.isScheduledDateValid(request.scheduledDate());
        }
        if (value instanceof UpdateFollowUpRequest request) {
            return FollowUpClinicalRules.isScheduledDateValid(request.scheduledDate());
        }
        return true;
    }
}
