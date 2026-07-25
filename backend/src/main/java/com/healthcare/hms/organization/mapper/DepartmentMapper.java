package com.healthcare.hms.organization.mapper;

import com.healthcare.hms.organization.dto.request.CreateDepartmentRequest;
import com.healthcare.hms.organization.dto.request.UpdateDepartmentRequest;
import com.healthcare.hms.organization.dto.response.DepartmentResponse;
import com.healthcare.hms.organization.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * Maps department persistence models to API DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    DepartmentResponse toResponse(Department department);

    @Mapping(target = "name", expression = "java(request.name().trim())")
    @Mapping(target = "code", expression = "java(normalizeCode(request.code()))")
    @Mapping(target = "description", source = "description", qualifiedByName = "trimToNull")
    @Mapping(target = "location", source = "location", qualifiedByName = "trimToNull")
    @Mapping(target = "departmentType", source = "departmentType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "headUserId", ignore = true)
    @Mapping(target = "headStaffId", ignore = true)
    @Mapping(target = "headStaffType", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "hospitalId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applyCreate(CreateDepartmentRequest request, @MappingTarget Department department);

    @Mapping(target = "name", expression = "java(request.name().trim())")
    @Mapping(target = "code", expression = "java(normalizeCode(request.code()))")
    @Mapping(target = "description", source = "description", qualifiedByName = "trimToNull")
    @Mapping(target = "location", source = "location", qualifiedByName = "trimToNull")
    @Mapping(target = "departmentType", source = "departmentType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "headUserId", ignore = true)
    @Mapping(target = "headStaffId", ignore = true)
    @Mapping(target = "headStaffType", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "hospitalId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applyUpdate(UpdateDepartmentRequest request, @MappingTarget Department department);

    @Named("trimToNull")
    default String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    default String normalizeCode(final String code) {
        return code.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
