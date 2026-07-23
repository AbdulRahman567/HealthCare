package com.healthcare.hms.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * @deprecated Use {@link RequirePermission} (Phase 3.3 canonical name). Kept as a
 * composed alias so attributes cannot drift from {@link RequirePermission}.
 */
@Deprecated(since = "3.3", forRemoval = false)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@RequirePermission
public @interface RequiresPermission {

    @AliasFor(annotation = RequirePermission.class, attribute = "value")
    String[] value();

    @AliasFor(annotation = RequirePermission.class, attribute = "requireAll")
    boolean requireAll() default false;
}
