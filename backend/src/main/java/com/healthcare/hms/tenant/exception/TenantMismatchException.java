package com.healthcare.hms.tenant.exception;

/**
 * Raised when a tenant id conflicts with the authenticated principal or request context.
 */
public class TenantMismatchException extends TenantException {

    public TenantMismatchException(final String message) {
        super("TENANT_HEADER_MISMATCH", message);
    }

    private TenantMismatchException(final String errorCode, final String message) {
        super(errorCode, message);
    }

    public static TenantMismatchException headerDoesNotMatchPrincipal() {
        return new TenantMismatchException("X-Tenant-ID does not match the authenticated tenant");
    }

    /**
     * Entity {@code tenant_id} does not match the bound {@code TenantContext}.
     */
    public static TenantMismatchException persistenceTenantMismatch() {
        return new TenantMismatchException(
                "TENANT_PERSISTENCE_MISMATCH",
                "Entity tenant does not match the current tenant context"
        );
    }

    /**
     * A tenant-scoped session attempted to mutate a platform row ({@code tenant_id} null).
     */
    public static TenantMismatchException persistencePlatformRowMutation() {
        return new TenantMismatchException(
                "TENANT_PLATFORM_ROW_MUTATION",
                "Platform-scoped rows cannot be modified under a tenant context"
        );
    }
}
