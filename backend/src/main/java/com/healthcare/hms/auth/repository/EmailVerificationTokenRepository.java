package com.healthcare.hms.auth.repository;

import com.healthcare.hms.auth.entity.EmailVerificationToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    @Query("""
            SELECT evt FROM EmailVerificationToken evt
            JOIN FETCH evt.user u
            WHERE evt.tokenHash = :tokenHash
            """)
    Optional<EmailVerificationToken> findByTokenHashWithUser(@Param("tokenHash") String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE EmailVerificationToken evt
            SET evt.usedAt = :usedAt
            WHERE evt.user.id = :userId
              AND evt.usedAt IS NULL
              AND evt.expiresAt > :now
            """)
    int invalidateActiveTokensForUser(
            @Param("userId") UUID userId,
            @Param("usedAt") Instant usedAt,
            @Param("now") Instant now
    );
}
