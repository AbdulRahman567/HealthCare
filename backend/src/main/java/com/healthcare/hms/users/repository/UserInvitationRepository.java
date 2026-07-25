package com.healthcare.hms.users.repository;

import com.healthcare.hms.users.entity.UserInvitation;
import com.healthcare.hms.users.enums.InvitationStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInvitationRepository
        extends JpaRepository<UserInvitation, UUID>, JpaSpecificationExecutor<UserInvitation> {

    @Query("""
            SELECT i FROM UserInvitation i
            WHERE i.tokenHash = :tokenHash
            """)
    Optional<UserInvitation> findByTokenHash(@Param("tokenHash") String tokenHash);

    Optional<UserInvitation> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndEmailIgnoreCaseAndStatus(
            UUID tenantId,
            String email,
            InvitationStatus status
    );

    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END
            FROM UserInvitation i
            WHERE i.tenantId = :tenantId
              AND LOWER(i.email) = LOWER(:email)
              AND i.status = com.healthcare.hms.users.enums.InvitationStatus.PENDING
              AND i.expiresAt > :now
            """)
    boolean existsActivePendingByTenantIdAndEmailIgnoreCase(
            @Param("tenantId") UUID tenantId,
            @Param("email") String email,
            @Param("now") java.time.Instant now
    );

    @Query("""
            SELECT i FROM UserInvitation i
            WHERE i.tenantId = :tenantId
              AND i.status = com.healthcare.hms.users.enums.InvitationStatus.PENDING
              AND i.expiresAt <= :now
            """)
    java.util.List<UserInvitation> findStalePendingByTenantId(
            @Param("tenantId") UUID tenantId,
            @Param("now") java.time.Instant now
    );

    Page<UserInvitation> findByTenantId(UUID tenantId, Pageable pageable);
}
