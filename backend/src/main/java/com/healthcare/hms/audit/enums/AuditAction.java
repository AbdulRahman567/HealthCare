package com.healthcare.hms.audit.enums;

/**
 * Canonical audit actions recorded for security and compliance events.
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    PASSWORD_CHANGE,
    PASSWORD_RESET_REQUEST,
    PASSWORD_RESET,
    EMAIL_VERIFICATION_REQUEST,
    EMAIL_VERIFIED,
    DOWNLOAD,
    UPLOAD
}
