package com.healthcare.hms.patients.timeline.provider;

import com.healthcare.hms.patients.allergy.entity.Allergy;
import com.healthcare.hms.patients.allergy.enums.AllergyStatus;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import com.healthcare.hms.patients.allergy.enums.Reaction;
import com.healthcare.hms.patients.allergy.enums.Severity;
import com.healthcare.hms.patients.allergy.repository.AllergyRepository;
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
 * Allergy timeline contributions (supplementary to banner / critical APIs).
 */
@Component
public class AllergyTimelineProvider implements TimelineEventProvider {

    private static final Set<TimelineEventType> TYPES = EnumSet.of(TimelineEventType.ALLERGY);

    private final AllergyRepository allergyRepository;

    public AllergyTimelineProvider(final AllergyRepository allergyRepository) {
        this.allergyRepository = allergyRepository;
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
        return allergyRepository
                .findByTenantIdAndPatientIdOrderBySeverityDescAllergenNameAsc(tenantId, patientId)
                .stream()
                .filter(allergy -> allergy.getStatus() != AllergyStatus.ENTERED_IN_ERROR)
                .map(this::toEvent)
                .toList();
    }

    private TimelineEventResponse toEvent(final Allergy allergy) {
        final LocalDate occurredOn = allergy.getOnsetDate() != null
                ? allergy.getOnsetDate()
                : (allergy.getCreatedAt() == null
                        ? null
                        : LocalDate.ofInstant(allergy.getCreatedAt(), ZoneOffset.UTC));

        final TimelineSeverityHint hint = severityHint(allergy);
        final String summary = allergy.getAllergyType().name()
                + " · "
                + allergy.getSeverity().name()
                + " · "
                + allergy.getReaction().name()
                + (allergy.isVerified() ? " · verified" : " · patient-reported");

        return new TimelineEventResponse(
                TimelineEventType.ALLERGY,
                allergy.getId(),
                occurredOn,
                allergy.getCreatedAt(),
                "Allergy: " + allergy.getAllergenName(),
                TimelineSummaries.truncate(summary),
                allergy.getStatus().name(),
                hint,
                allergy.isCriticalAlert() || allergy.isLifeThreatening(),
                allergy.getRecordedByUserId(),
                "/api/v1/patients/" + allergy.getPatientId() + "/allergies/" + allergy.getId(),
                TimelineSummaries.attrs(
                        "allergyType", allergy.getAllergyType().name(),
                        "severity", allergy.getSeverity().name(),
                        "reaction", allergy.getReaction().name(),
                        "verified", String.valueOf(allergy.isVerified())
                )
        );
    }

    private static TimelineSeverityHint severityHint(final Allergy allergy) {
        if (allergy.isLifeThreatening()
                || allergy.getSeverity() == Severity.LIFE_THREATENING
                || allergy.getReaction() == Reaction.ANAPHYLAXIS
                || allergy.isCriticalAlert()) {
            return TimelineSeverityHint.CRITICAL;
        }
        if (allergy.getAllergyType() == AllergyType.DRUG
                && (allergy.getSeverity() == Severity.SEVERE || allergy.getSeverity() == Severity.MODERATE)) {
            return TimelineSeverityHint.HIGH;
        }
        if (allergy.getStatus() == AllergyStatus.INACTIVE) {
            return TimelineSeverityHint.STANDARD;
        }
        return TimelineSeverityHint.STANDARD;
    }
}
