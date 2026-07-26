package com.healthcare.hms.patients.timeline.dto.response;

import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSeverityHint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Unified chronological timeline item (summary only — full PHI stays on module APIs).
 */
public record TimelineEventResponse(
        TimelineEventType type,
        UUID sourceId,
        LocalDate occurredOn,
        Instant recordedAt,
        String title,
        String summary,
        String status,
        TimelineSeverityHint severityHint,
        boolean critical,
        UUID recordedByUserId,
        String detailPath,
        Map<String, String> attributes
) {
}
