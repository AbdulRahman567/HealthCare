package com.healthcare.hms.appointments.reminder.enums;

/**
 * Delivery channel for an appointment reminder (Phase 6.8).
 *
 * <p>Providers are not integrated yet — channel dispatchers log or use the
 * existing {@link com.healthcare.hms.common.email.EmailSender} abstraction.
 */
public enum ReminderChannel {

    EMAIL,
    SMS,
    PUSH
}
