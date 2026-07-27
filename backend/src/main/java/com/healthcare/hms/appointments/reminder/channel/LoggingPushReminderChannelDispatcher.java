package com.healthcare.hms.appointments.reminder.channel;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.reminder.entity.AppointmentReminder;
import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Push notification channel stub — logs intent only until FCM/APNs (or similar) is integrated.
 */
@Component
public class LoggingPushReminderChannelDispatcher implements ReminderChannelDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggingPushReminderChannelDispatcher.class);

    @Override
    public ReminderChannel channel() {
        return ReminderChannel.PUSH;
    }

    @Override
    public ReminderDispatchResult dispatch(final AppointmentReminder reminder, final Appointment appointment) {
        log.info(
                "Push reminder stub (provider not integrated) reminderId={} appointmentId={} number={}",
                reminder.getId(),
                appointment.getId(),
                appointment.getAppointmentNumber()
        );
        return ReminderDispatchResult.ok(
                "push-stub-" + reminder.getId(),
                "Logged push reminder; device push provider not configured"
        );
    }
}
