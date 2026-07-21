package com.healthcare.hms.common.exception.auth;

/**
 * Raised when login is attempted before the account email has been verified.
 */
public class EmailNotVerifiedException extends AuthenticationException {

    public EmailNotVerifiedException() {
        super(
                "EMAIL_NOT_VERIFIED",
                "Email address is not verified. Please check your inbox or request a new verification link"
        );
    }
}
