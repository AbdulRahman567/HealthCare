package com.healthcare.hms.patients.immunization.mapper;

import com.healthcare.hms.patients.immunization.dto.request.UpsertImmunizationRequest;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationResponse;
import com.healthcare.hms.patients.immunization.entity.Immunization;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.enums.VaccineRoute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImmunizationMapper {

    @Mapping(target = "due", expression = "java(immunization.isDueOnOrBefore(java.time.LocalDate.now()))")
    ImmunizationResponse toResponse(Immunization immunization);

    @Mapping(target = "vaccineName", expression = "java(request.vaccineName().trim())")
    @Mapping(target = "vaccineCode", source = "vaccineCode", qualifiedByName = "trimToNull")
    @Mapping(target = "manufacturer", source = "manufacturer", qualifiedByName = "trimToNull")
    @Mapping(target = "batchNumber", source = "batchNumber", qualifiedByName = "trimToNull")
    @Mapping(target = "healthcareProvider", expression = "java(request.healthcareProvider().trim())")
    @Mapping(target = "clinicalNotes", source = "clinicalNotes", qualifiedByName = "trimToNull")
    @Mapping(target = "status", expression = "java(defaultStatus(request.status()))")
    @Mapping(target = "route", expression = "java(defaultRoute(request.route()))")
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
    void apply(UpsertImmunizationRequest request, @MappingTarget Immunization immunization);

    default ImmunizationStatus defaultStatus(final ImmunizationStatus status) {
        return status != null ? status : ImmunizationStatus.ADMINISTERED;
    }

    default VaccineRoute defaultRoute(final VaccineRoute route) {
        return route != null ? route : VaccineRoute.UNKNOWN;
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
