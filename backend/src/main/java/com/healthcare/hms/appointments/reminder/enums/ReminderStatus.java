package com.healthcare.hms.appointments.reminder.enums;

/**
 * Lifecycle status of a scheduled appointment reminder.
 *
 * <pre>
 * PENDING → SENT
 * PENDING → FAILED   (retries until maxAttempts, then terminal FAILED)
 * PENDING → CANCELLED (appointment cancelled / rescheduled)
 * PENDING → SKIPPED   (missing recipient / appointment no longer bookable)
 * </pre>
 */
public enum ReminderStatus {

    /** Waiting for {@code scheduledAt}; eligible for dispatch. */
    PENDING,

    /** Successfully handed to the channel dispatcher. */
    SENT,

    /** Exhausted attempts or non-retryable channel failure. */
    FAILED,

    /** Superseded because the appointment was cancelled or rescheduled. */
    CANCELLED,

    /** Intentionally not sent (e.g. no email/phone on file). */
    SKIPPED
}
