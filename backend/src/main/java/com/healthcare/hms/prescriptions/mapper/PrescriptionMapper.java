package com.healthcare.hms.prescriptions.mapper;

import com.healthcare.hms.prescriptions.dto.request.PrescriptionItemRequest;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionItemResponse;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionResponse;
import com.healthcare.hms.prescriptions.entity.Prescription;
import com.healthcare.hms.prescriptions.entity.PrescriptionItem;
import com.healthcare.hms.prescriptions.validation.PrescriptionClinicalRules;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionMapper {

    public void applyItem(
            final PrescriptionItemRequest request,
            final PrescriptionItem entity,
            final int sequenceNumber
    ) {
        final String medicineName = request.medicineName().trim();
        entity.setMedicineName(medicineName);
        entity.setMedicineNameKey(PrescriptionClinicalRules.normalizeMedicineKey(medicineName));
        entity.setMedicineId(request.medicineId());
        entity.setMedicineCode(trimToNull(request.medicineCode()));
        entity.setDosage(request.dosage().trim());
        entity.setFrequency(request.frequency().trim());
        entity.setRoute(request.route());
        entity.setDuration(request.duration().trim());
        entity.setInstructions(trimToNull(request.instructions()));
        entity.setQuantity(request.quantity());
        entity.setRefills(request.refills() != null ? request.refills() : 0);
        entity.setSequenceNumber(sequenceNumber);
        entity.setBeforeFood(Boolean.TRUE.equals(request.beforeFood()));
        entity.setAfterFood(Boolean.TRUE.equals(request.afterFood()));
    }

    public PrescriptionItemResponse toItemResponse(final PrescriptionItem entity) {
        return new PrescriptionItemResponse(
                entity.getId(),
                entity.getPrescriptionId(),
                entity.getMedicineName(),
                entity.getMedicineId(),
                entity.getMedicineCode(),
                entity.getDosage(),
                entity.getFrequency(),
                entity.getRoute(),
                entity.getDuration(),
                entity.getInstructions(),
                entity.getQuantity(),
                entity.getRefills(),
                entity.getSequenceNumber(),
                entity.getBeforeFood(),
                entity.getAfterFood(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    public PrescriptionResponse toResponse(
            final Prescription prescription,
            final List<PrescriptionItemResponse> items,
            final String consultationNumber,
            final String patientName,
            final String patientMrn,
            final String doctorName,
            final String departmentName
    ) {
        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPrescriptionNumber(),
                prescription.getConsultationId(),
                consultationNumber,
                prescription.getHospitalId(),
                prescription.getPatientId(),
                patientName,
                patientMrn,
                prescription.getDoctorId(),
                doctorName,
                prescription.getDepartmentId(),
                departmentName,
                prescription.getPrescriptionDate(),
                prescription.getStatus(),
                prescription.getNotes(),
                prescription.getIssuedAt(),
                prescription.getCancelledAt(),
                prescription.getCancelReason(),
                prescription.getDispensedAt(),
                prescription.getPharmacyReference(),
                items,
                prescription.getCreatedAt(),
                prescription.getUpdatedAt(),
                prescription.getVersion()
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
