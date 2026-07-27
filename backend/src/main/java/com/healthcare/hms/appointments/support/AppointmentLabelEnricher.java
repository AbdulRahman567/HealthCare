package com.healthcare.hms.appointments.support;

import com.healthcare.hms.appointments.dto.response.AppointmentResponse;
import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.mapper.AppointmentMapper;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Batch-enriches appointment responses with patient display labels (fixed query count).
 */
@Component
public class AppointmentLabelEnricher {

    private final PatientRepository patientRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentLabelEnricher(
            final PatientRepository patientRepository,
            final AppointmentMapper appointmentMapper
    ) {
        this.patientRepository = patientRepository;
        this.appointmentMapper = appointmentMapper;
    }

    public AppointmentResponse enrichOne(final UUID tenantId, final Appointment appointment) {
        return enrich(tenantId, List.of(appointment)).getFirst();
    }

    public List<AppointmentResponse> enrich(final UUID tenantId, final Collection<Appointment> appointments) {
        if (appointments.isEmpty()) {
            return List.of();
        }
        final Set<UUID> patientIds = appointments.stream()
                .map(Appointment::getPatientId)
                .collect(Collectors.toSet());
        final Map<UUID, Patient> patients = patientRepository.findByTenantIdAndIdIn(tenantId, patientIds).stream()
                .collect(Collectors.toMap(Patient::getId, Function.identity(), (a, b) -> a, HashMap::new));

        return appointments.stream()
                .map(appointment -> {
                    final Patient patient = patients.get(appointment.getPatientId());
                    return appointmentMapper.toResponse(
                            appointment,
                            patient == null ? null : displayName(patient.getFirstName(), patient.getLastName(), patient.getMrn()),
                            patient == null ? null : patient.getMrn()
                    );
                })
                .toList();
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
