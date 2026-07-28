package com.healthcare.hms.clinical.followup.timeline;

import com.healthcare.hms.clinical.entity.FollowUp;
import com.healthcare.hms.clinical.enums.FollowUpPriority;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import com.healthcare.hms.clinical.repository.FollowUpRepository;
import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSeverityHint;
import com.healthcare.hms.patients.timeline.spi.TimelineEventProvider;
import com.healthcare.hms.patients.timeline.support.TimelineSummaries;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Follow-up contributions to the patient chronological timeline (Phase 7.7).
 *
 * <p>Summaries are PHI-light (status/priority/date only) — full reason/recommendations
 * remain on the follow-up API.
 */
@Component
public class FollowUpTimelineProvider implements TimelineEventProvider {

    private static final Set<TimelineEventType> TYPES = EnumSet.of(TimelineEventType.FOLLOW_UP);

    private final FollowUpRepository followUpRepository;

    public FollowUpTimelineProvider(final FollowUpRepository followUpRepository) {
        this.followUpRepository = followUpRepository;
    }

    @Override
    public Set<TimelineEventType> supportedTypes() {
        return TYPES;
    }

    @Override
    public List<TimelineEventResponse> loadEvents(
            final UUID tenantId,
            final UUID patientId,
            final Set<TimelineEventType> typeFilter
    ) {
        if (!typeFilter.isEmpty() && typeFilter.stream().noneMatch(TYPES::contains)) {
            return List.of();
        }
        return followUpRepository.findByTenantIdAndPatientIdOrderByScheduledDateDesc(tenantId, patientId).stream()
                .filter(fu -> fu.getStatus() != FollowUpStatus.CANCELLED)
                .map(this::toEvent)
                .toList();
    }

    private TimelineEventResponse toEvent(final FollowUp followUp) {
        final String title = "Follow-up · " + followUp.getPriority().name();
        final StringBuilder summary = new StringBuilder();
        summary.append(followUp.getStatus().name());
        summary.append(" · due ").append(followUp.getScheduledDate());
        if (followUp.getScheduledTime() != null) {
            summary.append(' ').append(followUp.getScheduledTime());
        }

        return new TimelineEventResponse(
                TimelineEventType.FOLLOW_UP,
                followUp.getId(),
                followUp.getScheduledDate(),
                followUp.getCreatedAt(),
                title,
                TimelineSummaries.truncate(summary.toString()),
                followUp.getStatus().name(),
                severityHint(followUp),
                followUp.getPriority() == FollowUpPriority.URGENT,
                followUp.getCreatedBy(),
                "/api/v1/consultations/" + followUp.getConsultationId() + "/follow-ups/" + followUp.getId(),
                TimelineSummaries.attrs(
                        "priority", followUp.getPriority().name(),
                        "status", followUp.getStatus().name(),
                        "consultationId", followUp.getConsultationId().toString(),
                        "doctorId", followUp.getDoctorId().toString(),
                        "reminderEnabled", String.valueOf(Boolean.TRUE.equals(followUp.getReminderEnabled()))
                )
        );
    }

    private static TimelineSeverityHint severityHint(final FollowUp followUp) {
        if (followUp.getPriority() == FollowUpPriority.URGENT) {
            return TimelineSeverityHint.HIGH;
        }
        if (followUp.getStatus() == FollowUpStatus.MISSED) {
            return TimelineSeverityHint.HIGH;
        }
        return TimelineSeverityHint.STANDARD;
    }
}
