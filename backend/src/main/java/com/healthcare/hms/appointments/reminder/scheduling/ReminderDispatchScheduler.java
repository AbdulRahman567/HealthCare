package com.healthcare.hms.appointments.reminder.scheduling;

/**
 * Background dispatch contract for due appointment reminders (Phase 6.8).
 *
 * <p>Implementations may be Spring {@code @Scheduled}, a message-queue consumer,
 * or a manual admin trigger. External provider SDKs are out of scope.
 */
public interface ReminderDispatchScheduler {

    /**
     * Processes a batch of due {@code PENDING} reminders.
     *
     * @return number of reminders attempted in this run
     */
    int dispatchDueReminders();
}
