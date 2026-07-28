package com.healthcare.hms.clinical.diagnosis.support;

import com.healthcare.hms.clinical.diagnosis.dto.response.DiagnosisResponse;
import com.healthcare.hms.clinical.diagnosis.mapper.DiagnosisMapper;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.entity.Diagnosis;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisLabelEnricher {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DiagnosisMapper diagnosisMapper;

    public DiagnosisLabelEnricher(
            final DoctorRepository doctorRepository,
            final UserRepository userRepository,
            final DiagnosisMapper diagnosisMapper
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.diagnosisMapper = diagnosisMapper;
    }

    public DiagnosisResponse enrichOne(
            final UUID tenantId,
            final Diagnosis diagnosis,
            final Consultation consultation
    ) {
        return enrich(
                tenantId,
                List.of(diagnosis),
                consultation == null ? Map.of() : Map.of(consultation.getId(), consultation)
        ).getFirst();
    }

    public List<DiagnosisResponse> enrich(
            final UUID tenantId,
            final Collection<Diagnosis> diagnoses,
            final Map<UUID, Consultation> consultationsById
    ) {
        if (diagnoses.isEmpty()) {
            return List.of();
        }

        final Set<UUID> doctorIds = diagnoses.stream()
                .map(Diagnosis::getDiagnosingDoctorId)
                .collect(Collectors.toSet());
        final Map<UUID, Doctor> doctors = doctorRepository.findByTenantIdAndIdIn(tenantId, doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, Function.identity(), (a, b) -> a, HashMap::new));

        final Set<UUID> userIds = doctors.values().stream()
                .map(Doctor::getUserId)
                .collect(Collectors.toSet());
        final Map<UUID, User> users = userIds.isEmpty()
                ? Map.of()
                : userRepository.findByTenantIdAndIdIn(tenantId, userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, HashMap::new));

        return diagnoses.stream()
                .map(diagnosis -> {
                    final Consultation consultation = consultationsById.get(diagnosis.getConsultationId());
                    final Doctor doctor = doctors.get(diagnosis.getDiagnosingDoctorId());
                    final User doctorUser = doctor == null ? null : users.get(doctor.getUserId());
                    return diagnosisMapper.toResponse(
                            diagnosis,
                            consultation == null ? null : consultation.getConsultationNumber(),
                            doctorUser == null ? null : displayName(doctorUser.getFirstName(), doctorUser.getLastName())
                    );
                })
                .toList();
    }

    private static String displayName(final String first, final String last) {
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
        return builder.isEmpty() ? null : builder.toString();
    }
}
