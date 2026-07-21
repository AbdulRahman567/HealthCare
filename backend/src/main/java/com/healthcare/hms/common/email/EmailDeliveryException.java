package com.healthcare.hms.common.email;

/**
 * Raised when outbound email delivery fails.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
