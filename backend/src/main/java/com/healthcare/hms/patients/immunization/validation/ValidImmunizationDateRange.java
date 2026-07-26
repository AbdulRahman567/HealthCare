package com.healthcare.hms.patients.immunization.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures {@code nextDueDate} is not before {@code administrationDate} when both are set.
 */
@Documented
@Constraint(validatedBy = ImmunizationDateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImmunizationDateRange {

    String message() default "Next due date must be on or after administration date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
