package com.healthcare.hms.prescriptions.timeline;

import com.healthcare.hms.patients.timeline.dto.response.TimelineEventResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSeverityHint;
import com.healthcare.hms.patients.timeline.spi.TimelineEventProvider;
import com.healthcare.hms.patients.timeline.support.TimelineSummaries;
import com.healthcare.hms.prescriptions.entity.Prescription;
import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import com.healthcare.hms.prescriptions.repository.PrescriptionRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Prescription contributions to the patient chronological timeline (Phase 7.10).
 *
 * <p>Summaries are PHI-light (number/status/date only) — medicine lines remain on the
 * prescription API.
 */
@Component
public class PrescriptionTimelineProvider implements TimelineEventProvider {

    private static final Set<TimelineEventType> TYPES = EnumSet.of(TimelineEventType.PRESCRIPTION);

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionTimelineProvider(final PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
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
        return prescriptionRepository
                .findByTenantIdAndPatientIdOrderByPrescriptionDateDescCreatedAtDesc(tenantId, patientId)
                .stream()
                .filter(rx -> rx.getStatus() != PrescriptionStatus.CANCELLED)
                .map(this::toEvent)
                .toList();
    }

    private TimelineEventResponse toEvent(final Prescription prescription) {
        final String title = "Prescription · " + prescription.getPrescriptionNumber();
        final String summary = prescription.getStatus().name()
                + " · "
                + prescription.getPrescriptionDate();

        return new TimelineEventResponse(
                TimelineEventType.PRESCRIPTION,
                prescription.getId(),
                prescription.getPrescriptionDate(),
                prescription.getIssuedAt() != null ? prescription.getIssuedAt() : prescription.getCreatedAt(),
                title,
                TimelineSummaries.truncate(summary),
                prescription.getStatus().name(),
                severityHint(prescription),
                prescription.getStatus() == PrescriptionStatus.ISSUED,
                prescription.getCreatedBy(),
                "/api/v1/prescriptions/" + prescription.getId(),
                TimelineSummaries.attrs(
                        "status", prescription.getStatus().name(),
                        "prescriptionNumber", prescription.getPrescriptionNumber(),
                        "consultationId", prescription.getConsultationId().toString(),
                        "doctorId", prescription.getDoctorId().toString()
                )
        );
    }

    private static TimelineSeverityHint severityHint(final Prescription prescription) {
        return switch (prescription.getStatus()) {
            case ISSUED, PARTIALLY_DISPENSED, DISPENSED -> TimelineSeverityHint.HIGH;
            case DRAFT, CANCELLED -> TimelineSeverityHint.STANDARD;
        };
    }
}
