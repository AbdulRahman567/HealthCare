package com.healthcare.hms.appointments.queue.mapper;

import com.healthcare.hms.appointments.queue.dto.response.DoctorDayQueueResponse;
import com.healthcare.hms.appointments.queue.dto.response.QueueEntryResponse;
import com.healthcare.hms.appointments.queue.entity.DoctorDayQueue;
import com.healthcare.hms.appointments.queue.entity.QueueEntry;
import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class QueueMapper {

    private final PatientRepository patientRepository;

    public QueueMapper(final PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public QueueEntryResponse toEntryResponse(final QueueEntry entry) {
        return toEntryResponse(entry, null, null);
    }

    public QueueEntryResponse toEntryResponse(final QueueEntry entry, final String patientName) {
        return toEntryResponse(entry, patientName, null);
    }

    public QueueEntryResponse toEntryResponse(
            final QueueEntry entry,
            final String patientName,
            final UUID consultationId
    ) {
        return new QueueEntryResponse(
                entry.getId(),
                entry.getQueueId(),
                entry.getAppointmentId(),
                entry.getPatientId(),
                patientName,
                entry.getDoctorId(),
                entry.getHospitalId(),
                entry.getQueueNumber(),
                entry.getStatus(),
                entry.getCheckedInAt(),
                entry.getStatusChangedAt(),
                entry.getNotes(),
                consultationId,
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                entry.getVersion()
        );
    }

    public DoctorDayQueueResponse toQueueResponse(
            final DoctorDayQueue queue,
            final List<QueueEntry> entries,
            final Map<UUID, UUID> consultationIdByAppointmentId
    ) {
        final Map<UUID, String> patientNames = resolvePatientNames(queue.getTenantId(), entries);
        final long waiting = entries.stream()
                .filter(e -> e.getStatus() == QueueEntryStatus.WAITING
                        || e.getStatus() == QueueEntryStatus.CHECKED_IN)
                .count();
        final long inConsult = entries.stream()
                .filter(e -> e.getStatus() == QueueEntryStatus.IN_CONSULTATION)
                .count();
        final Map<UUID, UUID> consultationIds =
                consultationIdByAppointmentId != null ? consultationIdByAppointmentId : Map.of();
        return new DoctorDayQueueResponse(
                queue.getId(),
                queue.getDoctorId(),
                queue.getHospitalId(),
                queue.getQueueDate(),
                queue.getLastQueueNumber(),
                waiting,
                inConsult,
                entries.stream()
                        .map(entry -> toEntryResponse(
                                entry,
                                patientNames.get(entry.getPatientId()),
                                consultationIds.get(entry.getAppointmentId())
                        ))
                        .toList(),
                queue.getCreatedAt(),
                queue.getUpdatedAt(),
                queue.getVersion()
        );
    }

    private Map<UUID, String> resolvePatientNames(final UUID tenantId, final List<QueueEntry> entries) {
        if (entries.isEmpty()) {
            return Map.of();
        }
        final Set<UUID> patientIds = entries.stream().map(QueueEntry::getPatientId).collect(Collectors.toSet());
        final Map<UUID, Patient> patients = patientRepository.findByTenantIdAndIdIn(tenantId, patientIds).stream()
                .collect(Collectors.toMap(Patient::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Map<UUID, String> names = new HashMap<>();
        for (final UUID patientId : patientIds) {
            final Patient patient = patients.get(patientId);
            if (patient == null) {
                continue;
            }
            names.put(patientId, displayName(patient.getFirstName(), patient.getLastName(), patient.getMrn()));
        }
        return names;
    }

    private static String displayName(final String first, final String last, final String fallback) {
        final StringBuilder builder = new StringBuilder();
        if (first != null && !first.isBlank()) {
            builder.append(first.trim());
        }
        if (last != null && !last.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(last.trim());
        }
        if (!builder.isEmpty()) {
            return builder.toString();
        }
        return fallback;
    }
}
