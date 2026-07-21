package com.healthcare.hms.common.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extracts client metadata from HTTP requests for audit logging.
 */
public final class ClientRequestDetails {

    private ClientRequestDetails() {
    }

    public static String resolveClientIp(final HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        final String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            final int commaIndex = forwardedFor.indexOf(',');
            return commaIndex > 0
                    ? forwardedFor.substring(0, commaIndex).trim()
                    : forwardedFor.trim();
        }
        return request.getRemoteAddr();
    }

    public static String resolveUserAgent(final HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }
}
