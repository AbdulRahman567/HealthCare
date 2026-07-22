package com.healthcare.hms.hospitals.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates ISO 4217 currency codes (e.g. {@code USD}, {@code PKR}).
 */
@Documented
@Constraint(validatedBy = ValidCurrencyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {

    String message() default "Currency must be a valid ISO 4217 code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
