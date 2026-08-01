package com.healthcare.hms.auth.repository;

import com.healthcare.hms.auth.entity.PendingRegistration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, UUID> {

    Optional<PendingRegistration> findByTokenHashAndVerifiedAtIsNullAndDeletedFalse(String tokenHash);

    boolean existsByEmailIgnoreCaseAndVerifiedAtIsNullAndDeletedFalse(String email);

    Optional<PendingRegistration> findFirstByEmailIgnoreCaseAndVerifiedAtIsNullAndDeletedFalseOrderByCreatedAtDesc(
            String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM pending_registrations
            WHERE verified_at IS NULL
              AND token_expires_at < :now
            """, nativeQuery = true)
    int deleteExpiredUnverified(@Param("now") Instant now);
}
