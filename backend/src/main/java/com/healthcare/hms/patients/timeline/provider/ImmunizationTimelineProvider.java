package com.healthcare.hms.patients.timeline.provider;

import com.healthcare.hms.patients.immunization.entity.Immunization;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.repository.ImmunizationRepository;
import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSeverityHint;
import com.healthcare.hms.patients.timeline.spi.TimelineEventProvider;
import com.healthcare.hms.patients.timeline.support.TimelineSummaries;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Immunization / vaccination timeline contributions.
 */
@Component
public class ImmunizationTimelineProvider implements TimelineEventProvider {

    private static final Set<TimelineEventType> TYPES = EnumSet.of(TimelineEventType.IMMUNIZATION);

    private final ImmunizationRepository immunizationRepository;

    public ImmunizationTimelineProvider(final ImmunizationRepository immunizationRepository) {
        this.immunizationRepository = immunizationRepository;
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
        return immunizationRepository
                .findByTenantIdAndPatientIdOrderByAdministrationDateDescVaccineNameAscDoseNumberAsc(
                        tenantId, patientId
                )
                .stream()
                .filter(immunization -> immunization.getStatus() != ImmunizationStatus.ENTERED_IN_ERROR)
                .map(this::toEvent)
                .toList();
    }

    private TimelineEventResponse toEvent(final Immunization immunization) {
        final String title = switch (immunization.getStatus()) {
            case REFUSED -> "Vaccine refused: " + immunization.getVaccineName();
            case SCHEDULED -> "Vaccine scheduled: " + immunization.getVaccineName();
            default -> immunization.getVaccineName() + " — dose " + immunization.getDoseNumber();
        };
        final String summary = "Provider: "
                + immunization.getHealthcareProvider()
                + (immunization.getRoute() == null ? "" : " · " + immunization.getRoute().name());

        return new TimelineEventResponse(
                TimelineEventType.IMMUNIZATION,
                immunization.getId(),
                immunization.getAdministrationDate(),
                immunization.getCreatedAt(),
                title,
                TimelineSummaries.truncate(summary),
                immunization.getStatus().name(),
                TimelineSeverityHint.NONE,
                false,
                immunization.getRecordedByUserId(),
                "/api/v1/patients/" + immunization.getPatientId() + "/immunizations/" + immunization.getId(),
                TimelineSummaries.attrs(
                        "doseNumber", String.valueOf(immunization.getDoseNumber()),
                        "provider", immunization.getHealthcareProvider(),
                        "route", immunization.getRoute() == null ? null : immunization.getRoute().name()
                )
        );
    }
}
