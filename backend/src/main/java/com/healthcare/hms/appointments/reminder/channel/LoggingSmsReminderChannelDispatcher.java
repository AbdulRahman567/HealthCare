package com.healthcare.hms.appointments.reminder.channel;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.reminder.entity.AppointmentReminder;
import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SMS channel stub — logs intent only until an SMS provider is integrated.
 */
@Component
public class LoggingSmsReminderChannelDispatcher implements ReminderChannelDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsReminderChannelDispatcher.class);

    @Override
    public ReminderChannel channel() {
        return ReminderChannel.SMS;
    }

    @Override
    public ReminderDispatchResult dispatch(final AppointmentReminder reminder, final Appointment appointment) {
        if (!StringUtils.hasText(reminder.getRecipient())) {
            return ReminderDispatchResult.skipped("No SMS recipient on reminder");
        }

        log.info(
                "SMS reminder stub (provider not integrated) reminderId={} appointmentId={} number={}",
                reminder.getId(),
                appointment.getId(),
                appointment.getAppointmentNumber()
        );
        // Treat as successful hand-off to the stub so status tracking can be exercised end-to-end
        // without claiming a real carrier delivery. Replace with Twilio/etc. later.
        return ReminderDispatchResult.ok(
                "sms-stub-" + reminder.getId(),
                "Logged SMS reminder; external SMS provider not configured"
        );
    }
}
