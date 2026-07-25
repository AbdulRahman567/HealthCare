package com.healthcare.hms.users.enums;

/**
 * Lifecycle of a hospital user invitation.
 *
 * <pre>
 * PENDING → ACCEPTED | REJECTED | CANCELLED
 * PENDING → EXPIRED (when past expires_at; may still be resent while PENDING)
 * </pre>
 */
public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    EXPIRED
}
