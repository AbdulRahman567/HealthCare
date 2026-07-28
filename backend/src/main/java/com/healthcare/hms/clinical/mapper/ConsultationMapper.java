package com.healthcare.hms.clinical.mapper;

import com.healthcare.hms.clinical.dto.request.CompleteConsultationRequest;
import com.healthcare.hms.clinical.dto.request.CreateConsultationRequest;
import com.healthcare.hms.clinical.dto.request.UpdateConsultationDocumentationRequest;
import com.healthcare.hms.clinical.dto.response.ClinicalSummaryResponse;
import com.healthcare.hms.clinical.dto.response.ConsultationResponse;
import com.healthcare.hms.clinical.entity.Consultation;
import org.springframework.stereotype.Component;

@Component
public class ConsultationMapper {

    public void applyCreate(final CreateConsultationRequest request, final Consultation consultation) {
        consultation.setPatientId(request.patientId());
        consultation.setDoctorId(request.doctorId());
        consultation.setDepartmentId(request.departmentId());
        consultation.setAppointmentId(request.appointmentId());
        consultation.setChiefComplaint(trimToNull(request.chiefComplaint()));
    }

    public void applyDocumentation(
            final UpdateConsultationDocumentationRequest request,
            final Consultation consultation
    ) {
        if (request.chiefComplaint() != null) {
            consultation.setChiefComplaint(trimToNull(request.chiefComplaint()));
        }
        if (request.historyOfPresentIllness() != null) {
            consultation.setHistoryOfPresentIllness(trimToNull(request.historyOfPresentIllness()));
        }
        if (request.physicalExamination() != null) {
            consultation.setPhysicalExamination(trimToNull(request.physicalExamination()));
        }
        if (request.doctorNotes() != null) {
            consultation.setDoctorNotes(trimToNull(request.doctorNotes()));
        }
        if (request.summary() != null) {
            consultation.setSummary(trimToNull(request.summary()));
        }
        if (request.advice() != null) {
            consultation.setAdvice(trimToNull(request.advice()));
        }
    }

    public void applyComplete(final CompleteConsultationRequest request, final Consultation consultation) {
        if (request == null) {
            return;
        }
        if (request.summary() != null) {
            consultation.setSummary(trimToNull(request.summary()));
        }
        if (request.advice() != null) {
            consultation.setAdvice(trimToNull(request.advice()));
        }
    }

    public ClinicalSummaryResponse toClinicalSummary(final Consultation consultation) {
        return new ClinicalSummaryResponse(
                consultation.getChiefComplaint(),
                consultation.getHistoryOfPresentIllness(),
                consultation.getPhysicalExamination(),
                consultation.getDoctorNotes(),
                consultation.getSummary(),
                consultation.getAdvice()
        );
    }

    public ConsultationResponse toResponse(final Consultation consultation) {
        return toResponse(consultation, null, null, null, null, null);
    }

    public ConsultationResponse toResponse(
            final Consultation consultation,
            final String patientName,
            final String patientMrn,
            final String doctorName,
            final String departmentName
    ) {
        return toResponse(consultation, patientName, patientMrn, doctorName, departmentName, toClinicalSummary(consultation));
    }

    public ConsultationResponse toResponse(
            final Consultation consultation,
            final String patientName,
            final String patientMrn,
            final String doctorName,
            final String departmentName,
            final ClinicalSummaryResponse clinicalSummary
    ) {
        return new ConsultationResponse(
                consultation.getId(),
                consultation.getConsultationNumber(),
                consultation.getHospitalId(),
                consultation.getPatientId(),
                patientName,
                patientMrn,
                consultation.getDoctorId(),
                doctorName,
                consultation.getDepartmentId(),
                departmentName,
                consultation.getAppointmentId(),
                consultation.getConsultationDate(),
                consultation.getStatus(),
                consultation.getStartedAt(),
                consultation.getPausedAt(),
                consultation.getCompletedAt(),
                clinicalSummary,
                consultation.getCreatedAt(),
                consultation.getUpdatedAt(),
                consultation.getVersion()
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
