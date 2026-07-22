package com.healthcare.hms.tenant.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.exception.InvalidTenantIdentifierException;
import com.healthcare.hms.tenant.exception.TenantNotActiveException;
import com.healthcare.hms.tenant.exception.TenantNotFoundException;
import com.healthcare.hms.tenant.resolution.TenantResolutionService;
import com.healthcare.hms.tenant.validation.TenantValidation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

    @Mock
    private TenantResolutionService tenantResolutionService;

    @Mock
    private TenantValidation tenantValidation;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private TenantFilter filter;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() {
        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        filter = new TenantFilter(tenantResolutionService, tenantValidation, objectMapper);
        responseBody = new ByteArrayOutputStream();
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_bindsContextDuringChainAndClearsAfter() throws Exception {
        final Tenant tenant = AuthTestData.activeTenant();
        final TenantContext context = new TenantContext(
                tenant.getId(), tenant.getSlug(), tenant.getTenantType(), tenant.getStatus());
        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        when(request.getContextPath()).thenReturn("");
        when(tenantResolutionService.resolveTenantId(request)).thenReturn(Optional.of(tenant.getId()));
        when(tenantValidation.validate(tenant.getId())).thenReturn(context);
        authenticateHospitalUser(tenant.getId());

        final AtomicReference<UUID> seenDuringChain = new AtomicReference<>();
        doAnswer(invocation -> {
            seenDuringChain.set(TenantContextHolder.requireTenantId());
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(seenDuringChain.get()).isEqualTo(tenant.getId());
        assertThat(TenantContextHolder.isPresent()).isFalse();
    }

    @Test
    void doFilter_onPublicLogin_bypassesRequiredTenant() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getContextPath()).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        verify(tenantResolutionService, never()).resolveTenantId(any());
        verify(tenantValidation, never()).validate(any());
        verify(filterChain).doFilter(request, response);
        assertThat(TenantContextHolder.isPresent()).isFalse();
    }

    @Test
    void doFilter_onProtectedPathUnauthenticated_skipsTenantAndContinues() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        when(request.getContextPath()).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        verify(tenantResolutionService, never()).resolveTenantId(any());
        verify(tenantValidation, never()).validate(any());
        verify(filterChain).doFilter(request, response);
        assertThat(TenantContextHolder.isPresent()).isFalse();
    }

    @Test
    void doFilter_onProtectedPathAuthenticatedWithoutTenant_returns403() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        when(request.getContextPath()).thenReturn("");
        when(tenantResolutionService.resolveTenantId(request)).thenReturn(Optional.empty());
        stubErrorResponse();

        final AuthenticatedUser principal = new AuthenticatedUser(
                UUID.randomUUID(),
                null,
                "admin@hospital.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of(),
                0L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(responseBody.toString()).contains("TENANT_REQUIRED");
        assertThat(TenantContextHolder.isPresent()).isFalse();
    }

    @Test
    void doFilter_unauthenticatedWithTenantHeader_doesNotValidateOrEnumerate() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        when(request.getContextPath()).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        verify(tenantResolutionService, never()).resolveTenantId(any());
        verify(tenantValidation, never()).validate(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_usesPrincipalTenantWhenHeaderAbsent() throws Exception {
        final Tenant tenant = AuthTestData.activeTenant();
        final TenantContext context = new TenantContext(
                tenant.getId(), tenant.getSlug(), tenant.getTenantType(), tenant.getStatus());
        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        when(request.getContextPath()).thenReturn("");
        when(tenantResolutionService.resolveTenantId(request)).thenReturn(Optional.empty());
        when(tenantValidation.validate(tenant.getId())).thenReturn(context);

        final AuthenticatedUser principal = new AuthenticatedUser(
                UUID.randomUUID(),
                tenant.getId(),
                "admin@hospital.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of(),
                0L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        final AtomicBoolean bound = new AtomicBoolean(false);
        doAnswer(invocation -> {
            bound.set(TenantContextHolder.isPresent());
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(bound.get()).isTrue();
        verify(tenantValidation).validate(tenant.getId());
    }

    @Test
    void doFilter_platformSuperAdminWithoutTenant_continues() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getContextPath()).thenReturn("");
        when(tenantResolutionService.resolveTenantId(request)).thenReturn(Optional.empty());

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

        filter.doFilterInternal(request, response, filterChain);

        verify(tenantValidation, never()).validate(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_nullTenantWithoutSuperAdminRole_returns403() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getContextPath()).thenReturn("");
        when(tenantResolutionService.resolveTenantId(request)).thenReturn(Optional.empty());
        stubErrorResponse();

        final AuthenticatedUser orphan = new AuthenticatedUser(
                UUID.randomUUID(),
                null,
                "orphan@platform.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of(),
                0L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(orphan, null, orphan.getAuthorities())
        );

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(responseBody.toString()).contains("TENANT_REQUIRED");
    }

    @Test
    void doFilter_publicPathIgnoresTenantHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getContextPath()).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        verify(tenantResolutionService, never()).resolveTenantId(any());
        verify(tenantValidation, never()).validate(any());
        verify(filterChain).doFilter(request, response);
        assertThat(TenantContextHolder.isPresent()).isFalse();
    }

    @Test
    void doFilter_whenTenantInactive_returns403AndClears() throws Exception {
        final UUID tenantId = UUID.randomUUID();
        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        when(request.getContextPath()).thenReturn("");
        when(tenantResolutionService.resolveTenantId(request)).thenReturn(Optional.of(tenantId));
        when(tenantValidation.validate(tenantId))
                .thenThrow(new TenantNotActiveException(TenantStatus.SUSPENDED));
        stubErrorResponse();
        authenticateHospitalUser(tenantId);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        assertThat(TenantContextHolder.isPresent()).isFalse();
    }

    @Test
    void doFilter_whenInvalidIdentifier_returns400() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        when(request.getContextPath()).thenReturn("");
        when(tenantResolutionService.resolveTenantId(request))
                .thenThrow(new InvalidTenantIdentifierException("X-Tenant-ID must be a valid UUID"));
        stubErrorResponse();
        authenticateHospitalUser(UUID.randomUUID());

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_whenTenantMissing_returns404() throws Exception {
        final UUID tenantId = UUID.randomUUID();
        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        when(request.getContextPath()).thenReturn("");
        when(tenantResolutionService.resolveTenantId(request)).thenReturn(Optional.of(tenantId));
        when(tenantValidation.validate(tenantId)).thenThrow(new TenantNotFoundException(tenantId));
        stubErrorResponse();
        authenticateHospitalUser(tenantId);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void bypassPaths_matchPublicSurfaces() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register/hospital");
        when(request.getContextPath()).thenReturn("");
        assertThat(TenantBypassPaths.matches(request)).isTrue();

        when(request.getRequestURI()).thenReturn("/api/v1/system/health");
        assertThat(TenantBypassPaths.matches(request)).isTrue();

        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        assertThat(TenantBypassPaths.matches(request)).isTrue();

        when(request.getRequestURI()).thenReturn("/api/v1/patients");
        assertThat(TenantBypassPaths.matches(request)).isFalse();
    }

    private void authenticateHospitalUser(final UUID tenantId) {
        final AuthenticatedUser principal = new AuthenticatedUser(
                UUID.randomUUID(),
                tenantId,
                "admin@hospital.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of(),
                0L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private void stubErrorResponse() throws Exception {
        when(response.isCommitted()).thenReturn(false);
        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(responseBody));
    }
}
