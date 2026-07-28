package com.healthcare.hms.clinical.entity;

import com.healthcare.hms.clinical.enums.FollowUpPriority;
import com.healthcare.hms.clinical.enums.FollowUpReminderStatus;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Follow-up plan originating from a {@link Consultation}.
 *
 * <p>Supports clinical recommendations, priority/status lifecycle, optional appointment
 * linkage, and reminder-ready fields for a future dispatcher (Phase 7.7).
 */
@Entity
@Table(
        name = "consultation_follow_ups",
        indexes = {
                @Index(name = "idx_consultation_follow_ups_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_consultation_follow_ups_consultation", columnList = "tenant_id, consultation_id"),
                @Index(name = "idx_consultation_follow_ups_patient", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_consultation_follow_ups_doctor", columnList = "tenant_id, doctor_id"),
                @Index(name = "idx_consultation_follow_ups_scheduled", columnList = "tenant_id, scheduled_date"),
                @Index(name = "idx_consultation_follow_ups_status", columnList = "tenant_id, status, scheduled_date"),
                @Index(name = "idx_consultation_follow_ups_appointment", columnList = "tenant_id, follow_up_appointment_id"),
                @Index(name = "idx_consultation_follow_ups_reminder_dispatch", columnList = "tenant_id, reminder_status, next_reminder_at"),
                @Index(name = "idx_consultation_follow_ups_doctor_due", columnList = "tenant_id, doctor_id, status, scheduled_date"),
                @Index(name = "idx_consultation_follow_ups_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class FollowUp extends TenantOwnedEntity {

    @NotNull
    @Column(name = "consultation_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID consultationId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotNull
    @Column(name = "doctor_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID doctorId;

    @NotNull
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FollowUpStatus status = FollowUpStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private FollowUpPriority priority = FollowUpPriority.ROUTINE;

    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;

    @Size(max = 1000)
    @Column(name = "instructions", length = 1000)
    private String instructions;

    /** Clinician recommendations for the return visit (care plan guidance). */
    @Size(max = 2000)
    @Column(name = "clinical_recommendations", length = 2000)
    private String clinicalRecommendations;

    /**
     * Linked appointment when the follow-up is formally scheduled (Phase 6 integration).
     */
    @Column(name = "follow_up_appointment_id", columnDefinition = "CHAR(36)")
    private UUID followUpAppointmentId;

    /** When true, future reminder dispatcher may schedule notifications. */
    @NotNull
    @Column(name = "reminder_enabled", nullable = false)
    private Boolean reminderEnabled = true;

    /** Days before {@code scheduled_date} to fire the first reminder. */
    @NotNull
    @Min(0)
    @Column(name = "reminder_lead_days", nullable = false)
    private Integer reminderLeadDays = 1;

    /** Next eligible reminder fire time (UTC). Null when disabled or terminal. */
    @Column(name = "next_reminder_at")
    private Instant nextReminderAt;

    @Column(name = "last_reminder_at")
    private Instant lastReminderAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_status", nullable = false, length = 20)
    private FollowUpReminderStatus reminderStatus = FollowUpReminderStatus.PENDING;

    public UUID getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(final UUID consultationId) {
        this.consultationId = consultationId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(final UUID doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(final LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(final LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public FollowUpStatus getStatus() {
        return status;
    }

    public void setStatus(final FollowUpStatus status) {
        this.status = status;
    }

    public FollowUpPriority getPriority() {
        return priority;
    }

    public void setPriority(final FollowUpPriority priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(final String instructions) {
        this.instructions = instructions;
    }

    public String getClinicalRecommendations() {
        return clinicalRecommendations;
    }

    public void setClinicalRecommendations(final String clinicalRecommendations) {
        this.clinicalRecommendations = clinicalRecommendations;
    }

    public UUID getFollowUpAppointmentId() {
        return followUpAppointmentId;
    }

    public void setFollowUpAppointmentId(final UUID followUpAppointmentId) {
        this.followUpAppointmentId = followUpAppointmentId;
    }

    public Boolean getReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(final Boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public Integer getReminderLeadDays() {
        return reminderLeadDays;
    }

    public void setReminderLeadDays(final Integer reminderLeadDays) {
        this.reminderLeadDays = reminderLeadDays;
    }

    public Instant getNextReminderAt() {
        return nextReminderAt;
    }

    public void setNextReminderAt(final Instant nextReminderAt) {
        this.nextReminderAt = nextReminderAt;
    }

    public Instant getLastReminderAt() {
        return lastReminderAt;
    }

    public void setLastReminderAt(final Instant lastReminderAt) {
        this.lastReminderAt = lastReminderAt;
    }

    public FollowUpReminderStatus getReminderStatus() {
        return reminderStatus;
    }

    public void setReminderStatus(final FollowUpReminderStatus reminderStatus) {
        this.reminderStatus = reminderStatus;
    }

    public boolean isOpen() {
        return status == FollowUpStatus.PENDING || status == FollowUpStatus.SCHEDULED;
    }
}
