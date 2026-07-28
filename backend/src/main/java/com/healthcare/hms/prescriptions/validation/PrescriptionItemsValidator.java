package com.healthcare.hms.prescriptions.validation;

import com.healthcare.hms.prescriptions.dto.request.CreatePrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.request.PrescriptionItemRequest;
import com.healthcare.hms.prescriptions.dto.request.UpdatePrescriptionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class PrescriptionItemsValidator implements ConstraintValidator<ValidPrescriptionItems, Object> {

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        final List<PrescriptionItemRequest> items;
        if (value instanceof CreatePrescriptionRequest request) {
            items = request.items();
        } else if (value instanceof UpdatePrescriptionRequest request) {
            items = request.items();
            if (items == null) {
                return true;
            }
            if (items.isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("At least one prescription item is required")
                        .addPropertyNode("items")
                        .addConstraintViolation();
                return false;
            }
        } else {
            return true;
        }
        if (items == null || items.isEmpty()) {
            return true;
        }
        final boolean duplicates = PrescriptionClinicalRules.hasDuplicateMedicines(
                items.stream().map(PrescriptionItemRequest::medicineName).toList()
        );
        if (duplicates) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Duplicate medicines are not allowed on the same prescription")
                    .addPropertyNode("items")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
