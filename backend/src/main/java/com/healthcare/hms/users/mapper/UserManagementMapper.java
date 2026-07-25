package com.healthcare.hms.users.mapper;

import com.healthcare.hms.users.dto.response.UserManagementResponse;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserManagementMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleTypes")
    UserManagementResponse toResponse(User user);

    @Named("roleTypes")
    default Set<String> roleTypes(final Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getType().name())
                .collect(Collectors.toUnmodifiableSet());
    }
}
