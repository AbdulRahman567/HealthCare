package com.healthcare.hms.common.exception.auth;

/**
 * Raised when an authenticated account is not allowed to access the system.
 */
public class AccountNotActiveException extends AuthenticationException {

    public AccountNotActiveException() {
        super("ACCOUNT_NOT_ACTIVE", "Account is not active");
    }
}
