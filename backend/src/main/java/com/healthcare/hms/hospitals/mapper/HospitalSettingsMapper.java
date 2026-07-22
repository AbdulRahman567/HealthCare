package com.healthcare.hms.hospitals.mapper;

import com.healthcare.hms.hospitals.dto.request.UpdateHospitalSettingsRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalSettingsResponse;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.model.WorkingDayHours;
import com.healthcare.hms.hospitals.model.WorkingHours;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * Maps hospital settings between persistence and API models.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HospitalSettingsMapper {

    @Mapping(target = "hospitalId", source = "id")
    HospitalSettingsResponse toResponse(Hospital hospital);

    @Mapping(target = "name", expression = "java(request.name().trim())")
    @Mapping(target = "description", source = "description", qualifiedByName = "trimToNull")
    @Mapping(target = "logoUrl", source = "logoUrl", qualifiedByName = "trimToNull")
    @Mapping(target = "timezone", expression = "java(request.timezone().trim())")
    @Mapping(target = "currency", expression = "java(request.currency().trim().toUpperCase(java.util.Locale.ROOT))")
    @Mapping(target = "language", expression = "java(normalizeLanguage(request.language()))")
    @Mapping(target = "email", expression = "java(request.email().trim().toLowerCase(java.util.Locale.ROOT))")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "trimToNull")
    @Mapping(target = "secondaryPhone", source = "secondaryPhone", qualifiedByName = "trimToNull")
    @Mapping(target = "website", source = "website", qualifiedByName = "trimToNull")
    @Mapping(target = "address", source = "address", qualifiedByName = "trimToNull")
    @Mapping(target = "city", source = "city", qualifiedByName = "trimToNull")
    @Mapping(target = "stateProvince", source = "stateProvince", qualifiedByName = "trimToNull")
    @Mapping(target = "country", source = "country", qualifiedByName = "trimToNull")
    @Mapping(target = "postalCode", source = "postalCode", qualifiedByName = "trimToNull")
    @Mapping(target = "workingHours", expression = "java(copyWorkingHours(request.workingHours()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "defaultHospital", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateHospitalSettingsRequest request, @MappingTarget Hospital hospital);

    @Named("trimToNull")
    default String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    default String normalizeLanguage(final String language) {
        final String trimmed = language.trim();
        final int separator = trimmed.indexOf('-');
        if (separator < 0) {
            return trimmed.toLowerCase(java.util.Locale.ROOT);
        }
        return trimmed.substring(0, separator).toLowerCase(java.util.Locale.ROOT)
                + trimmed.substring(separator).toUpperCase(java.util.Locale.ROOT);
    }

    default WorkingHours copyWorkingHours(final WorkingHours source) {
        if (source == null) {
            return null;
        }
        return new WorkingHours(
                copyDay(source.getMonday()),
                copyDay(source.getTuesday()),
                copyDay(source.getWednesday()),
                copyDay(source.getThursday()),
                copyDay(source.getFriday()),
                copyDay(source.getSaturday()),
                copyDay(source.getSunday())
        );
    }

    private WorkingDayHours copyDay(final WorkingDayHours day) {
        if (day == null) {
            return null;
        }
        return new WorkingDayHours(
                day.isClosed(),
                trimToNull(day.getOpen()),
                trimToNull(day.getClose())
        );
    }
}
