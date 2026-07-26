package com.healthcare.hms.patients.timeline.provider;

import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSeverityHint;
import com.healthcare.hms.patients.timeline.spi.TimelineEventProvider;
import com.healthcare.hms.patients.timeline.support.TimelineSummaries;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Emits a single registration anchor event from the patient row.
 */
@Component
public class RegistrationTimelineProvider implements TimelineEventProvider {

    private static final Set<TimelineEventType> TYPES = EnumSet.of(TimelineEventType.REGISTRATION);

    private final PatientRepository patientRepository;

    public RegistrationTimelineProvider(final PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
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
        return patientRepository.findByIdAndTenantId(patientId, tenantId)
                .map(this::toEvent)
                .map(List::of)
                .orElseGet(List::of);
    }

    private TimelineEventResponse toEvent(final Patient patient) {
        final LocalDate occurredOn = patient.getCreatedAt() == null
                ? null
                : LocalDate.ofInstant(patient.getCreatedAt(), ZoneOffset.UTC);
        return new TimelineEventResponse(
                TimelineEventType.REGISTRATION,
                patient.getId(),
                occurredOn,
                patient.getCreatedAt(),
                "Patient registered",
                "MRN " + patient.getMrn(),
                patient.getStatus() == null ? null : patient.getStatus().name(),
                TimelineSeverityHint.NONE,
                false,
                patient.getCreatedBy(),
                "/api/v1/patients/" + patient.getId(),
                TimelineSummaries.attrs("mrn", patient.getMrn())
        );
    }
}
