package com.healthcare.hms.security.resolver;

import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.annotation.CurrentUser;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.security.util.SecurityUtils;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link CurrentUser}-annotated controller parameters from the security context.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            @NonNull final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            @NonNull final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        final CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        final boolean required = annotation == null || annotation.required();

        return SecurityUtils.findCurrentUser()
                .orElseGet(() -> {
                    if (required) {
                        throw new UnauthorizedException();
                    }
                    return null;
                });
    }
}
