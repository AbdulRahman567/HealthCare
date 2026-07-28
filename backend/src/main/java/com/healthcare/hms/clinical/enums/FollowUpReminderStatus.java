package com.healthcare.hms.clinical.enums;

/**
 * Reminder dispatch state for a follow-up plan (future reminder scheduler).
 */
public enum FollowUpReminderStatus {

    /** Eligible for reminder scheduling when {@code next_reminder_at} is reached. */
    PENDING,

    /** Reminder has been sent at least once. */
    SENT,

    /** Reminders disabled or follow-up terminal (completed/cancelled). */
    SKIPPED,

    /** Delivery failed; eligible for retry by future dispatcher. */
    FAILED
}
