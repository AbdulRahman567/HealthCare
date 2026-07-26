package com.healthcare.hms.patients.timeline.service;

import com.healthcare.hms.patients.timeline.dto.response.TimelinePageResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSortDirection;
import java.util.Set;
import java.util.UUID;

/**
 * Patient chronological timeline (Phase 5.6).
 */
public interface TimelineService {

    /**
     * Returns a cursor-paged chronological feed for the patient chart.
     *
     * @param patientId patient id
     * @param types     optional type filter (empty = all available providers)
     * @param cursor    opaque cursor from previous page (nullable)
     * @param size      page size (clamped)
     * @param direction DESC (default) or ASC
     */
    TimelinePageResponse getTimeline(
            UUID patientId,
            Set<TimelineEventType> types,
            String cursor,
            int size,
            TimelineSortDirection direction
    );
}
