package com.healthcare.hms.clinical.diagnosis.mapper;

import com.healthcare.hms.clinical.diagnosis.dto.request.CreateDiagnosisRequest;
import com.healthcare.hms.clinical.diagnosis.dto.request.UpdateDiagnosisRequest;
import com.healthcare.hms.clinical.diagnosis.dto.response.DiagnosisResponse;
import com.healthcare.hms.clinical.diagnosis.validation.DiagnosisClinicalRules;
import com.healthcare.hms.clinical.entity.Diagnosis;
import com.healthcare.hms.clinical.enums.DiagnosisSeverity;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisMapper {

    public void applyCreate(
            final CreateDiagnosisRequest request,
            final Diagnosis entity,
            final UUID diagnosingDoctorId,
            final int sequenceNumber,
            final Instant diagnosedAt
    ) {
        entity.setDiagnosisName(request.diagnosisName().trim());
        entity.setIcdCode(DiagnosisClinicalRules.normalizeIcd10Code(request.icdCode()));
        entity.setDiagnosisType(request.diagnosisType());
        entity.setDiagnosisStatus(request.diagnosisStatus() != null
                ? request.diagnosisStatus()
                : DiagnosisStatus.PROVISIONAL);
        entity.setSeverity(request.severity() != null ? request.severity() : DiagnosisSeverity.UNKNOWN);
        entity.setSequenceNumber(sequenceNumber);
        entity.setClinicalNotes(trimToNull(request.clinicalNotes()));
        entity.setDiagnosingDoctorId(diagnosingDoctorId);
        entity.setDiagnosedAt(diagnosedAt);
    }

    public void applyUpdate(final UpdateDiagnosisRequest request, final Diagnosis entity) {
        if (request.diagnosisName() != null) {
            entity.setDiagnosisName(request.diagnosisName().trim());
        }
        if (request.icdCode() != null) {
            entity.setIcdCode(DiagnosisClinicalRules.normalizeIcd10Code(request.icdCode()));
        }
        if (request.diagnosisType() != null) {
            entity.setDiagnosisType(request.diagnosisType());
        }
        if (request.diagnosisStatus() != null) {
            entity.setDiagnosisStatus(request.diagnosisStatus());
        }
        if (request.severity() != null) {
            entity.setSeverity(request.severity());
        }
        if (request.sequenceNumber() != null) {
            entity.setSequenceNumber(request.sequenceNumber());
        }
        if (request.clinicalNotes() != null) {
            entity.setClinicalNotes(trimToNull(request.clinicalNotes()));
        }
        if (request.diagnosingDoctorId() != null) {
            entity.setDiagnosingDoctorId(request.diagnosingDoctorId());
        }
        if (request.diagnosedAt() != null) {
            entity.setDiagnosedAt(request.diagnosedAt());
        }
    }

    public DiagnosisResponse toResponse(final Diagnosis entity) {
        return toResponse(entity, null, null);
    }

    public DiagnosisResponse toResponse(
            final Diagnosis entity,
            final String consultationNumber,
            final String diagnosingDoctorName
    ) {
        return new DiagnosisResponse(
                entity.getId(),
                entity.getConsultationId(),
                consultationNumber,
                entity.getPatientId(),
                entity.getDiagnosingDoctorId(),
                diagnosingDoctorName,
                entity.getDiagnosisName(),
                entity.getIcdCode(),
                entity.getDiagnosisType(),
                entity.getDiagnosisStatus(),
                entity.getSeverity(),
                entity.getDiagnosedAt(),
                entity.getSequenceNumber(),
                entity.getClinicalNotes(),
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
