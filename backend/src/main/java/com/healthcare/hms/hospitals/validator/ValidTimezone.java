package com.healthcare.hms.hospitals.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a value is a supported IANA timezone id (e.g. {@code Asia/Karachi}).
 */
@Documented
@Constraint(validatedBy = ValidTimezoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimezone {

    String message() default "Timezone must be a valid IANA timezone id";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
