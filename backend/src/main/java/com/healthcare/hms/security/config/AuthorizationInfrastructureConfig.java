package com.healthcare.hms.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Enables AspectJ proxies for permission method authorization
 * ({@link com.healthcare.hms.security.authorization.PermissionAuthorizationAspect}).
 */
@Configuration
@EnableAspectJAutoProxy
public class AuthorizationInfrastructureConfig {
}
