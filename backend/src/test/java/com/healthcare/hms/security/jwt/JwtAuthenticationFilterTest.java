package com.healthcare.hms.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.security.SecurityConstants;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtPrincipalValidator jwtPrincipalValidator;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, jwtPrincipalValidator);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("continues without authentication when Authorization header is missing")
    void noAuthorizationHeader() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).parseAccessToken(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("continues when Authorization header is not Bearer")
    void nonBearerHeader() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).parseAccessToken(any());
    }

    @Test
    @DisplayName("continues when Bearer token is blank")
    void blankBearerToken() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + "   ");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).parseAccessToken(any());
    }

    @Test
    @DisplayName("skips JWT parsing when SecurityContext already has authentication")
    void alreadyAuthenticated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null, Set.of()));

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + "token");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).parseAccessToken(any());
    }

    @Test
    @DisplayName("sets SecurityContext when access token is valid")
    void validTokenAuthenticates() throws Exception {
        final JwtClaims claims = mock(JwtClaims.class);
        final AuthenticatedUser principal = new AuthenticatedUser(
                AuthTestData.userId(),
                AuthTestData.tenantId(),
                "admin@hospital.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("HOSPITAL_READ"),
                0L
        );
        when(jwtService.parseAccessToken("good-token")).thenReturn(claims);
        when(jwtPrincipalValidator.validateAndBuildPrincipal(claims)).thenReturn(principal);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + "good-token");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(principal);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("clears context and stores auth exception when JWT validation fails")
    void invalidTokenClearsContext() throws Exception {
        when(jwtService.parseAccessToken("bad-token"))
                .thenThrow(new InvalidTokenException("invalid"));

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + "bad-token");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getAttribute(SecurityConstants.AUTH_EXCEPTION_ATTRIBUTE))
                .isInstanceOf(InvalidTokenException.class);
        verify(filterChain).doFilter(request, response);
    }
}
