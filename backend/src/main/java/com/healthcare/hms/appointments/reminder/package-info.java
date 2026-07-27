/**
 * Appointment reminder infrastructure (Phase 6.8).
 *
 * <p>Schedules multi-channel reminders (EMAIL / SMS / PUSH) ahead of appointment
 * slots, tracks delivery status, and dispatches due rows through channel ports.
 * External SMS/push providers are <strong>not</strong> integrated — SMS and PUSH
 * use logging stubs; EMAIL reuses {@link com.healthcare.hms.common.email.EmailSender}.
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link com.healthcare.hms.appointments.reminder.entity.AppointmentReminder}</li>
 *   <li>{@link com.healthcare.hms.appointments.reminder.service.AppointmentReminderService}</li>
 *   <li>{@link com.healthcare.hms.appointments.reminder.service.ReminderDispatchService}</li>
 *   <li>{@link com.healthcare.hms.appointments.reminder.channel.ReminderChannelDispatcher}</li>
 *   <li>{@link com.healthcare.hms.appointments.reminder.scheduling.ReminderDispatchScheduler}</li>
 * </ul>
 *
 * <p>Flyway: {@code V30__appointment_reminders.sql}. Config prefix: {@code hms.reminders.*}.
 */
package com.healthcare.hms.appointments.reminder;
