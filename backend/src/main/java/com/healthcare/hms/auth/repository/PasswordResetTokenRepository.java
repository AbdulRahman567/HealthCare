package com.healthcare.hms.auth.repository;

import com.healthcare.hms.auth.entity.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Query("""
            SELECT prt FROM PasswordResetToken prt
            JOIN FETCH prt.user u
            WHERE prt.tokenHash = :tokenHash
            """)
    Optional<PasswordResetToken> findByTokenHashWithUser(@Param("tokenHash") String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE PasswordResetToken prt
            SET prt.usedAt = :usedAt
            WHERE prt.user.id = :userId
              AND prt.usedAt IS NULL
              AND prt.expiresAt > :now
            """)
    int invalidateActiveTokensForUser(
            @Param("userId") UUID userId,
            @Param("usedAt") Instant usedAt,
            @Param("now") Instant now
    );
}
