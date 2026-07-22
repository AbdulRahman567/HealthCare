package com.healthcare.hms.auth.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.hibernate.annotations.SQLRestriction;

/**
 * Single-use email verification token. Only the SHA-256 hash is persisted.
 */
@Entity
@Table(
        name = "email_verification_tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_verification_tokens_token_hash",
                        columnNames = {"token_hash"}
                )
        },
        indexes = {
                @Index(name = "idx_email_verification_tokens_user_id", columnList = "user_id"),
                @Index(name = "idx_email_verification_tokens_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_email_verification_tokens_expires_at", columnList = "expires_at"),
                @Index(name = "idx_email_verification_tokens_used_at", columnList = "used_at"),
                @Index(name = "idx_email_verification_tokens_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class EmailVerificationToken extends TenantOwnedEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 128)
    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 512)
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(final String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(final Instant usedAt) {
        this.usedAt = usedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isActive() {
        return !isUsed() && !isExpired() && !isDeleted();
    }

    public void markUsed() {
        this.usedAt = Instant.now();
    }
}
