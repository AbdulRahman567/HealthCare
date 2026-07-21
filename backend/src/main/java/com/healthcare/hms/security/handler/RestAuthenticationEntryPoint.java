package com.healthcare.hms.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hms.common.api.ApiErrorResponse;
import com.healthcare.hms.common.exception.auth.AuthenticationException;
import com.healthcare.hms.security.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Returns a consistent JSON body when an unauthenticated request hits a protected endpoint.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final org.springframework.security.core.AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        final Object attribute = request.getAttribute(SecurityConstants.AUTH_EXCEPTION_ATTRIBUTE);
        final String message;
        final String errorCode;

        if (attribute instanceof AuthenticationException applicationAuthException) {
            message = applicationAuthException.getMessage();
            errorCode = applicationAuthException.getErrorCode();
        } else {
            message = "Authentication is required to access this resource";
            errorCode = "AUTH_UNAUTHORIZED";
        }

        final ApiErrorResponse body = ApiErrorResponse.of(message, errorCode, request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
