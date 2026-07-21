package com.healthcare.hms.common.exception;

/**
 * Raised for domain rule violations that are not validation annotation failures.
 */
public class BusinessException extends ApplicationException {

    public BusinessException(final String errorCode, final String message) {
        super(errorCode, message);
    }

    public BusinessException(final String message) {
        super("BUSINESS_RULE_VIOLATION", message);
    }
}
