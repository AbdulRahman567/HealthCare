package com.healthcare.hms.patients.timeline.cursor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import org.springframework.stereotype.Component;

/**
 * Encodes / decodes opaque timeline cursors and compares events for merge sort.
 */
@Component
public class TimelineCursorCodec {

    private final ObjectMapper objectMapper;

    public TimelineCursorCodec() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String encode(final TimelineEventResponse event) {
        final TimelineCursor cursor = new TimelineCursor(
                event.occurredOn(),
                event.recordedAt(),
                event.type(),
                event.sourceId()
        );
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(cursor));
        } catch (final JsonProcessingException exception) {
            throw new IllegalStateException("Failed to encode timeline cursor", exception);
        }
    }

    public TimelineCursor decode(final String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }
        try {
            final byte[] bytes = Base64.getUrlDecoder().decode(encoded.trim());
            return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8), TimelineCursor.class);
        } catch (final IllegalArgumentException | JsonProcessingException exception) {
            throw new BusinessException("Invalid timeline cursor");
        }
    }

    public Comparator<TimelineEventResponse> comparatorDescending() {
        return Comparator
                .comparing(TimelineEventResponse::occurredOn, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(TimelineEventResponse::recordedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(e -> e.type().name())
                .thenComparing(TimelineEventResponse::sourceId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    public Comparator<TimelineEventResponse> comparatorAscending() {
        return Comparator
                .comparing(TimelineEventResponse::occurredOn, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(TimelineEventResponse::recordedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(e -> e.type().name())
                .thenComparing(TimelineEventResponse::sourceId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    /**
     * Whether {@code event} is strictly after the cursor when paging newest-first (DESC).
     */
    public boolean isBeforeCursorDesc(final TimelineEventResponse event, final TimelineCursor cursor) {
        if (cursor == null) {
            return true;
        }
        final TimelineEventResponse synthetic = synthetic(cursor);
        return comparatorDescending().compare(event, synthetic) > 0;
    }

    /**
     * Whether {@code event} is strictly after the cursor when paging oldest-first (ASC).
     */
    public boolean isAfterCursorAsc(final TimelineEventResponse event, final TimelineCursor cursor) {
        if (cursor == null) {
            return true;
        }
        final TimelineEventResponse synthetic = synthetic(cursor);
        return comparatorAscending().compare(event, synthetic) > 0;
    }

    private static TimelineEventResponse synthetic(final TimelineCursor cursor) {
        return new TimelineEventResponse(
                cursor.type(),
                cursor.sourceId(),
                cursor.occurredOn(),
                cursor.recordedAt(),
                "",
                "",
                null,
                null,
                false,
                null,
                null,
                null
        );
    }
}
