package com.healthcare.hms.appointments.reminder.channel;

/**
 * Outcome of a single channel dispatch attempt.
 *
 * @param success            whether the channel accepted the message
 * @param retryable          if failed, whether another attempt should be scheduled
 * @param providerMessageId  optional external id (null until providers are wired)
 * @param detail             human-readable success/failure detail (no PHI)
 */
public record ReminderDispatchResult(
        boolean success,
        boolean retryable,
        String providerMessageId,
        String detail
) {

    public static ReminderDispatchResult ok(final String providerMessageId, final String detail) {
        return new ReminderDispatchResult(true, false, providerMessageId, detail);
    }

    public static ReminderDispatchResult skipped(final String detail) {
        return new ReminderDispatchResult(false, false, null, detail);
    }

    public static ReminderDispatchResult failed(final boolean retryable, final String detail) {
        return new ReminderDispatchResult(false, retryable, null, detail);
    }
}
