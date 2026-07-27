package com.healthcare.hms.appointments.reminder.channel;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.reminder.entity.AppointmentReminder;
import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;

/**
 * Future-ready delivery port for a single reminder channel (Phase 6.8).
 *
 * <p>Implementations must not throw for "provider not configured" — return a
 * failed {@link ReminderDispatchResult} so status tracking stays consistent.
 * External SMS/push vendors are intentionally not integrated yet.
 */
public interface ReminderChannelDispatcher {

    ReminderChannel channel();

    /**
     * Attempts delivery for the given reminder + appointment context.
     */
    ReminderDispatchResult dispatch(AppointmentReminder reminder, Appointment appointment);
}
