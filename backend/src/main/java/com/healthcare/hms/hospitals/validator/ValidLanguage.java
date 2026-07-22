package com.healthcare.hms.hospitals.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates BCP 47 / ISO 639 language tags (e.g. {@code en}, {@code en-US}, {@code ur}).
 */
@Documented
@Constraint(validatedBy = ValidLanguageValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLanguage {

    String message() default "Language must be a valid language tag (e.g. en, en-US)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
