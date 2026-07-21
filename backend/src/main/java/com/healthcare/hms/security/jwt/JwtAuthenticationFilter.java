package com.healthcare.hms.security.jwt;

import com.healthcare.hms.common.exception.auth.AuthenticationException;
import com.healthcare.hms.security.SecurityConstants;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts and validates Bearer access tokens, then populates the SecurityContext.
 * Performs cryptographic JWT validation plus principal state checks.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final JwtPrincipalValidator jwtPrincipalValidator;

    public JwtAuthenticationFilter(
            final JwtService jwtService,
            final JwtPrincipalValidator jwtPrincipalValidator
    ) {
        this.jwtService = jwtService;
        this.jwtPrincipalValidator = jwtPrincipalValidator;
    }

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authorizationHeader.substring(SecurityConstants.BEARER_PREFIX.length()).trim();

        if (!StringUtils.hasText(token) || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final JwtClaims claims = jwtService.parseAccessToken(token);
            final AuthenticatedUser principal = jwtPrincipalValidator.validateAndBuildPrincipal(claims);

            final UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (final AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            request.setAttribute(SecurityConstants.AUTH_EXCEPTION_ATTRIBUTE, exception);
            log.debug("JWT authentication failed: {}", exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
