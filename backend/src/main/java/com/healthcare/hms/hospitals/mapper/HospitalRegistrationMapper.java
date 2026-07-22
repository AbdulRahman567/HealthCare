package com.healthcare.hms.hospitals.mapper;

import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.users.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Maps registration aggregates into the public registration response.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HospitalRegistrationMapper {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantSlug", source = "tenant.slug")
    @Mapping(target = "tenantStatus", source = "tenant.status")
    @Mapping(target = "hospitalId", source = "hospital.id")
    @Mapping(target = "hospitalName", source = "hospital.name")
    @Mapping(target = "hospitalCode", source = "hospital.code")
    @Mapping(target = "hospitalStatus", source = "hospital.status")
    @Mapping(target = "defaultHospital", source = "hospital.defaultHospital")
    @Mapping(target = "hospitalEmail", source = "hospital.email")
    @Mapping(target = "hospitalPhone", source = "hospital.phone")
    @Mapping(target = "hospitalAddress", source = "hospital.address")
    @Mapping(target = "subscriptionPlan", source = "tenant.subscriptionPlan")
    @Mapping(target = "adminUserId", source = "admin.id")
    @Mapping(target = "adminEmail", source = "admin.email")
    @Mapping(target = "adminEmailVerified", source = "admin.emailVerified")
    @Mapping(target = "provisionedRoles", source = "provisionedRoles")
    @Mapping(target = "createdAt", source = "tenant.createdAt")
    HospitalRegistrationResponse toResponse(
            Tenant tenant,
            Hospital hospital,
            User admin,
            List<String> provisionedRoles
    );
}
