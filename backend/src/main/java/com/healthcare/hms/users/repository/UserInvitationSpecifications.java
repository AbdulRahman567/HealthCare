package com.healthcare.hms.users.repository;

import com.healthcare.hms.users.entity.UserInvitation;
import com.healthcare.hms.users.enums.InvitationStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Invitation list filters (tenant-scoped).
 */
public final class UserInvitationSpecifications {

    private UserInvitationSpecifications() {
    }

    public static Specification<UserInvitation> withFilters(
            final UUID tenantId,
            final InvitationStatus status,
            final String email
    ) {
        return (root, query, cb) -> {
            var predicate = cb.equal(root.get("tenantId"), tenantId);
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (email != null && !email.isBlank()) {
                predicate = cb.and(
                        predicate,
                        cb.like(cb.lower(root.get("email")), "%" + email.trim().toLowerCase() + "%")
                );
            }
            return predicate;
        };
    }
}
