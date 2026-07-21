package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.annotation.RequiresPermission;
import com.healthcare.hms.security.annotation.RequiresRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Permission middleware that enforces {@link RequiresPermission} and {@link RequiresRole}
 * on matched controller handlers before the method executes.
 */
@Component
public class PermissionAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthorizationService authorizationService;

    public PermissionAuthorizationInterceptor(final AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public boolean preHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        final RequiresPermission methodPermission =
                AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequiresPermission.class);
        final RequiresPermission typePermission =
                AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequiresPermission.class);
        final RequiresRole methodRole =
                AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequiresRole.class);
        final RequiresRole typeRole =
                AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequiresRole.class);

        if (methodPermission == null && typePermission == null && methodRole == null && typeRole == null) {
            return true;
        }

        authorizationService.requireAuthenticated();
        enforcePermission(methodPermission != null ? methodPermission : typePermission);
        enforceRole(methodRole != null ? methodRole : typeRole);
        return true;
    }

    private void enforcePermission(final RequiresPermission annotation) {
        if (annotation == null || annotation.value().length == 0) {
            return;
        }
        if (annotation.requireAll()) {
            authorizationService.requireAllPermissions(annotation.value());
        } else {
            authorizationService.requireAnyPermission(annotation.value());
        }
    }

    private void enforceRole(final RequiresRole annotation) {
        if (annotation == null || annotation.value().length == 0) {
            return;
        }
        if (annotation.requireAll()) {
            authorizationService.requireAllRoles(annotation.value());
        } else {
            authorizationService.requireAnyRole(annotation.value());
        }
    }
}
