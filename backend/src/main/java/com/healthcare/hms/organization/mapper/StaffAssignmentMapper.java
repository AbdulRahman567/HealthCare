package com.healthcare.hms.organization.mapper;

import com.healthcare.hms.organization.dto.response.StaffAssignmentResponse;
import com.healthcare.hms.organization.entity.StaffDepartmentAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaffAssignmentMapper {

    @Mapping(target = "open", expression = "java(assignment.isOpen())")
    StaffAssignmentResponse toResponse(StaffDepartmentAssignment assignment);
}
