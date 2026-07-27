package com.healthcare.hms.appointments.reminder.service;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.hospitals.entity.Hospital;

/**
 * Booking lifecycle hooks for reminder scheduling (create / reschedule / cancel).
 *
 * <p>Keeps {@link com.healthcare.hms.appointments.service.impl.AppointmentServiceImpl}
 * free of reminder persistence details.
 */
public interface AppointmentReminderLifecycle {

    void onAppointmentBooked(Appointment appointment, Hospital hospital);

    void onAppointmentRescheduled(Appointment appointment, Hospital hospital);

    void onAppointmentCancelled(Appointment appointment);
}
