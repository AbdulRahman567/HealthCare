package com.healthcare.hms.auth.repository;

import com.healthcare.hms.auth.entity.RefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);

    @Query("""
            SELECT rt FROM RefreshToken rt
            JOIN FETCH rt.user u
            WHERE rt.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashWithUser(@Param("tokenHash") String tokenHash);

    @Query("""
            SELECT rt FROM RefreshToken rt
            JOIN FETCH rt.user u
            WHERE rt.tokenHash = :tokenHash
              AND rt.revoked = false
              AND rt.expiresAt > :now
            """)
    Optional<RefreshToken> findActiveByTokenHash(
            @Param("tokenHash") String tokenHash,
            @Param("now") Instant now
    );

    List<RefreshToken> findByUserIdAndRevokedFalseAndExpiresAtAfterOrderByCreatedAtAsc(
            UUID userId,
            Instant expiresAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revoked = true,
                rt.revokedAt = :revokedAt
            WHERE rt.user.id = :userId
              AND rt.revoked = false
            """)
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

    long countByUserIdAndRevokedFalseAndExpiresAtAfter(UUID userId, Instant expiresAt);
}
