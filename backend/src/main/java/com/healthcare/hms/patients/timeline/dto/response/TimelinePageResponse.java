package com.healthcare.hms.patients.timeline.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Cursor-paged timeline envelope (offset pagination is unsuitable for merged feeds).
 */
public record TimelinePageResponse(
        UUID patientId,
        List<TimelineEventResponse> content,
        int size,
        boolean hasNext,
        String nextCursor
) {
}
