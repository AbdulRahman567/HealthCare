package com.healthcare.hms.patients.timeline.service.impl;

import com.healthcare.hms.patients.service.PatientQueryService;
import com.healthcare.hms.patients.timeline.cursor.TimelineCursor;
import com.healthcare.hms.patients.timeline.cursor.TimelineCursorCodec;
import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.dto.response.TimelinePageResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSortDirection;
import com.healthcare.hms.patients.timeline.service.TimelineService;
import com.healthcare.hms.patients.timeline.spi.TimelineEventProvider;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fan-out read-model aggregator: providers → merge by clinical date → cursor page.
 */
@Service
public class TimelineServiceImpl implements TimelineService {

    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 100;

    private final List<TimelineEventProvider> providers;
    private final PatientQueryService patientQueryService;
    private final TimelineCursorCodec cursorCodec;

    public TimelineServiceImpl(
            final List<TimelineEventProvider> providers,
            final PatientQueryService patientQueryService,
            final TimelineCursorCodec cursorCodec
    ) {
        this.providers = List.copyOf(providers);
        this.patientQueryService = patientQueryService;
        this.cursorCodec = cursorCodec;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.PATIENT_READ)
    public TimelinePageResponse getTimeline(
            final UUID patientId,
            final Set<TimelineEventType> types,
            final String cursorEncoded,
            final int requestedSize,
            final TimelineSortDirection direction
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        patientQueryService.requireById(tenantId, patientId);

        final int size = clampSize(requestedSize);
        final TimelineSortDirection sort = direction == null ? TimelineSortDirection.DESC : direction;
        final Set<TimelineEventType> typeFilter = normalizeTypes(types);
        final TimelineCursor cursor = cursorCodec.decode(cursorEncoded);

        final List<TimelineEventResponse> merged = new ArrayList<>();
        for (final TimelineEventProvider provider : providers) {
            if (!typeFilter.isEmpty()
                    && provider.supportedTypes().stream().noneMatch(typeFilter::contains)) {
                continue;
            }
            merged.addAll(provider.loadEvents(tenantId, patientId, typeFilter));
        }

        final Comparator<TimelineEventResponse> comparator = sort == TimelineSortDirection.ASC
                ? cursorCodec.comparatorAscending()
                : cursorCodec.comparatorDescending();
        merged.sort(comparator);

        final List<TimelineEventResponse> afterCursor = merged.stream()
                .filter(event -> passesCursor(event, cursor, sort))
                .toList();

        final boolean hasNext = afterCursor.size() > size;
        final List<TimelineEventResponse> page = afterCursor.stream().limit(size).toList();
        final String nextCursor = hasNext && !page.isEmpty()
                ? cursorCodec.encode(page.getLast())
                : null;

        return new TimelinePageResponse(patientId, page, size, hasNext, nextCursor);
    }

    private boolean passesCursor(
            final TimelineEventResponse event,
            final TimelineCursor cursor,
            final TimelineSortDirection sort
    ) {
        if (cursor == null) {
            return true;
        }
        if (sort == TimelineSortDirection.ASC) {
            return cursorCodec.isAfterCursorAsc(event, cursor);
        }
        return cursorCodec.isBeforeCursorDesc(event, cursor);
    }

    private static int clampSize(final int requestedSize) {
        if (requestedSize <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(requestedSize, MAX_SIZE);
    }

    private static Set<TimelineEventType> normalizeTypes(final Set<TimelineEventType> types) {
        if (types == null || types.isEmpty()) {
            return Set.of();
        }
        return EnumSet.copyOf(types);
    }
}
