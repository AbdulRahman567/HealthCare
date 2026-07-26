package com.healthcare.hms.patients.timeline.provider;

import com.healthcare.hms.patients.history.entity.ChronicCondition;
import com.healthcare.hms.patients.history.entity.PastDisease;
import com.healthcare.hms.patients.history.entity.SurgeryHistory;
import com.healthcare.hms.patients.history.repository.ChronicConditionRepository;
import com.healthcare.hms.patients.history.repository.PastDiseaseRepository;
import com.healthcare.hms.patients.history.repository.SurgeryHistoryRepository;
import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSeverityHint;
import com.healthcare.hms.patients.timeline.spi.TimelineEventProvider;
import com.healthcare.hms.patients.timeline.support.TimelineSummaries;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Medical history timeline contributions (past disease, surgery, chronic condition).
 */
@Component
public class MedicalHistoryTimelineProvider implements TimelineEventProvider {

    private static final Set<TimelineEventType> TYPES = EnumSet.of(
            TimelineEventType.PAST_DISEASE,
            TimelineEventType.SURGERY,
            TimelineEventType.CHRONIC_CONDITION
    );

    private final PastDiseaseRepository pastDiseaseRepository;
    private final SurgeryHistoryRepository surgeryHistoryRepository;
    private final ChronicConditionRepository chronicConditionRepository;

    public MedicalHistoryTimelineProvider(
            final PastDiseaseRepository pastDiseaseRepository,
            final SurgeryHistoryRepository surgeryHistoryRepository,
            final ChronicConditionRepository chronicConditionRepository
    ) {
        this.pastDiseaseRepository = pastDiseaseRepository;
        this.surgeryHistoryRepository = surgeryHistoryRepository;
        this.chronicConditionRepository = chronicConditionRepository;
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
        final List<TimelineEventResponse> events = new ArrayList<>();
        if (include(typeFilter, TimelineEventType.PAST_DISEASE)) {
            pastDiseaseRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId)
                    .forEach(entry -> events.add(fromPastDisease(entry)));
        }
        if (include(typeFilter, TimelineEventType.SURGERY)) {
            surgeryHistoryRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId)
                    .forEach(entry -> events.add(fromSurgery(entry)));
        }
        if (include(typeFilter, TimelineEventType.CHRONIC_CONDITION)) {
            chronicConditionRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId)
                    .forEach(entry -> events.add(fromChronic(entry)));
        }
        return events;
    }

    private static boolean include(final Set<TimelineEventType> typeFilter, final TimelineEventType type) {
        return typeFilter.isEmpty() || typeFilter.contains(type);
    }

    private TimelineEventResponse fromPastDisease(final PastDisease entry) {
        return new TimelineEventResponse(
                TimelineEventType.PAST_DISEASE,
                entry.getId(),
                entry.getDiagnosisDate(),
                entry.getCreatedAt(),
                entry.getDiseaseName(),
                TimelineSummaries.truncate(buildHistorySummary(
                        entry.getDiseaseCategory().name(),
                        entry.getSeverity().name(),
                        entry.getConditionStatus().name(),
                        null
                )),
                entry.getConditionStatus().name(),
                mapSeverity(entry.getSeverity().name()),
                false,
                entry.getRecordedByUserId(),
                "/api/v1/patients/" + entry.getPatientId() + "/medical-history/past-diseases/" + entry.getId(),
                TimelineSummaries.attrs(
                        "category", entry.getDiseaseCategory().name(),
                        "code", entry.getDiseaseCode(),
                        "severity", entry.getSeverity().name()
                )
        );
    }

    private TimelineEventResponse fromSurgery(final SurgeryHistory entry) {
        return new TimelineEventResponse(
                TimelineEventType.SURGERY,
                entry.getId(),
                entry.getDiagnosisDate(),
                entry.getCreatedAt(),
                entry.getProcedureName(),
                TimelineSummaries.truncate(buildHistorySummary(
                        entry.getProcedureCategory().name(),
                        entry.getSeverity().name(),
                        entry.getConditionStatus().name(),
                        entry.getPerformingFacility()
                )),
                entry.getConditionStatus().name(),
                mapSeverity(entry.getSeverity().name()),
                false,
                entry.getRecordedByUserId(),
                "/api/v1/patients/" + entry.getPatientId() + "/medical-history/surgeries/" + entry.getId(),
                TimelineSummaries.attrs(
                        "category", entry.getProcedureCategory().name(),
                        "code", entry.getProcedureCode(),
                        "facility", entry.getPerformingFacility()
                )
        );
    }

    private TimelineEventResponse fromChronic(final ChronicCondition entry) {
        return new TimelineEventResponse(
                TimelineEventType.CHRONIC_CONDITION,
                entry.getId(),
                entry.getDiagnosisDate(),
                entry.getCreatedAt(),
                entry.getConditionName(),
                TimelineSummaries.truncate(buildHistorySummary(
                        entry.getDiseaseCategory().name(),
                        entry.getSeverity().name(),
                        entry.getConditionStatus().name(),
                        null
                )),
                entry.getConditionStatus().name(),
                mapSeverity(entry.getSeverity().name()),
                false,
                entry.getRecordedByUserId(),
                "/api/v1/patients/" + entry.getPatientId() + "/medical-history/chronic-conditions/" + entry.getId(),
                TimelineSummaries.attrs(
                        "category", entry.getDiseaseCategory().name(),
                        "code", entry.getConditionCode(),
                        "severity", entry.getSeverity().name()
                )
        );
    }

    private static String buildHistorySummary(
            final String category,
            final String severity,
            final String status,
            final String notesOrFacility
    ) {
        final StringBuilder builder = new StringBuilder();
        builder.append(category).append(" · ").append(severity).append(" · ").append(status);
        if (notesOrFacility != null && !notesOrFacility.isBlank()) {
            builder.append(" — ").append(notesOrFacility.trim());
        }
        return builder.toString();
    }

    private static TimelineSeverityHint mapSeverity(final String severity) {
        return switch (severity) {
            case "CRITICAL" -> TimelineSeverityHint.CRITICAL;
            case "SEVERE" -> TimelineSeverityHint.HIGH;
            case "MODERATE", "MILD" -> TimelineSeverityHint.STANDARD;
            default -> TimelineSeverityHint.NONE;
        };
    }
}
