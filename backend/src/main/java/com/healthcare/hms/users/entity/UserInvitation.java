package com.healthcare.hms.users.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.users.enums.InvitationStatus;
import com.healthcare.hms.users.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Invite-by-email onboarding for a tenant hospital (Phase 4.5).
 *
 * <p>Only the SHA-256 token hash is persisted. Accept/reject are public token flows;
 * invite/resend/cancel require hospital-admin permissions.
 */
@Entity
@Table(
        name = "user_invitations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_invitations_token_hash", columnNames = {"token_hash"})
        },
        indexes = {
                @Index(name = "idx_user_invitations_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_user_invitations_email", columnList = "tenant_id, email"),
                @Index(name = "idx_user_invitations_status", columnList = "tenant_id, status"),
                @Index(name = "idx_user_invitations_expires_at", columnList = "expires_at"),
                @Index(name = "idx_user_invitations_invited_by", columnList = "invited_by"),
                @Index(name = "idx_user_invitations_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class UserInvitation extends TenantOwnedEntity {

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Size(max = 100)
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Size(max = 100)
    @Column(name = "last_name", length = 100)
    private String lastName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 30)
    private RoleType roleType;

    @NotNull
    @Column(name = "invited_by", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID invitedBy;

    @NotBlank
    @Size(max = 128)
    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvitationStatus status = InvitationStatus.PENDING;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "accepted_user_id", columnDefinition = "CHAR(36)")
    private UUID acceptedUserId;

    @Size(max = 500)
    @Column(name = "message", length = 500)
    private String message;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 512)
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    /**
     * Generated column for unique pending invitation per tenant email — read-only.
     */
    @Column(name = "pending_slot", length = 1, insertable = false, updatable = false)
    private String pendingSlot;

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(final UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(final RoleType roleType) {
        this.roleType = roleType;
    }

    public UUID getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(final UUID invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(final String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(final InvitationStatus status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(final Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(final Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(final Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public UUID getAcceptedUserId() {
        return acceptedUserId;
    }

    public void setAcceptedUserId(final UUID acceptedUserId) {
        this.acceptedUserId = acceptedUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
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

    public String getPendingSlot() {
        return pendingSlot;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING && !isDeleted();
    }

    public boolean isAcceptable() {
        return isPending() && !isExpired();
    }

    public void markAccepted(final UUID userId) {
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
        this.acceptedUserId = userId;
    }

    public void markRejected() {
        this.status = InvitationStatus.REJECTED;
        this.rejectedAt = Instant.now();
    }

    public void markCancelled() {
        this.status = InvitationStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public void markExpired() {
        this.status = InvitationStatus.EXPIRED;
    }
}
