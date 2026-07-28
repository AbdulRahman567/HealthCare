package com.healthcare.hms.clinical.vitals.mapper;

import com.healthcare.hms.clinical.entity.VitalSigns;
import com.healthcare.hms.clinical.vitals.dto.request.RecordVitalSignsRequest;
import com.healthcare.hms.clinical.vitals.dto.request.UpdateVitalSignsRequest;
import com.healthcare.hms.clinical.vitals.dto.response.BloodPressureResponse;
import com.healthcare.hms.clinical.vitals.dto.response.VitalSignsResponse;
import com.healthcare.hms.clinical.vitals.support.VitalSignsCalculator;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class VitalSignsMapper {

    public void applyRecord(
            final RecordVitalSignsRequest request,
            final VitalSigns entity,
            final Instant recordedAt
    ) {
        entity.setTemperatureCelsius(request.temperatureCelsius());
        entity.setPulseBpm(request.heartRateBpm());
        entity.setSystolicBp(request.systolicBp());
        entity.setDiastolicBp(request.diastolicBp());
        entity.setRespiratoryRate(request.respiratoryRate());
        entity.setOxygenSaturationPercent(request.oxygenSaturationPercent());
        entity.setHeightCm(request.heightCm());
        entity.setWeightKg(request.weightKg());
        entity.setPainScale(request.painScale());
        entity.setNotes(trimToNull(request.notes()));
        entity.setRecordedAt(recordedAt);
        entity.setBmi(VitalSignsCalculator.computeBmi(request.heightCm(), request.weightKg()));
    }

    public void applyUpdate(final UpdateVitalSignsRequest request, final VitalSigns entity) {
        if (request.temperatureCelsius() != null) {
            entity.setTemperatureCelsius(request.temperatureCelsius());
        }
        if (request.heartRateBpm() != null) {
            entity.setPulseBpm(request.heartRateBpm());
        }
        if (request.systolicBp() != null) {
            entity.setSystolicBp(request.systolicBp());
        }
        if (request.diastolicBp() != null) {
            entity.setDiastolicBp(request.diastolicBp());
        }
        if (request.respiratoryRate() != null) {
            entity.setRespiratoryRate(request.respiratoryRate());
        }
        if (request.oxygenSaturationPercent() != null) {
            entity.setOxygenSaturationPercent(request.oxygenSaturationPercent());
        }
        if (request.heightCm() != null) {
            entity.setHeightCm(request.heightCm());
        }
        if (request.weightKg() != null) {
            entity.setWeightKg(request.weightKg());
        }
        if (request.painScale() != null) {
            entity.setPainScale(request.painScale());
        }
        if (request.notes() != null) {
            entity.setNotes(trimToNull(request.notes()));
        }
        if (request.recordedAt() != null) {
            entity.setRecordedAt(request.recordedAt());
        }
        entity.setBmi(VitalSignsCalculator.computeBmi(entity.getHeightCm(), entity.getWeightKg()));
    }

    public VitalSignsResponse toResponse(final VitalSigns entity) {
        return toResponse(entity, null, null);
    }

    public VitalSignsResponse toResponse(
            final VitalSigns entity,
            final String consultationNumber,
            final String recordedByName
    ) {
        final BloodPressureResponse bloodPressure = entity.getSystolicBp() == null && entity.getDiastolicBp() == null
                ? null
                : new BloodPressureResponse(entity.getSystolicBp(), entity.getDiastolicBp());

        return new VitalSignsResponse(
                entity.getId(),
                entity.getConsultationId(),
                consultationNumber,
                entity.getPatientId(),
                entity.getRecordedAt(),
                entity.getRecordedByUserId(),
                recordedByName,
                entity.getTemperatureCelsius(),
                entity.getPulseBpm(),
                bloodPressure,
                entity.getRespiratoryRate(),
                entity.getOxygenSaturationPercent(),
                entity.getHeightCm(),
                entity.getWeightKg(),
                entity.getBmi(),
                entity.getPainScale(),
                entity.getNotes(),
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
