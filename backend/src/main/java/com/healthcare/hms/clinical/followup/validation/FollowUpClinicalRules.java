package com.healthcare.hms.clinical.followup.validation;

import java.time.LocalDate;

/**
 * Domain rules for consultation follow-up plans.
 */
public final class FollowUpClinicalRules {

    private FollowUpClinicalRules() {
    }

    public static boolean isScheduledDateValid(final LocalDate scheduledDate) {
        if (scheduledDate == null) {
            return true;
        }
        return !scheduledDate.isBefore(LocalDate.now());
    }
}
