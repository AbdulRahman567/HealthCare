package com.healthcare.hms.security.config;

import com.healthcare.hms.security.authorization.PermissionAuthorizationInterceptor;
import com.healthcare.hms.security.resolver.CurrentUserArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers authorization infrastructure with Spring MVC.
 */
@Configuration
public class WebMvcSecurityConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final PermissionAuthorizationInterceptor permissionAuthorizationInterceptor;

    public WebMvcSecurityConfig(
            final CurrentUserArgumentResolver currentUserArgumentResolver,
            final PermissionAuthorizationInterceptor permissionAuthorizationInterceptor
    ) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.permissionAuthorizationInterceptor = permissionAuthorizationInterceptor;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(permissionAuthorizationInterceptor)
                .addPathPatterns("/api/**");
    }
}
