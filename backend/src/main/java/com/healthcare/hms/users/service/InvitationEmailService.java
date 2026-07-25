package com.healthcare.hms.users.service;

import com.healthcare.hms.users.entity.UserInvitation;
import com.healthcare.hms.users.enums.RoleType;

/**
 * Abstraction for delivering user-invitation emails (Phase 4.5).
 *
 * <p>Implementations use {@link com.healthcare.hms.common.email.EmailSender};
 * production SMTP vs logging is selected by {@code hms.mail.enabled}.
 */
public interface InvitationEmailService {

    /**
     * Sends the invite link containing the raw (never-persisted) token.
     */
    void sendInvitation(UserInvitation invitation, String rawToken, String hospitalName);

    /**
     * Re-sends an updated invite link after token refresh.
     */
    void sendInvitationResent(UserInvitation invitation, String rawToken, String hospitalName);
}
