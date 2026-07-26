package com.healthcare.hms.patients.history.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures {@code recoveryDate} is not before {@code diagnosisDate} when both are set.
 */
@Documented
@Constraint(validatedBy = ClinicalDateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidClinicalDateRange {

    String message() default "Recovery date must be on or after diagnosis date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
