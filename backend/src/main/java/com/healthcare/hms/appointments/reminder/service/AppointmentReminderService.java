package com.healthcare.hms.appointments.reminder.service;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.reminder.entity.AppointmentReminder;
import com.healthcare.hms.hospitals.entity.Hospital;
import java.util.List;
import java.util.UUID;

/**
 * Schedules and cancels appointment reminders; tracks status in the database.
 */
public interface AppointmentReminderService {

    /**
     * Creates PENDING reminders for configured channels and lead times.
     * Idempotent for an existing PENDING slot of the same channel/type/lead.
     */
    List<AppointmentReminder> scheduleForAppointment(Appointment appointment, Hospital hospital);

    /**
     * Cancels all PENDING reminders for the appointment (e.g. on cancel/reschedule).
     */
    int cancelPendingForAppointment(UUID tenantId, UUID appointmentId, String reason);

    /**
     * Replaces PENDING reminders after a slot change (cancel then schedule).
     */
    List<AppointmentReminder> rescheduleForAppointment(Appointment appointment, Hospital hospital);

    List<AppointmentReminder> listForAppointment(UUID tenantId, UUID appointmentId);
}
