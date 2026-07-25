package com.healthcare.hms.users.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.users.dto.request.AcceptInvitationRequest;
import com.healthcare.hms.users.dto.request.CreateInvitationRequest;
import com.healthcare.hms.users.dto.request.RejectInvitationRequest;
import com.healthcare.hms.users.dto.response.AcceptInvitationResponse;
import com.healthcare.hms.users.dto.response.InvitationPreviewResponse;
import com.healthcare.hms.users.dto.response.UserInvitationResponse;
import com.healthcare.hms.users.enums.InvitationStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * User invitation lifecycle (Phase 4.5).
 */
public interface UserInvitationService {

    UserInvitationResponse invite(CreateInvitationRequest request, String ipAddress, String userAgent);

    UserInvitationResponse getById(UUID invitationId);

    PageResponse<UserInvitationResponse> search(InvitationStatus status, String email, Pageable pageable);

    UserInvitationResponse resend(UUID invitationId, String ipAddress, String userAgent);

    UserInvitationResponse cancel(UUID invitationId, String ipAddress, String userAgent);

    AcceptInvitationResponse accept(AcceptInvitationRequest request, String ipAddress, String userAgent);

    InvitationPreviewResponse reject(RejectInvitationRequest request, String ipAddress, String userAgent);

    /**
     * Public preview of a pending invitation with minimized identifiers.
     */
    InvitationPreviewResponse previewByToken(String rawToken);
}
