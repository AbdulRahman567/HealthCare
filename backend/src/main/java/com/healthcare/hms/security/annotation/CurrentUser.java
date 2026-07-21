package com.healthcare.hms.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the authenticated {@link com.healthcare.hms.security.principal.AuthenticatedUser}
 * into controller method parameters via {@link com.healthcare.hms.security.resolver.CurrentUserArgumentResolver}.
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {

    /**
     * When true (default), missing authentication raises an unauthorized error.
     */
    boolean required() default true;
}
