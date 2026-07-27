package com.healthcare.hms.appointments.reminder.config;

import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Appointment reminder feature flags and scheduling defaults (Phase 6.8).
 */
@Validated
@ConfigurationProperties(prefix = "hms.reminders")
public class AppointmentReminderProperties {

    /**
     * When false, lifecycle hooks do not create reminder rows.
     */
    private boolean enabled = true;

    /**
     * When true, Spring {@code @Scheduled} dispatch job runs.
     */
    private boolean schedulerEnabled = false;

    /**
     * Cron for due-reminder dispatch (default: every 5 minutes).
     */
    @NotNull
    private String dispatchCron = "0 */5 * * * *";

    @Min(1)
    private int batchSize = 50;

    @Min(1)
    private int maxAttempts = 3;

    /**
     * How far ahead of the appointment slot to schedule reminders (e.g. 24h, 1h).
     */
    @NotEmpty
    private List<Duration> leadTimes = new ArrayList<>(List.of(Duration.ofHours(24), Duration.ofHours(1)));

    /**
     * Channels to schedule for each appointment. Dispatchers exist for all three;
     * SMS/PUSH are logging stubs until providers are wired.
     */
    @NotEmpty
    private List<ReminderChannel> channels = new ArrayList<>(List.of(ReminderChannel.EMAIL));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void setSchedulerEnabled(final boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }

    public String getDispatchCron() {
        return dispatchCron;
    }

    public void setDispatchCron(final String dispatchCron) {
        this.dispatchCron = dispatchCron;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(final int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public List<Duration> getLeadTimes() {
        return leadTimes;
    }

    public void setLeadTimes(final List<Duration> leadTimes) {
        this.leadTimes = leadTimes;
    }

    public List<ReminderChannel> getChannels() {
        return channels;
    }

    public void setChannels(final List<ReminderChannel> channels) {
        this.channels = channels;
    }
}
