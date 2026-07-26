package com.healthcare.hms.patients.allergy.mapper;

import com.healthcare.hms.patients.allergy.dto.request.UpsertAllergyRequest;
import com.healthcare.hms.patients.allergy.dto.response.AllergyResponse;
import com.healthcare.hms.patients.allergy.entity.Allergy;
import com.healthcare.hms.patients.allergy.enums.AllergyStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AllergyMapper {

    @Mapping(target = "lifeThreatening", expression = "java(allergy.isLifeThreatening())")
    AllergyResponse toResponse(Allergy allergy);

    @Mapping(target = "allergenName", expression = "java(request.allergenName().trim())")
    @Mapping(target = "allergenCode", source = "allergenCode", qualifiedByName = "trimToNull")
    @Mapping(target = "clinicalNotes", source = "clinicalNotes", qualifiedByName = "trimToNull")
    @Mapping(target = "status", expression = "java(resolveStatus(request.status(), allergy))")
    @Mapping(target = "verified", expression = "java(resolveFlag(request.verified(), allergy.isVerified()))")
    @Mapping(
            target = "patientReported",
            expression = "java(resolveFlag(request.patientReported(), allergy.isPatientReported()))"
    )
    @Mapping(
            target = "criticalAlert",
            expression = "java(resolveFlag(request.criticalAlert(), allergy.isCriticalAlert()))"
    )
    @Mapping(
            target = "showOnBanner",
            expression = "java(resolveFlag(request.showOnBanner(), allergy.isShowOnBanner()))"
    )
    @Mapping(target = "patientId", ignore = true)
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
    void apply(UpsertAllergyRequest request, @MappingTarget Allergy allergy);

    @AfterMapping
    default void enforceAlertRules(@MappingTarget final Allergy allergy) {
        allergy.applyClinicalAlertRules();
    }

    /**
     * Create: null → ACTIVE. Update: null preserves the existing status
     * (avoids accidental clinical-flag resets when fields are omitted).
     */
    default AllergyStatus resolveStatus(final AllergyStatus status, final Allergy allergy) {
        if (status != null) {
            return status;
        }
        return allergy.getStatus() != null ? allergy.getStatus() : AllergyStatus.ACTIVE;
    }

    /**
     * Patch semantics for optional booleans: omitted ({@code null}) keeps the
     * existing entity value. Required so UPDATE cannot silently clear
     * {@code criticalAlert} / banner flags without an explicit {@code false}.
     */
    default boolean resolveFlag(final Boolean value, final boolean existing) {
        return value != null ? value : existing;
    }

    @Named("trimToNull")
    default String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
