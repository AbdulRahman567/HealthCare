package com.healthcare.hms.appointments.reminder.service.impl;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.reminder.config.AppointmentReminderProperties;
import com.healthcare.hms.appointments.reminder.entity.AppointmentReminder;
import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;
import com.healthcare.hms.appointments.reminder.enums.ReminderStatus;
import com.healthcare.hms.appointments.reminder.enums.ReminderType;
import com.healthcare.hms.appointments.reminder.repository.AppointmentReminderRepository;
import com.healthcare.hms.appointments.reminder.service.AppointmentReminderService;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.service.PatientQueryService;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AppointmentReminderServiceImpl implements AppointmentReminderService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderServiceImpl.class);
    private static final String ENTITY = "APPOINTMENT_REMINDER";

    private final AppointmentReminderRepository reminderRepository;
    private final AppointmentReminderProperties properties;
    private final PatientQueryService patientQueryService;
    private final AuditLogService auditLogService;

    public AppointmentReminderServiceImpl(
            final AppointmentReminderRepository reminderRepository,
            final AppointmentReminderProperties properties,
            final PatientQueryService patientQueryService,
            final AuditLogService auditLogService
    ) {
        this.reminderRepository = reminderRepository;
        this.properties = properties;
        this.patientQueryService = patientQueryService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<AppointmentReminder> scheduleForAppointment(
            final Appointment appointment,
            final Hospital hospital
    ) {
        return scheduleInCurrentTransaction(appointment, hospital);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int cancelPendingForAppointment(
            final UUID tenantId,
            final UUID appointmentId,
            final String reason
    ) {
        final List<AppointmentReminder> pending = reminderRepository.findByTenantIdAndAppointmentIdAndStatusIn(
                tenantId,
                appointmentId,
                List.of(ReminderStatus.PENDING)
        );
        for (final AppointmentReminder reminder : pending) {
            final String old = snapshot(reminder);
            reminder.markCancelled(reason);
            reminderRepository.save(reminder);
            audit(reminder, AuditAction.REMINDER_CANCELLED, old);
        }
        if (!pending.isEmpty()) {
            log.info(
                    "Cancelled pending reminders count={} appointmentId={} tenantId={}",
                    pending.size(),
                    appointmentId,
                    tenantId
            );
        }
        return pending.size();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<AppointmentReminder> rescheduleForAppointment(
            final Appointment appointment,
            final Hospital hospital
    ) {
        // Nested REQUIRES_NEW calls via self-invocation would skip proxy — cancel then schedule inline.
        final List<AppointmentReminder> pending = reminderRepository.findByTenantIdAndAppointmentIdAndStatusIn(
                appointment.getTenantId(),
                appointment.getId(),
                List.of(ReminderStatus.PENDING)
        );
        for (final AppointmentReminder reminder : pending) {
            final String old = snapshot(reminder);
            reminder.markCancelled("Appointment rescheduled");
            reminderRepository.save(reminder);
            audit(reminder, AuditAction.REMINDER_CANCELLED, old);
        }
        return scheduleInCurrentTransaction(appointment, hospital);
    }

    /**
     * Shared create path used by schedule (new TX) and reschedule (same TX after cancel).
     */
    private List<AppointmentReminder> scheduleInCurrentTransaction(
            final Appointment appointment,
            final Hospital hospital
    ) {
        if (!properties.isEnabled()) {
            return List.of();
        }
        if (!appointment.isBookableSlot()) {
            return List.of();
        }

        final Patient patient = patientQueryService.requireById(
                appointment.getTenantId(),
                appointment.getPatientId()
        );
        final ZoneId zone = resolveZone(hospital);
        final Instant appointmentInstant = LocalDateTime
                .of(appointment.getAppointmentDate(), appointment.getStartTime())
                .atZone(zone)
                .toInstant();

        final List<AppointmentReminder> created = new ArrayList<>();
        for (final ReminderChannel channel : properties.getChannels()) {
            for (final Duration lead : properties.getLeadTimes()) {
                final int leadMinutes = Math.toIntExact(lead.toMinutes());
                if (reminderRepository.existsByTenantIdAndAppointmentIdAndChannelAndReminderTypeAndLeadOffsetMinutesAndStatus(
                        appointment.getTenantId(),
                        appointment.getId(),
                        channel,
                        ReminderType.APPOINTMENT_UPCOMING,
                        leadMinutes,
                        ReminderStatus.PENDING
                )) {
                    continue;
                }

                final Instant scheduledAt = appointmentInstant.minus(lead);
                if (!scheduledAt.isAfter(Instant.now())) {
                    continue;
                }

                final AppointmentReminder reminder = new AppointmentReminder();
                reminder.setTenantId(appointment.getTenantId());
                reminder.setAppointmentId(appointment.getId());
                reminder.setHospitalId(appointment.getHospitalId());
                reminder.setPatientId(appointment.getPatientId());
                reminder.setReminderType(ReminderType.APPOINTMENT_UPCOMING);
                reminder.setChannel(channel);
                reminder.setStatus(ReminderStatus.PENDING);
                reminder.setLeadOffsetMinutes(leadMinutes);
                reminder.setScheduledAt(scheduledAt);
                reminder.setMaxAttempts(properties.getMaxAttempts());
                reminder.setRecipient(resolveRecipient(channel, patient));
                reminder.setAttemptCount(0);

                final AppointmentReminder saved = reminderRepository.save(reminder);
                created.add(saved);
                audit(saved, AuditAction.REMINDER_SCHEDULED, null);
            }
        }

        if (!created.isEmpty()) {
            log.info(
                    "Scheduled appointment reminders count={} appointmentId={} tenantId={}",
                    created.size(),
                    appointment.getId(),
                    appointment.getTenantId()
            );
        }
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReminder> listForAppointment(final UUID tenantId, final UUID appointmentId) {
        return reminderRepository.findByTenantIdAndAppointmentIdOrderByScheduledAtAsc(tenantId, appointmentId);
    }

    private static String resolveRecipient(final ReminderChannel channel, final Patient patient) {
        return switch (channel) {
            case EMAIL -> blankToNull(patient.getEmail());
            case SMS -> blankToNull(patient.getPhone());
            case PUSH -> null;
        };
    }

    private static String blankToNull(final String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static ZoneId resolveZone(final Hospital hospital) {
        try {
            return ZoneId.of(hospital.getTimezone());
        } catch (final Exception ex) {
            return ZoneId.of("UTC");
        }
    }

    private void audit(final AppointmentReminder reminder, final AuditAction action, final String oldSnapshot) {
        UUID actorId = null;
        try {
            actorId = SecurityUtils.requireCurrentUser().getUserId();
        } catch (final Exception ignored) {
            // Background dispatch / system flows may have no authenticated user.
        }
        final UUID tenantId = reminder.getTenantId() != null
                ? reminder.getTenantId()
                : TenantContextHolder.getTenantId().orElse(null);
        if (tenantId == null) {
            return;
        }
        auditLogService.record(
                tenantId,
                actorId,
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
        return "{id=%s, appointmentId=%s, channel=%s, status=%s, scheduledAt=%s, leadOffsetMinutes=%s}"
                .formatted(
                        reminder.getId(),
                        reminder.getAppointmentId(),
                        reminder.getChannel(),
                        reminder.getStatus(),
                        reminder.getScheduledAt(),
                        reminder.getLeadOffsetMinutes()
                );
    }
}
