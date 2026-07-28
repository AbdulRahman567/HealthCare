package com.healthcare.hms.patients.history.mapper;

import com.healthcare.hms.patients.history.dto.request.UpsertChronicConditionRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertFamilyHistoryRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertPastDiseaseRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertSurgeryHistoryRequest;
import com.healthcare.hms.patients.history.dto.response.ChronicConditionResponse;
import com.healthcare.hms.patients.history.dto.response.FamilyHistoryResponse;
import com.healthcare.hms.patients.history.dto.response.MedicalHistoryResponse;
import com.healthcare.hms.patients.history.dto.response.PastDiseaseResponse;
import com.healthcare.hms.patients.history.dto.response.SurgeryHistoryResponse;
import com.healthcare.hms.patients.history.entity.ChronicCondition;
import com.healthcare.hms.patients.history.entity.FamilyHistory;
import com.healthcare.hms.patients.history.entity.MedicalHistory;
import com.healthcare.hms.patients.history.entity.PastDisease;
import com.healthcare.hms.patients.history.entity.SurgeryHistory;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicalHistoryMapper {

    PastDiseaseResponse toPastDiseaseResponse(PastDisease entity);

    SurgeryHistoryResponse toSurgeryHistoryResponse(SurgeryHistory entity);

    ChronicConditionResponse toChronicConditionResponse(ChronicCondition entity);

    FamilyHistoryResponse toFamilyHistoryResponse(FamilyHistory entity);

    default MedicalHistoryResponse toMedicalHistoryResponse(
            final MedicalHistory history,
            final List<PastDisease> pastDiseases,
            final List<SurgeryHistory> surgeries,
            final List<ChronicCondition> chronicConditions,
            final List<FamilyHistory> familyHistories
    ) {
        return new MedicalHistoryResponse(
                history.getId(),
                history.getPatientId(),
                history.getLastReviewedAt(),
                history.getLastReviewedBy(),
                pastDiseases.stream().map(this::toPastDiseaseResponse).toList(),
                surgeries.stream().map(this::toSurgeryHistoryResponse).toList(),
                chronicConditions.stream().map(this::toChronicConditionResponse).toList(),
                familyHistories.stream().map(this::toFamilyHistoryResponse).toList(),
                history.getCreatedAt(),
                history.getUpdatedAt(),
                history.getVersion()
        );
    }

    @Mapping(target = "diseaseName", expression = "java(request.diseaseName().trim())")
    @Mapping(target = "diseaseCode", source = "diseaseCode", qualifiedByName = "trimToNull")
    @Mapping(target = "clinicalNotes", source = "clinicalNotes", qualifiedByName = "trimToNull")
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "medicalHistoryId", ignore = true)
    @Mapping(target = "recordedByUserId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applyPastDisease(UpsertPastDiseaseRequest request, @MappingTarget PastDisease entity);

    @Mapping(target = "procedureName", expression = "java(request.procedureName().trim())")
    @Mapping(target = "procedureCode", source = "procedureCode", qualifiedByName = "trimToNull")
    @Mapping(target = "performingFacility", source = "performingFacility", qualifiedByName = "trimToNull")
    @Mapping(target = "clinicalNotes", source = "clinicalNotes", qualifiedByName = "trimToNull")
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "medicalHistoryId", ignore = true)
    @Mapping(target = "recordedByUserId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applySurgery(UpsertSurgeryHistoryRequest request, @MappingTarget SurgeryHistory entity);

    @Mapping(target = "conditionName", expression = "java(request.conditionName().trim())")
    @Mapping(target = "conditionCode", source = "conditionCode", qualifiedByName = "trimToNull")
    @Mapping(target = "clinicalNotes", source = "clinicalNotes", qualifiedByName = "trimToNull")
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "medicalHistoryId", ignore = true)
    @Mapping(target = "recordedByUserId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applyChronicCondition(UpsertChronicConditionRequest request, @MappingTarget ChronicCondition entity);

    @Mapping(target = "diseaseName", expression = "java(request.diseaseName().trim())")
    @Mapping(target = "diseaseCode", source = "diseaseCode", qualifiedByName = "trimToNull")
    @Mapping(target = "clinicalNotes", source = "clinicalNotes", qualifiedByName = "trimToNull")
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "medicalHistoryId", ignore = true)
    @Mapping(target = "recordedByUserId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applyFamilyHistory(UpsertFamilyHistoryRequest request, @MappingTarget FamilyHistory entity);

    @Named("trimToNull")
    default String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
