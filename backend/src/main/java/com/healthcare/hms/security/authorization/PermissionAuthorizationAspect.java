package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.annotation.RequiresRole;
import com.healthcare.hms.security.authorization.PermissionAnnotationSupport.PermissionRequirement;
import java.lang.reflect.Method;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * Method-level permission authorization for non-controller Spring beans (services, etc.).
 *
 * <p>Controllers are intentionally skipped — HTTP handlers are enforced by
 * {@link PermissionAuthorizationInterceptor} to avoid double checks.
 *
 * <p>Fails closed via {@link PermissionGuard} → {@link AuthorizationService}.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class PermissionAuthorizationAspect {

    private final PermissionGuard permissionGuard;

    public PermissionAuthorizationAspect(final PermissionGuard permissionGuard) {
        this.permissionGuard = permissionGuard;
    }

    @Around(
            "@annotation(com.healthcare.hms.security.annotation.RequirePermission) || "
                    + "@annotation(com.healthcare.hms.security.annotation.RequiresPermission) || "
                    + "@within(com.healthcare.hms.security.annotation.RequirePermission) || "
                    + "@within(com.healthcare.hms.security.annotation.RequiresPermission) || "
                    + "@annotation(com.healthcare.hms.security.annotation.RequiresRole) || "
                    + "@within(com.healthcare.hms.security.annotation.RequiresRole) || "
                    + "@annotation(com.healthcare.hms.security.annotation.RequireAuthenticated) || "
                    + "@within(com.healthcare.hms.security.annotation.RequireAuthenticated)"
    )
    public Object authorize(final ProceedingJoinPoint joinPoint) throws Throwable {
        final Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        if (isWebController(targetClass)) {
            return joinPoint.proceed();
        }

        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), targetClass);

        final Optional<PermissionRequirement> permission =
                PermissionAnnotationSupport.findPermissionRequirement(method, targetClass);
        final Optional<RequiresRole> role =
                PermissionAnnotationSupport.findRoleRequirement(method, targetClass);
        final boolean authenticatedOnly =
                PermissionAnnotationSupport.requiresAuthenticatedOnly(method, targetClass);

        if (permission.isEmpty() && role.isEmpty() && !authenticatedOnly) {
            return joinPoint.proceed();
        }

        permissionGuard.requireAuthenticated();
        permission.ifPresent(permissionGuard::enforce);
        role.ifPresent(permissionGuard::enforce);

        return joinPoint.proceed();
    }

    private static boolean isWebController(final Class<?> type) {
        return AnnotationUtils.findAnnotation(type, RestController.class) != null
                || AnnotationUtils.findAnnotation(type, Controller.class) != null;
    }
}
