package com.healthcare.hms.config;

import com.healthcare.hms.security.SecurityConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation with Bearer JWT and tenant header schemes for protected endpoints.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";
    private static final String TENANT_HEADER_SCHEME = "tenantHeader";

    @Bean
    public OpenAPI hmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Healthcare Management System API")
                        .description("""
                                Enterprise-grade HMS backend APIs.

                                Multi-tenant requests require a JWT Bearer token and, for hospital-scoped
                                principals, an optional confirming `%s` header (UUID). When omitted, the
                                principal's JWT tenant is used. Header and principal tenant must match.
                                """.formatted(SecurityConstants.TENANT_HEADER))
                        .version("v1")
                        .contact(new Contact()
                                .name("HMS Engineering")
                                .email("engineering@hms.local"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://hms.local/license")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(BEARER_AUTH_SCHEME)
                        .addList(TENANT_HEADER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token. Format: Bearer {token}"))
                        .addSecuritySchemes(TENANT_HEADER_SCHEME, new SecurityScheme()
                                .name(SecurityConstants.TENANT_HEADER)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description(
                                        "Tenant UUID. Optional when the JWT already carries tenant_id; "
                                                + "required confirmation must match the authenticated principal.")));
    }
}
