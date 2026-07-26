package com.healthcare.hms.patients.timeline.cursor;

import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Opaque keyset cursor for chronological merge pagination.
 */
public record TimelineCursor(
        LocalDate occurredOn,
        Instant recordedAt,
        TimelineEventType type,
        UUID sourceId
) {
}
