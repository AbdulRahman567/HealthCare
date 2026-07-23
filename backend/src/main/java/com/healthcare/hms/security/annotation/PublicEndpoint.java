package com.healthcare.hms.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an API handler as anonymously accessible (no JWT / tenant header required).
 *
 * <p>Must remain aligned with {@link com.healthcare.hms.security.SecurityConstants#PUBLIC_ENDPOINTS}.
 * Used by OpenAPI customization so Swagger documents public operations without Bearer auth.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicEndpoint {
}
