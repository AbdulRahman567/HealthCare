package com.healthcare.hms.auth.mapper;

import com.healthcare.hms.auth.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.auth.dto.response.UserProfileResponse;
import com.healthcare.hms.hospitals.entity.Tenant;
import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for authentication-related response DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoleTypes")
    @Mapping(target = "permissions", source = "roles", qualifiedByName = "mapPermissions")
    UserProfileResponse toUserProfile(User user);

    @Mapping(target = "tenantId", source = "id")
    HospitalRegistrationResponse toHospitalRegistration(Tenant tenant);

    @Named("mapRoleTypes")
    default Set<String> mapRoleTypes(final Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getType().name())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Named("mapPermissions")
    default Set<String> mapPermissions(final Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }
}
