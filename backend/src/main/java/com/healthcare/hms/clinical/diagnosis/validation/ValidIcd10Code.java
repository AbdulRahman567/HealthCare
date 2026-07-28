package com.healthcare.hms.clinical.diagnosis.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates optional ICD-10-CM code format when supplied.
 */
@Documented
@Constraint(validatedBy = Icd10CodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIcd10Code {

    String message() default "ICD-10 code must match pattern e.g. A00 or A00.0";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
