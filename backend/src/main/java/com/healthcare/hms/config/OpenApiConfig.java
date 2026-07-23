package com.healthcare.hms.config;

import com.healthcare.hms.security.SecurityConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation with Bearer JWT and tenant header schemes for protected endpoints.
 *
 * <p>Per-operation security and authorization metadata are enriched by
 * {@link AuthorizationOpenApiCustomizer} (Phase 3.4).
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

                                ## Authorization model (Phase 3.4)

                                Every protected endpoint verifies:

                                1. **JWT** — Bearer access token; principal is loaded from the database
                                   (claim drift fails closed).
                                2. **Role** — principal role set from RBAC grants.
                                3. **Permission** — declarative `@RequirePermission` / authenticated-only
                                   self-service via `@RequireAuthenticated`.
                                4. **Tenant** — hospital-scoped requests must match principal tenant;
                                   optional confirming `%s` header must equal JWT `tenant_id`.

                                ### HTTP status codes

                                | Code | Meaning |
                                | ---- | ------- |
                                | 401 | Unauthenticated (missing/invalid JWT or credential failure) |
                                | 403 | Forbidden (missing permission/role or tenant mismatch) |

                                Error bodies use a generic message and never expose stack traces,
                                password hashes, raw tokens, or permission-code diagnostics.

                                ### Public endpoints

                                Anonymous access is limited to the allow-list in
                                `SecurityConstants.PUBLIC_ENDPOINTS` (login, hospital register,
                                token refresh, password/email recovery, health, OpenAPI UI).
                                Those operations clear Bearer security in this document.
                                """.formatted(SecurityConstants.TENANT_HEADER))
                        .version("v1")
                        .contact(new Contact()
                                .name("HMS Engineering")
                                .email("engineering@hms.local"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://hms.local/license")))
                .tags(List.of(
                        new Tag().name("Authentication")
                                .description("Public session APIs and authenticated profile/self-service"),
                        new Tag().name("Hospital Registration")
                                .description("Public hospital onboarding"),
                        new Tag().name("Hospital Settings")
                                .description("Protected tenant hospital settings (HOSPITAL_READ / HOSPITAL_UPDATE)"),
                        new Tag().name("System")
                                .description("Public health probes")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList(BEARER_AUTH_SCHEME)
                        .addList(TENANT_HEADER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT access token issued by POST /api/v1/auth/login.
                                        Format: `Bearer {token}`.
                                        Validated against DB user status, token version, roles, and permissions.
                                        """))
                        .addSecuritySchemes(TENANT_HEADER_SCHEME, new SecurityScheme()
                                .name(SecurityConstants.TENANT_HEADER)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("""
                                        Tenant UUID. Optional when the JWT already carries `tenant_id`;
                                        when present it must match the authenticated principal.
                                        Ignored on public (anonymous) endpoints.
                                        """)));
    }
}
