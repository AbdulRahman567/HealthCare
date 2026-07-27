package com.healthcare.hms.appointments.reminder.service;

/**
 * Dispatches due reminders through channel ports and updates status tracking.
 */
public interface ReminderDispatchService {

    /**
     * Loads a batch of due PENDING reminders and attempts delivery.
     *
     * @return number of reminders processed (any terminal or retry outcome)
     */
    int dispatchDueBatch();
}
