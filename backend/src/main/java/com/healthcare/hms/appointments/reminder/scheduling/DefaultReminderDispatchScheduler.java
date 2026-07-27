package com.healthcare.hms.appointments.reminder.scheduling;

import com.healthcare.hms.appointments.reminder.service.ReminderDispatchService;
import org.springframework.stereotype.Component;

/**
 * Default background scheduler adapter — delegates to {@link ReminderDispatchService}.
 *
 * <p>Triggered by Spring {@code @Scheduled} when {@code hms.reminders.scheduler-enabled=true},
 * or callable from tests / future queue consumers.
 */
@Component
public class DefaultReminderDispatchScheduler implements ReminderDispatchScheduler {

    private final ReminderDispatchService reminderDispatchService;

    public DefaultReminderDispatchScheduler(final ReminderDispatchService reminderDispatchService) {
        this.reminderDispatchService = reminderDispatchService;
    }

    @Override
    public int dispatchDueReminders() {
        return reminderDispatchService.dispatchDueBatch();
    }
}
