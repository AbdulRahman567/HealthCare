package com.healthcare.hms.clinical.timeline;

import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.enums.ConsultationStatus;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
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
 * Consultation / visit contributions to the patient chronological timeline (Phase 7.10).
 *
 * <p>Summaries are PHI-light (number/status/date only) — clinical narrative remains on the
 * consultation API.
 */
@Component
public class ConsultationTimelineProvider implements TimelineEventProvider {

    private static final Set<TimelineEventType> TYPES = EnumSet.of(TimelineEventType.VISIT);

    private final ConsultationRepository consultationRepository;

    public ConsultationTimelineProvider(final ConsultationRepository consultationRepository) {
        this.consultationRepository = consultationRepository;
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
        return consultationRepository
                .findByTenantIdAndPatientIdOrderByConsultationDateDescStartedAtDesc(tenantId, patientId)
                .stream()
                .filter(c -> c.getStatus() != ConsultationStatus.CANCELLED)
                .map(this::toEvent)
                .toList();
    }

    private TimelineEventResponse toEvent(final Consultation consultation) {
        final String title = "Visit · " + consultation.getConsultationNumber();
        final StringBuilder summary = new StringBuilder();
        summary.append(consultation.getStatus().name());
        summary.append(" · ").append(consultation.getConsultationDate());
        if (consultation.getCompletedAt() != null) {
            summary.append(" · completed");
        }

        return new TimelineEventResponse(
                TimelineEventType.VISIT,
                consultation.getId(),
                consultation.getConsultationDate(),
                consultation.getStartedAt() != null ? consultation.getStartedAt() : consultation.getCreatedAt(),
                title,
                TimelineSummaries.truncate(summary.toString()),
                consultation.getStatus().name(),
                severityHint(consultation),
                consultation.getStatus() == ConsultationStatus.IN_PROGRESS
                        || consultation.getStatus() == ConsultationStatus.PAUSED,
                consultation.getCreatedBy(),
                "/api/v1/consultations/" + consultation.getId(),
                TimelineSummaries.attrs(
                        "status", consultation.getStatus().name(),
                        "consultationNumber", consultation.getConsultationNumber(),
                        "doctorId", consultation.getDoctorId().toString(),
                        "appointmentId", consultation.getAppointmentId() != null
                                ? consultation.getAppointmentId().toString()
                                : null
                )
        );
    }

    private static TimelineSeverityHint severityHint(final Consultation consultation) {
        return switch (consultation.getStatus()) {
            case IN_PROGRESS, PAUSED -> TimelineSeverityHint.HIGH;
            case DRAFT -> TimelineSeverityHint.STANDARD;
            case COMPLETED, CANCELLED -> TimelineSeverityHint.STANDARD;
        };
    }
}
