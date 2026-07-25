package com.healthcare.hms.users.mapper;

import com.healthcare.hms.users.dto.response.UserInvitationResponse;
import com.healthcare.hms.users.entity.UserInvitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserInvitationMapper {

    @Mapping(target = "expired", expression = "java(invitation.isExpired())")
    UserInvitationResponse toResponse(UserInvitation invitation);
}
