package com.healthcare.hms.patients.timeline.cursor;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSeverityHint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TimelineCursorCodec")
class TimelineCursorCodecTest {

    private final TimelineCursorCodec codec = new TimelineCursorCodec();

    @Test
    @DisplayName("encode/decode round-trip")
    void roundTrip() {
        final TimelineEventResponse event = new TimelineEventResponse(
                TimelineEventType.IMMUNIZATION,
                UUID.randomUUID(),
                LocalDate.of(2026, 7, 26),
                Instant.parse("2026-07-26T10:15:30Z"),
                "Hep B — dose 1",
                "Provider: Dr. Smith",
                "ADMINISTERED",
                TimelineSeverityHint.NONE,
                false,
                null,
                "/api/v1/patients/x/immunizations/y",
                Map.of("doseNumber", "1")
        );

        final String encoded = codec.encode(event);
        final TimelineCursor decoded = codec.decode(encoded);

        assertThat(decoded.occurredOn()).isEqualTo(event.occurredOn());
        assertThat(decoded.recordedAt()).isEqualTo(event.recordedAt());
        assertThat(decoded.type()).isEqualTo(event.type());
        assertThat(decoded.sourceId()).isEqualTo(event.sourceId());
    }
}
