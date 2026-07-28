package com.healthcare.hms.clinical.followup.mapper;

import com.healthcare.hms.clinical.entity.FollowUp;
import com.healthcare.hms.clinical.enums.FollowUpPriority;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import com.healthcare.hms.clinical.followup.dto.request.CreateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.request.UpdateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.response.FollowUpResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FollowUpMapper {

    public void applyCreate(
            final CreateFollowUpRequest request,
            final FollowUp entity,
            final UUID doctorId
    ) {
        entity.setScheduledDate(request.scheduledDate());
        entity.setScheduledTime(request.scheduledTime());
        entity.setStatus(request.status() != null ? request.status() : FollowUpStatus.PENDING);
        entity.setPriority(request.priority() != null ? request.priority() : FollowUpPriority.ROUTINE);
        entity.setReason(trimToNull(request.reason()));
        entity.setInstructions(trimToNull(request.instructions()));
        entity.setClinicalRecommendations(trimToNull(request.clinicalRecommendations()));
        entity.setDoctorId(doctorId);
        entity.setFollowUpAppointmentId(request.followUpAppointmentId());
        entity.setReminderEnabled(request.reminderEnabled() != null ? request.reminderEnabled() : true);
        entity.setReminderLeadDays(request.reminderLeadDays() != null ? request.reminderLeadDays() : 1);
    }

    public void applyUpdate(final UpdateFollowUpRequest request, final FollowUp entity) {
        if (request.scheduledDate() != null) {
            entity.setScheduledDate(request.scheduledDate());
        }
        if (request.scheduledTime() != null) {
            entity.setScheduledTime(request.scheduledTime());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.priority() != null) {
            entity.setPriority(request.priority());
        }
        if (request.reason() != null) {
            entity.setReason(trimToNull(request.reason()));
        }
        if (request.instructions() != null) {
            entity.setInstructions(trimToNull(request.instructions()));
        }
        if (request.clinicalRecommendations() != null) {
            entity.setClinicalRecommendations(trimToNull(request.clinicalRecommendations()));
        }
        if (request.doctorId() != null) {
            entity.setDoctorId(request.doctorId());
        }
        if (request.followUpAppointmentId() != null) {
            entity.setFollowUpAppointmentId(request.followUpAppointmentId());
        }
        if (request.reminderEnabled() != null) {
            entity.setReminderEnabled(request.reminderEnabled());
        }
        if (request.reminderLeadDays() != null) {
            entity.setReminderLeadDays(request.reminderLeadDays());
        }
    }

    public FollowUpResponse toResponse(final FollowUp entity) {
        return toResponse(entity, null, null);
    }

    public FollowUpResponse toResponse(
            final FollowUp entity,
            final String consultationNumber,
            final String doctorName
    ) {
        return new FollowUpResponse(
                entity.getId(),
                entity.getConsultationId(),
                consultationNumber,
                entity.getPatientId(),
                entity.getDoctorId(),
                doctorName,
                entity.getScheduledDate(),
                entity.getScheduledTime(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getReason(),
                entity.getInstructions(),
                entity.getClinicalRecommendations(),
                entity.getFollowUpAppointmentId(),
                entity.getReminderEnabled(),
                entity.getReminderLeadDays(),
                entity.getNextReminderAt(),
                entity.getLastReminderAt(),
                entity.getReminderStatus(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
