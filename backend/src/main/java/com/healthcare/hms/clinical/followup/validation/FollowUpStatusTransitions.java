package com.healthcare.hms.clinical.followup.validation;

import com.healthcare.hms.clinical.enums.FollowUpStatus;
import java.util.EnumSet;
import java.util.Set;

/**
 * Status transition rules for follow-up plans.
 */
public final class FollowUpStatusTransitions {

    private static final Set<FollowUpStatus> TERMINAL = EnumSet.of(
            FollowUpStatus.COMPLETED,
            FollowUpStatus.CANCELLED,
            FollowUpStatus.MISSED
    );

    private FollowUpStatusTransitions() {
    }

    public static boolean canTransition(final FollowUpStatus from, final FollowUpStatus to) {
        if (from == null || to == null || from == to) {
            return from == to;
        }
        if (TERMINAL.contains(from)) {
            return false;
        }
        return switch (from) {
            case PENDING -> to == FollowUpStatus.SCHEDULED
                    || to == FollowUpStatus.COMPLETED
                    || to == FollowUpStatus.CANCELLED
                    || to == FollowUpStatus.MISSED;
            case SCHEDULED -> to == FollowUpStatus.COMPLETED
                    || to == FollowUpStatus.CANCELLED
                    || to == FollowUpStatus.MISSED
                    || to == FollowUpStatus.PENDING;
            default -> false;
        };
    }

    public static boolean isTerminal(final FollowUpStatus status) {
        return status != null && TERMINAL.contains(status);
    }
}
