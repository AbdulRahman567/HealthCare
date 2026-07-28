package com.healthcare.hms.clinical.followup.support;

import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.entity.FollowUp;
import com.healthcare.hms.clinical.followup.dto.response.FollowUpResponse;
import com.healthcare.hms.clinical.followup.mapper.FollowUpMapper;
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
public class FollowUpLabelEnricher {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final FollowUpMapper followUpMapper;

    public FollowUpLabelEnricher(
            final DoctorRepository doctorRepository,
            final UserRepository userRepository,
            final FollowUpMapper followUpMapper
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.followUpMapper = followUpMapper;
    }

    public FollowUpResponse enrichOne(
            final UUID tenantId,
            final FollowUp followUp,
            final Consultation consultation
    ) {
        return enrich(
                tenantId,
                List.of(followUp),
                consultation == null ? Map.of() : Map.of(consultation.getId(), consultation)
        ).getFirst();
    }

    public List<FollowUpResponse> enrich(
            final UUID tenantId,
            final Collection<FollowUp> followUps,
            final Map<UUID, Consultation> consultationsById
    ) {
        if (followUps.isEmpty()) {
            return List.of();
        }

        final Set<UUID> doctorIds = followUps.stream()
                .map(FollowUp::getDoctorId)
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

        return followUps.stream()
                .map(followUp -> {
                    final Consultation consultation = consultationsById.get(followUp.getConsultationId());
                    final Doctor doctor = doctors.get(followUp.getDoctorId());
                    final User doctorUser = doctor == null ? null : users.get(doctor.getUserId());
                    return followUpMapper.toResponse(
                            followUp,
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
