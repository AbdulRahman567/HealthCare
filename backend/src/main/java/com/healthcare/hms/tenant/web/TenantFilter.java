package com.healthcare.hms.tenant.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hms.common.api.ApiErrorResponse;
import com.healthcare.hms.security.authorization.PlatformPrincipalSupport;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.exception.InvalidTenantIdentifierException;
import com.healthcare.hms.tenant.exception.TenantException;
import com.healthcare.hms.tenant.exception.TenantMismatchException;
import com.healthcare.hms.tenant.exception.TenantNotActiveException;
import com.healthcare.hms.tenant.exception.TenantNotFoundException;
import com.healthcare.hms.tenant.exception.TenantRequiredException;
import com.healthcare.hms.tenant.resolution.TenantResolutionService;
import com.healthcare.hms.tenant.validation.TenantValidation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Tenant middleware (Phase 2.3).
 *
 * <pre>
 * Authentication (JWT filter)
 *   → Tenant Resolution
 *   → Tenant Validation
 *   → TenantContextHolder.set
 *   → Controller / Service / Repository
 *   → finally TenantContextHolder.clear
 * </pre>
 *
 * <p>Public endpoints (login, hospital register, health, swagger, …) bypass
 * tenant resolution entirely — {@code X-Tenant-ID} is ignored on those paths to
 * prevent unauthenticated tenant enumeration. An MVC interceptor is intentionally
 * not used; this filter already runs once per request after authentication.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    private final TenantResolutionService tenantResolutionService;
    private final TenantValidation tenantValidation;
    private final ObjectMapper objectMapper;

    public TenantFilter(
            final TenantResolutionService tenantResolutionService,
            final TenantValidation tenantValidation,
            final ObjectMapper objectMapper
    ) {
        this.tenantResolutionService = tenantResolutionService;
        this.tenantValidation = tenantValidation;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            bindTenantContext(request);
            filterChain.doFilter(request, response);
        } catch (final InvalidTenantIdentifierException exception) {
            writeError(request, response, HttpStatus.BAD_REQUEST, exception);
        } catch (final TenantNotFoundException exception) {
            writeError(request, response, HttpStatus.NOT_FOUND, exception);
        } catch (final TenantNotActiveException
                 | TenantMismatchException
                 | TenantRequiredException exception) {
            writeError(request, response, HttpStatus.FORBIDDEN, exception);
        } catch (final TenantException exception) {
            writeError(request, response, HttpStatus.BAD_REQUEST, exception);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private void bindTenantContext(final HttpServletRequest request) {
        // Public surfaces ignore X-Tenant-ID entirely — validating here would enable
        // unauthenticated tenant enumeration (404 vs 403) and could poison bootstrap writes.
        if (TenantBypassPaths.matches(request)) {
            return;
        }

        // Unauthenticated traffic must reach Spring Security for 401. Binding or validating
        // tenant here would (1) mask AUTH_UNAUTHORIZED as TENANT_REQUIRED and (2) enable
        // anonymous tenant existence probes via 404/403 differences.
        if (!isAuthenticatedHospitalPrincipal()) {
            return;
        }

        final Optional<UUID> resolved = resolveTenantId(request);
        if (resolved.isEmpty()) {
            if (isPlatformSuperAdmin()) {
                return;
            }
            throw new TenantRequiredException();
        }

        final TenantContext context = tenantValidation.validate(resolved.get());
        TenantContextHolder.set(context);
    }

    private boolean isAuthenticatedHospitalPrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUser;
    }

    private Optional<UUID> resolveTenantId(final HttpServletRequest request) {
        final Optional<UUID> fromResolvers = tenantResolutionService.resolveTenantId(request);
        if (fromResolvers.isPresent()) {
            return fromResolvers;
        }
        return principalTenantId();
    }

    private Optional<UUID> principalTenantId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.ofNullable(user.getTenantId());
    }

    /**
     * Platform Super Admin may omit tenant context. Delegates to
     * {@link PlatformPrincipalSupport} so the trust bar cannot drift from validation.
     */
    private boolean isPlatformSuperAdmin() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return false;
        }
        return PlatformPrincipalSupport.isPlatformSuperAdmin(user);
    }

    private void writeError(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final HttpStatus status,
            final TenantException exception
    ) throws IOException {
        if (response.isCommitted()) {
            log.warn(
                    "Tenant middleware failed after response commit path={} code={}",
                    request.getRequestURI(),
                    exception.getErrorCode()
            );
            return;
        }

        log.debug(
                "Tenant middleware rejected path={} code={}",
                request.getRequestURI(),
                exception.getErrorCode()
        );

        response.resetBuffer();
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.of(exception.getMessage(), exception.getErrorCode(), request.getRequestURI())
        );
    }
}
