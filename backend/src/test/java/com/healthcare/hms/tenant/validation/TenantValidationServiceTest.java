package com.healthcare.hms.tenant.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.exception.TenantMismatchException;
import com.healthcare.hms.tenant.service.TenantAccessService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TenantValidationServiceTest {

    @Mock
    private TenantAccessService tenantAccessService;

    private TenantValidationService validation;

    @BeforeEach
    void setUp() {
        validation = new TenantValidationService(tenantAccessService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validate_whenActive_returnsContext() {
        final Tenant tenant = AuthTestData.activeTenant();
        final TenantContext context = new TenantContext(
                tenant.getId(), tenant.getSlug(), tenant.getTenantType(), tenant.getStatus());
        when(tenantAccessService.requireActiveTenant(tenant.getId())).thenReturn(tenant);
        when(tenantAccessService.toContext(tenant)).thenReturn(context);

        assertThat(validation.validate(tenant.getId())).isEqualTo(context);
    }

    @Test
    void validate_whenPrincipalMismatches_throwsBeforeLookup() {
        final UUID headerTenant = UUID.randomUUID();
        final UUID jwtTenant = UUID.randomUUID();
        final AuthenticatedUser principal = new AuthenticatedUser(
                UUID.randomUUID(),
                jwtTenant,
                "admin@hospital.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of(),
                0L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertThatThrownBy(() -> validation.validate(headerTenant))
                .isInstanceOf(TenantMismatchException.class);
        verifyNoInteractions(tenantAccessService);
    }

    @Test
    void validate_whenNullTenantNonSuperAdminUsesHeader_throwsBeforeLookup() {
        final UUID headerTenant = UUID.randomUUID();
        final AuthenticatedUser principal = new AuthenticatedUser(
                UUID.randomUUID(),
                null,
                "orphan@platform.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("HOSPITAL_UPDATE"),
                0L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertThatThrownBy(() -> validation.validate(headerTenant))
                .isInstanceOf(TenantMismatchException.class);
        verifyNoInteractions(tenantAccessService);
    }

    @Test
    void validate_whenSuperAdminUsesHeader_allowsBinding() {
        final Tenant tenant = AuthTestData.activeTenant();
        final TenantContext context = new TenantContext(
                tenant.getId(), tenant.getSlug(), tenant.getTenantType(), tenant.getStatus());
        when(tenantAccessService.requireActiveTenant(tenant.getId())).thenReturn(tenant);
        when(tenantAccessService.toContext(tenant)).thenReturn(context);

        final AuthenticatedUser superAdmin = new AuthenticatedUser(
                UUID.randomUUID(),
                null,
                "super@platform.test",
                Set.of("SUPER_ADMIN"),
                Set.of(),
                0L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(superAdmin, null, superAdmin.getAuthorities())
        );

        assertThat(validation.validate(tenant.getId())).isEqualTo(context);
    }
}
