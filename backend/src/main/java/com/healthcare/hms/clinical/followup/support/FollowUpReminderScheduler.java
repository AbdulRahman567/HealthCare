package com.healthcare.hms.clinical.followup.support;

import com.healthcare.hms.clinical.enums.FollowUpReminderStatus;
import com.healthcare.hms.clinical.entity.FollowUp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

/**
 * Computes follow-up reminder schedule fields for a future dispatcher.
 */
@Component
public class FollowUpReminderScheduler {

    public void refreshSchedule(final FollowUp followUp) {
        if (followUp.getReminderEnabled() == null) {
            followUp.setReminderEnabled(true);
        }
        if (followUp.getReminderLeadDays() == null) {
            followUp.setReminderLeadDays(1);
        }

        if (!Boolean.TRUE.equals(followUp.getReminderEnabled()) || !followUp.isOpen()) {
            followUp.setNextReminderAt(null);
            if (!followUp.isOpen()) {
                followUp.setReminderStatus(FollowUpReminderStatus.SKIPPED);
            }
            return;
        }

        final LocalDate remindOn = followUp.getScheduledDate().minusDays(followUp.getReminderLeadDays());
        final LocalTime time = followUp.getScheduledTime() != null ? followUp.getScheduledTime() : LocalTime.of(9, 0);
        final Instant nextAt = remindOn.atTime(time).toInstant(ZoneOffset.UTC);

        followUp.setNextReminderAt(nextAt);
        if (followUp.getReminderStatus() == null
                || followUp.getReminderStatus() == FollowUpReminderStatus.SKIPPED) {
            followUp.setReminderStatus(FollowUpReminderStatus.PENDING);
        }
    }
}
