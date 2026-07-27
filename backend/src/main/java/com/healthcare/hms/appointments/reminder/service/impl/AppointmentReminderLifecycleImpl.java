package com.healthcare.hms.appointments.reminder.service.impl;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.reminder.config.AppointmentReminderProperties;
import com.healthcare.hms.appointments.reminder.service.AppointmentReminderLifecycle;
import com.healthcare.hms.appointments.reminder.service.AppointmentReminderService;
import com.healthcare.hms.hospitals.entity.Hospital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AppointmentReminderLifecycleImpl implements AppointmentReminderLifecycle {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderLifecycleImpl.class);

    private final AppointmentReminderService reminderService;
    private final AppointmentReminderProperties properties;

    public AppointmentReminderLifecycleImpl(
            final AppointmentReminderService reminderService,
            final AppointmentReminderProperties properties
    ) {
        this.reminderService = reminderService;
        this.properties = properties;
    }

    @Override
    public void onAppointmentBooked(final Appointment appointment, final Hospital hospital) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            reminderService.scheduleForAppointment(appointment, hospital);
        } catch (final Exception ex) {
            // Reminder failure must not roll back booking — log and continue.
            log.error(
                    "Failed to schedule reminders for appointmentId={}",
                    appointment.getId(),
                    ex
            );
        }
    }

    @Override
    public void onAppointmentRescheduled(final Appointment appointment, final Hospital hospital) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            reminderService.rescheduleForAppointment(appointment, hospital);
        } catch (final Exception ex) {
            log.error(
                    "Failed to reschedule reminders for appointmentId={}",
                    appointment.getId(),
                    ex
            );
        }
    }

    @Override
    public void onAppointmentCancelled(final Appointment appointment) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            reminderService.cancelPendingForAppointment(
                    appointment.getTenantId(),
                    appointment.getId(),
                    "Appointment cancelled"
            );
        } catch (final Exception ex) {
            log.error(
                    "Failed to cancel reminders for appointmentId={}",
                    appointment.getId(),
                    ex
            );
        }
    }
}
