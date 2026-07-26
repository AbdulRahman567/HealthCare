package com.healthcare.hms.patients.timeline.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.service.PatientQueryService;
import com.healthcare.hms.patients.timeline.cursor.TimelineCursorCodec;
import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.dto.response.TimelinePageResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSeverityHint;
import com.healthcare.hms.patients.timeline.enums.TimelineSortDirection;
import com.healthcare.hms.patients.timeline.spi.TimelineEventProvider;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimelineServiceImpl")
class TimelineServiceImplTest {

    @Mock
    private PatientQueryService patientQueryService;
    @Mock
    private TimelineEventProvider historyProvider;
    @Mock
    private TimelineEventProvider allergyProvider;

    private TimelineCursorCodec cursorCodec;
    private TimelineServiceImpl service;

    private UUID tenantId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        cursorCodec = new TimelineCursorCodec();

        TenantContextHolder.set(new TenantContext(
                tenantId,
                "city-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        lenient().when(historyProvider.supportedTypes()).thenReturn(EnumSet.of(
                TimelineEventType.PAST_DISEASE,
                TimelineEventType.SURGERY,
                TimelineEventType.CHRONIC_CONDITION
        ));
        lenient().when(allergyProvider.supportedTypes()).thenReturn(EnumSet.of(TimelineEventType.ALLERGY));

        service = new TimelineServiceImpl(
                List.of(historyProvider, allergyProvider),
                patientQueryService,
                cursorCodec
        );
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("merges providers chronologically newest-first")
    void getTimeline_mergesDescending() {
        when(patientQueryService.requireById(tenantId, patientId)).thenReturn(new Patient());
        when(historyProvider.loadEvents(eq(tenantId), eq(patientId), any()))
                .thenReturn(List.of(event(
                        TimelineEventType.PAST_DISEASE,
                        LocalDate.of(2024, 1, 10),
                        "Flu"
                )));
        when(allergyProvider.loadEvents(eq(tenantId), eq(patientId), any()))
                .thenReturn(List.of(event(
                        TimelineEventType.ALLERGY,
                        LocalDate.of(2025, 6, 1),
                        "Allergy: Penicillin"
                )));

        final TimelinePageResponse page = service.getTimeline(
                patientId,
                Set.of(),
                null,
                20,
                TimelineSortDirection.DESC
        );

        assertThat(page.content()).hasSize(2);
        assertThat(page.content().getFirst().type()).isEqualTo(TimelineEventType.ALLERGY);
        assertThat(page.content().get(1).type()).isEqualTo(TimelineEventType.PAST_DISEASE);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("cursor page returns older events and nextCursor when more remain")
    void getTimeline_cursorPagination() {
        when(patientQueryService.requireById(tenantId, patientId)).thenReturn(new Patient());
        final TimelineEventResponse newest = event(
                TimelineEventType.IMMUNIZATION,
                LocalDate.of(2026, 7, 1),
                "Hep B"
        );
        final TimelineEventResponse middle = event(
                TimelineEventType.ALLERGY,
                LocalDate.of(2025, 1, 1),
                "Allergy: Latex"
        );
        final TimelineEventResponse oldest = event(
                TimelineEventType.REGISTRATION,
                LocalDate.of(2020, 1, 1),
                "Patient registered"
        );

        when(historyProvider.loadEvents(eq(tenantId), eq(patientId), any())).thenReturn(List.of(oldest));
        when(allergyProvider.loadEvents(eq(tenantId), eq(patientId), any()))
                .thenReturn(List.of(newest, middle));

        final TimelinePageResponse first = service.getTimeline(
                patientId, Set.of(), null, 2, TimelineSortDirection.DESC
        );
        assertThat(first.content()).hasSize(2);
        assertThat(first.hasNext()).isTrue();
        assertThat(first.nextCursor()).isNotBlank();

        final TimelinePageResponse second = service.getTimeline(
                patientId, Set.of(), first.nextCursor(), 2, TimelineSortDirection.DESC
        );
        assertThat(second.content()).hasSize(1);
        assertThat(second.content().getFirst().title()).isEqualTo("Patient registered");
        assertThat(second.hasNext()).isFalse();
    }

    @Test
    @DisplayName("type filter skips irrelevant providers")
    void getTimeline_typeFilter() {
        when(patientQueryService.requireById(tenantId, patientId)).thenReturn(new Patient());
        when(allergyProvider.loadEvents(eq(tenantId), eq(patientId), any()))
                .thenReturn(List.of(event(
                        TimelineEventType.ALLERGY,
                        LocalDate.of(2025, 1, 1),
                        "Allergy: Peanuts"
                )));

        final TimelinePageResponse page = service.getTimeline(
                patientId,
                Set.of(TimelineEventType.ALLERGY),
                null,
                20,
                TimelineSortDirection.DESC
        );

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().type()).isEqualTo(TimelineEventType.ALLERGY);
    }

    @Test
    @DisplayName("missing patient fails")
    void getTimeline_patientNotFound() {
        when(patientQueryService.requireById(tenantId, patientId))
                .thenThrow(new ResourceNotFoundException("Patient not found"));

        assertThatThrownBy(() -> service.getTimeline(
                patientId, Set.of(), null, 20, TimelineSortDirection.DESC
        )).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("invalid cursor fails with business exception")
    void getTimeline_invalidCursor() {
        when(patientQueryService.requireById(tenantId, patientId)).thenReturn(new Patient());

        assertThatThrownBy(() -> service.getTimeline(
                patientId, Set.of(), "not-a-cursor", 20, TimelineSortDirection.DESC
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("cursor");
    }

    @Test
    @DisplayName("future-only type filter yields empty feed when no provider")
    void getTimeline_futureTypesEmpty() {
        when(patientQueryService.requireById(tenantId, patientId)).thenReturn(new Patient());

        final TimelinePageResponse page = service.getTimeline(
                patientId,
                Set.of(TimelineEventType.VISIT, TimelineEventType.PRESCRIPTION),
                null,
                20,
                TimelineSortDirection.DESC
        );

        assertThat(page.content()).isEmpty();
        assertThat(page.hasNext()).isFalse();
    }

    private static TimelineEventResponse event(
            final TimelineEventType type,
            final LocalDate occurredOn,
            final String title
    ) {
        return new TimelineEventResponse(
                type,
                UUID.randomUUID(),
                occurredOn,
                Instant.parse(occurredOn + "T12:00:00Z"),
                title,
                title,
                "ACTIVE",
                TimelineSeverityHint.NONE,
                false,
                null,
                "/api/v1/patients/x",
                Map.of()
        );
    }
}
