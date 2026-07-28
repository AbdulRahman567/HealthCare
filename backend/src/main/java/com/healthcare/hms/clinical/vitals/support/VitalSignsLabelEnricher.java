package com.healthcare.hms.clinical.vitals.support;

import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.entity.VitalSigns;
import com.healthcare.hms.clinical.vitals.dto.response.VitalSignsResponse;
import com.healthcare.hms.clinical.vitals.mapper.VitalSignsMapper;
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
public class VitalSignsLabelEnricher {

    private final UserRepository userRepository;
    private final VitalSignsMapper vitalSignsMapper;

    public VitalSignsLabelEnricher(final UserRepository userRepository, final VitalSignsMapper vitalSignsMapper) {
        this.userRepository = userRepository;
        this.vitalSignsMapper = vitalSignsMapper;
    }

    public VitalSignsResponse enrichOne(
            final UUID tenantId,
            final VitalSigns vitalSigns,
            final Consultation consultation
    ) {
        return enrich(tenantId, List.of(vitalSigns), consultation == null ? Map.of() : Map.of(consultation.getId(), consultation))
                .getFirst();
    }

    public List<VitalSignsResponse> enrich(
            final UUID tenantId,
            final Collection<VitalSigns> vitalSignsList,
            final Map<UUID, Consultation> consultationsById
    ) {
        if (vitalSignsList.isEmpty()) {
            return List.of();
        }

        final Set<UUID> userIds = vitalSignsList.stream()
                .map(VitalSigns::getRecordedByUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        final Map<UUID, User> users = userIds.isEmpty()
                ? Map.of()
                : userRepository.findByTenantIdAndIdIn(tenantId, userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, HashMap::new));

        return vitalSignsList.stream()
                .map(vitalSigns -> {
                    final Consultation consultation = consultationsById.get(vitalSigns.getConsultationId());
                    final User recorder = vitalSigns.getRecordedByUserId() == null
                            ? null
                            : users.get(vitalSigns.getRecordedByUserId());
                    return vitalSignsMapper.toResponse(
                            vitalSigns,
                            consultation == null ? null : consultation.getConsultationNumber(),
                            recorder == null ? null : displayName(recorder.getFirstName(), recorder.getLastName())
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
