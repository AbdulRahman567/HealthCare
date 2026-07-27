package com.healthcare.hms.appointments.reminder.entity;

import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;
import com.healthcare.hms.appointments.reminder.enums.ReminderStatus;
import com.healthcare.hms.appointments.reminder.enums.ReminderType;
import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Tenant-owned scheduled reminder for an appointment (Phase 6.8).
 *
 * <p>One PENDING row per {@code (appointment, channel, type, leadOffsetMinutes)}.
 * Delivery is performed by channel dispatchers — external SMS/push providers
 * are not integrated yet ({@code providerMessageId} reserved for later).
 */
@Entity
@Table(
        name = "appointment_reminders",
        indexes = {
                @Index(name = "idx_appointment_reminders_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_appointment_reminders_appointment", columnList = "tenant_id, appointment_id"),
                @Index(name = "idx_appointment_reminders_dispatch", columnList = "status, scheduled_at"),
                @Index(
                        name = "idx_appointment_reminders_tenant_status_scheduled",
                        columnList = "tenant_id, status, scheduled_at"),
                @Index(name = "idx_appointment_reminders_channel_status", columnList = "tenant_id, channel, status"),
                @Index(name = "idx_appointment_reminders_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class AppointmentReminder extends TenantOwnedEntity {

    @NotNull
    @Column(name = "appointment_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID appointmentId;

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 40)
    private ReminderType reminderType = ReminderType.APPOINTMENT_UPCOMING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private ReminderChannel channel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReminderStatus status = ReminderStatus.PENDING;

    /**
     * How many minutes before the appointment slot this reminder should fire.
     */
    @NotNull
    @Min(0)
    @Column(name = "lead_offset_minutes", nullable = false, updatable = false)
    private Integer leadOffsetMinutes;

    @NotNull
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @NotNull
    @Min(0)
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @NotNull
    @Min(1)
    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    /**
     * Channel destination snapshot (email or phone). Null for PUSH until device registry exists.
     */
    @Size(max = 255)
    @Column(name = "recipient", length = 255)
    private String recipient;

    /**
     * External provider id once SMS/push/email vendors are integrated.
     */
    @Size(max = 100)
    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    @Size(max = 500)
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(final UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(final UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(final ReminderType reminderType) {
        this.reminderType = reminderType;
    }

    public ReminderChannel getChannel() {
        return channel;
    }

    public void setChannel(final ReminderChannel channel) {
        this.channel = channel;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(final ReminderStatus status) {
        this.status = status;
    }

    public Integer getLeadOffsetMinutes() {
        return leadOffsetMinutes;
    }

    public void setLeadOffsetMinutes(final Integer leadOffsetMinutes) {
        this.leadOffsetMinutes = leadOffsetMinutes;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(final Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(final Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(final Instant lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(final Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(final Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(final String recipient) {
        this.recipient = recipient;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(final String providerMessageId) {
        this.providerMessageId = providerMessageId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(final String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isPending() {
        return status == ReminderStatus.PENDING;
    }

    public void markSent(final String providerMessageId) {
        this.status = ReminderStatus.SENT;
        this.sentAt = Instant.now();
        this.lastAttemptAt = this.sentAt;
        this.attemptCount = this.attemptCount == null ? 1 : this.attemptCount + 1;
        this.providerMessageId = providerMessageId;
        this.failureReason = null;
    }

    public void markSkipped(final String reason) {
        this.status = ReminderStatus.SKIPPED;
        this.lastAttemptAt = Instant.now();
        this.failureReason = truncate(reason, 500);
    }

    public void markCancelled(final String reason) {
        this.status = ReminderStatus.CANCELLED;
        this.lastAttemptAt = Instant.now();
        this.failureReason = truncate(reason, 500);
    }

    /**
     * Records a failed attempt. Moves to {@link ReminderStatus#FAILED} when attempts are exhausted.
     */
    public void markAttemptFailed(final String reason) {
        this.lastAttemptAt = Instant.now();
        this.attemptCount = this.attemptCount == null ? 1 : this.attemptCount + 1;
        this.failureReason = truncate(reason, 500);
        if (this.attemptCount >= this.maxAttempts) {
            this.status = ReminderStatus.FAILED;
        }
    }

    private static String truncate(final String value, final int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
