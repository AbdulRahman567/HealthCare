package com.healthcare.hms.appointments.reminder.channel;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.reminder.entity.AppointmentReminder;
import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;
import com.healthcare.hms.common.email.EmailDeliveryException;
import com.healthcare.hms.common.email.EmailMessage;
import com.healthcare.hms.common.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Email reminder channel using the shared {@link EmailSender} abstraction.
 *
 * <p>Does not introduce a new email vendor — SMTP vs logging is already selected
 * by {@code hms.mail.enabled}.
 */
@Component
public class EmailReminderChannelDispatcher implements ReminderChannelDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EmailReminderChannelDispatcher.class);

    private final EmailSender emailSender;

    public EmailReminderChannelDispatcher(final EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public ReminderChannel channel() {
        return ReminderChannel.EMAIL;
    }

    @Override
    public ReminderDispatchResult dispatch(final AppointmentReminder reminder, final Appointment appointment) {
        if (!StringUtils.hasText(reminder.getRecipient())) {
            return ReminderDispatchResult.skipped("No email recipient on reminder");
        }

        final String subject = "Appointment reminder — " + appointment.getAppointmentNumber();
        final String textBody = buildTextBody(appointment);
        final String htmlBody = buildHtmlBody(appointment);

        try {
            emailSender.send(new EmailMessage(reminder.getRecipient().trim(), subject, textBody, htmlBody));
            log.info(
                    "Appointment reminder email accepted reminderId={} appointmentId={} channel=EMAIL",
                    reminder.getId(),
                    appointment.getId()
            );
            return ReminderDispatchResult.ok(null, "Email accepted by EmailSender");
        } catch (final EmailDeliveryException ex) {
            log.warn(
                    "Appointment reminder email failed reminderId={} appointmentId={}",
                    reminder.getId(),
                    appointment.getId()
            );
            return ReminderDispatchResult.failed(true, "Email delivery failed");
        }
    }

    private static String buildTextBody(final Appointment appointment) {
        return """
                Reminder: you have an appointment scheduled.

                Appointment: %s
                Date: %s
                Time: %s – %s

                Please arrive a few minutes early. Contact the hospital if you need to reschedule.
                """.formatted(
                appointment.getAppointmentNumber(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime()
        );
    }

    private static String buildHtmlBody(final Appointment appointment) {
        return """
                <p>Reminder: you have an appointment scheduled.</p>
                <ul>
                  <li><strong>Appointment:</strong> %s</li>
                  <li><strong>Date:</strong> %s</li>
                  <li><strong>Time:</strong> %s – %s</li>
                </ul>
                <p>Please arrive a few minutes early. Contact the hospital if you need to reschedule.</p>
                """.formatted(
                appointment.getAppointmentNumber(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime()
        );
    }
}
