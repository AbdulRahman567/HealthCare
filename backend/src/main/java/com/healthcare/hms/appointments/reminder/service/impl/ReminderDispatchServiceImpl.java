package com.healthcare.hms.appointments.reminder.service.impl;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.reminder.channel.ReminderChannelDispatcher;
import com.healthcare.hms.appointments.reminder.channel.ReminderDispatchResult;
import com.healthcare.hms.appointments.reminder.config.AppointmentReminderProperties;
import com.healthcare.hms.appointments.reminder.entity.AppointmentReminder;
import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;
import com.healthcare.hms.appointments.reminder.enums.ReminderStatus;
import com.healthcare.hms.appointments.reminder.repository.AppointmentReminderRepository;
import com.healthcare.hms.appointments.reminder.service.ReminderDispatchService;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderDispatchServiceImpl implements ReminderDispatchService {

    private static final Logger log = LoggerFactory.getLogger(ReminderDispatchServiceImpl.class);
    private static final String ENTITY = "APPOINTMENT_REMINDER";

    private final AppointmentReminderRepository reminderRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentReminderProperties properties;
    private final AuditLogService auditLogService;
    private final Map<ReminderChannel, ReminderChannelDispatcher> dispatchers;

    public ReminderDispatchServiceImpl(
            final AppointmentReminderRepository reminderRepository,
            final AppointmentRepository appointmentRepository,
            final AppointmentReminderProperties properties,
            final AuditLogService auditLogService,
            final List<ReminderChannelDispatcher> channelDispatchers
    ) {
        this.reminderRepository = reminderRepository;
        this.appointmentRepository = appointmentRepository;
        this.properties = properties;
        this.auditLogService = auditLogService;
        this.dispatchers = new EnumMap<>(ReminderChannel.class);
        for (final ReminderChannelDispatcher dispatcher : channelDispatchers) {
            this.dispatchers.put(dispatcher.channel(), dispatcher);
        }
    }

    @Override
    @Transactional
    public int dispatchDueBatch() {
        if (!properties.isEnabled()) {
            return 0;
        }

        final List<AppointmentReminder> due = reminderRepository.findDueForDispatch(
                ReminderStatus.PENDING,
                Instant.now(),
                PageRequest.of(0, properties.getBatchSize())
        );

        int processed = 0;
        for (final AppointmentReminder reminder : due) {
            processOne(reminder);
            processed++;
        }
        return processed;
    }

    private void processOne(final AppointmentReminder reminder) {
        final String old = snapshot(reminder);
        final Appointment appointment = appointmentRepository
                .findByIdAndTenantId(reminder.getAppointmentId(), reminder.getTenantId())
                .orElse(null);

        if (appointment == null) {
            reminder.markSkipped("Appointment not found");
            reminderRepository.save(reminder);
            audit(reminder, AuditAction.REMINDER_SKIPPED, old);
            return;
        }

        if (!appointment.isBookableSlot()) {
            reminder.markSkipped("Appointment status is not bookable: " + appointment.getStatus());
            reminderRepository.save(reminder);
            audit(reminder, AuditAction.REMINDER_SKIPPED, old);
            return;
        }

        final ReminderChannelDispatcher dispatcher = dispatchers.get(reminder.getChannel());
        if (dispatcher == null) {
            reminder.markAttemptFailed("No dispatcher registered for channel " + reminder.getChannel());
            reminderRepository.save(reminder);
            audit(reminder, AuditAction.REMINDER_FAILED, old);
            return;
        }

        // Missing recipient → skip (patient may add contact later; we do not retry forever).
        if (reminder.getChannel() != ReminderChannel.PUSH
                && (reminder.getRecipient() == null || reminder.getRecipient().isBlank())) {
            reminder.markSkipped("Missing recipient for channel " + reminder.getChannel());
            reminderRepository.save(reminder);
            audit(reminder, AuditAction.REMINDER_SKIPPED, old);
            return;
        }

        final ReminderDispatchResult result = dispatcher.dispatch(reminder, appointment);
        if (result.success()) {
            reminder.markSent(result.providerMessageId());
            reminderRepository.save(reminder);
            audit(reminder, AuditAction.REMINDER_SENT, old);
            log.info(
                    "Reminder sent reminderId={} appointmentId={} channel={}",
                    reminder.getId(),
                    reminder.getAppointmentId(),
                    reminder.getChannel()
            );
            return;
        }

        if (!result.retryable()) {
            reminder.markSkipped(result.detail());
            reminderRepository.save(reminder);
            audit(reminder, AuditAction.REMINDER_SKIPPED, old);
            return;
        }

        reminder.markAttemptFailed(result.detail());
        reminderRepository.save(reminder);
        audit(
                reminder,
                reminder.getStatus() == ReminderStatus.FAILED
                        ? AuditAction.REMINDER_FAILED
                        : AuditAction.UPDATE,
                old
        );
        log.warn(
                "Reminder dispatch failed reminderId={} appointmentId={} channel={} status={}",
                reminder.getId(),
                reminder.getAppointmentId(),
                reminder.getChannel(),
                reminder.getStatus()
        );
    }

    private void audit(final AppointmentReminder reminder, final AuditAction action, final String oldSnapshot) {
        auditLogService.record(
                reminder.getTenantId(),
                null,
                ENTITY,
                reminder.getId().toString(),
                action,
                oldSnapshot,
                snapshot(reminder),
                null,
                null
        );
    }

    private static String snapshot(final AppointmentReminder reminder) {
        return "{id=%s, appointmentId=%s, channel=%s, status=%s, attemptCount=%s}"
                .formatted(
                        reminder.getId(),
                        reminder.getAppointmentId(),
                        reminder.getChannel(),
                        reminder.getStatus(),
                        reminder.getAttemptCount()
                );
    }
}
