package com.healthcare.hms.clinical.notes.support;

import com.healthcare.hms.clinical.entity.ClinicalNote;
import com.healthcare.hms.clinical.entity.ClinicalNoteAttachment;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteAttachmentResponse;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteResponse;
import com.healthcare.hms.clinical.notes.mapper.ClinicalNoteMapper;
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
public class ClinicalNoteLabelEnricher {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final ClinicalNoteMapper clinicalNoteMapper;

    public ClinicalNoteLabelEnricher(
            final DoctorRepository doctorRepository,
            final UserRepository userRepository,
            final ClinicalNoteMapper clinicalNoteMapper
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.clinicalNoteMapper = clinicalNoteMapper;
    }

    public ClinicalNoteResponse enrichOne(
            final UUID tenantId,
            final ClinicalNote note,
            final Consultation consultation,
            final List<ClinicalNoteAttachment> attachments
    ) {
        return enrich(
                tenantId,
                List.of(note),
                consultation == null ? Map.of() : Map.of(consultation.getId(), consultation),
                Map.of(note.getId(), attachments == null ? List.of() : attachments)
        ).getFirst();
    }

    public List<ClinicalNoteResponse> enrich(
            final UUID tenantId,
            final Collection<ClinicalNote> notes,
            final Map<UUID, Consultation> consultationsById,
            final Map<UUID, List<ClinicalNoteAttachment>> attachmentsByNoteId
    ) {
        if (notes.isEmpty()) {
            return List.of();
        }

        final Set<UUID> doctorIds = notes.stream().map(ClinicalNote::getAuthorDoctorId).collect(Collectors.toSet());
        final Map<UUID, Doctor> doctors = doctorRepository.findByTenantIdAndIdIn(tenantId, doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Set<UUID> userIds = doctors.values().stream().map(Doctor::getUserId).collect(Collectors.toSet());
        final Map<UUID, User> users = userIds.isEmpty()
                ? Map.of()
                : userRepository.findByTenantIdAndIdIn(tenantId, userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, HashMap::new));

        return notes.stream()
                .map(note -> {
                    final Consultation consultation = consultationsById.get(note.getConsultationId());
                    final Doctor doctor = doctors.get(note.getAuthorDoctorId());
                    final User doctorUser = doctor == null ? null : users.get(doctor.getUserId());
                    final List<ClinicalNoteAttachmentResponse> attachments = attachmentsByNoteId
                            .getOrDefault(note.getId(), List.of())
                            .stream()
                            .map(clinicalNoteMapper::toAttachmentResponse)
                            .toList();
                    return clinicalNoteMapper.toResponse(
                            note,
                            attachments,
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
