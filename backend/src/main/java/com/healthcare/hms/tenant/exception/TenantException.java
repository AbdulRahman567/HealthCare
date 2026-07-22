package com.healthcare.hms.tenant.exception;

import com.healthcare.hms.common.exception.ApplicationException;

/**
 * Base type for all tenant middleware and domain tenancy failures.
 */
public abstract class TenantException extends ApplicationException {

    protected TenantException(final String errorCode, final String message) {
        super(errorCode, message);
    }

    protected TenantException(final String errorCode, final String message, final Throwable cause) {
        super(errorCode, message, cause);
    }
}
