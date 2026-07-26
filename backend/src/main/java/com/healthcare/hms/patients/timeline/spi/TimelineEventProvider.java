package com.healthcare.hms.patients.timeline.spi;

import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Extension point for clinical modules that contribute timeline events.
 *
 * <p>Future visits / prescriptions / lab / billing modules register a Spring bean
 * implementing this SPI — no changes to the timeline orchestrator required.
 */
public interface TimelineEventProvider {

    /**
     * Event types this provider can emit.
     */
    Set<TimelineEventType> supportedTypes();

    /**
     * Loads timeline events for the patient (tenant already bound in context).
     *
     * <p>Implementations must use indexed tenant+patient queries and exclude
     * soft-deleted / entered-in-error rows from the default clinical narrative.
     *
     * @param tenantId  current tenant
     * @param patientId patient chart id
     * @param typeFilter requested types (empty = all supported by this provider)
     */
    List<TimelineEventResponse> loadEvents(UUID tenantId, UUID patientId, Set<TimelineEventType> typeFilter);
}
