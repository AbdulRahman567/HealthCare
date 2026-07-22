package com.healthcare.hms.tenant.web;

import com.healthcare.hms.security.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;

/**
 * Paths that bypass mandatory tenant resolution/validation (Phase 2.3).
 *
 * <p>Aligned with public auth, health, and API docs surfaces. Authenticated
 * platform Super Admin traffic (null tenant on the principal) is handled
 * separately inside {@link TenantFilter}.
 */
public final class TenantBypassPaths {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * Endpoints that must not require {@code X-Tenant-ID}.
     * Same reference as {@link SecurityConstants#PUBLIC_ENDPOINTS} — keep auth permitAll
     * and tenant bypass in lockstep; do not diverge these arrays.
     */
    public static final String[] PATTERNS = SecurityConstants.PUBLIC_ENDPOINTS;

    private TenantBypassPaths() {
    }

    public static boolean matches(final HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        final String path = request.getRequestURI();
        final String contextPath = request.getContextPath();
        final String relative = (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath))
                ? path.substring(contextPath.length())
                : path;

        for (final String pattern : PATTERNS) {
            if (PATH_MATCHER.match(pattern, relative)) {
                return true;
            }
        }
        return false;
    }
}
