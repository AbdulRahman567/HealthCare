package com.healthcare.hms.users.entity;

import com.healthcare.hms.auth.entity.RefreshToken;
import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.users.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_tenant_email", columnNames = {"tenant_id", "email"})
        },
        indexes = {
                @Index(name = "idx_users_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_status", columnList = "status"),
                @Index(name = "idx_users_tenant_last_first", columnList = "tenant_id, last_name, first_name"),
                @Index(name = "idx_users_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class User extends TenantOwnedEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "token_version", nullable = false)
    private long tokenVersion = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_user_roles_user_role",
                    columnNames = {"user_id", "role_id"}
            ),
            indexes = {
                    @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
                    @Index(name = "idx_user_roles_role_id", columnList = "role_id")
            }
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RefreshToken> refreshTokens = new HashSet<>();

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

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(final boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(final Instant emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public void markEmailVerified() {
        this.emailVerified = true;
        this.emailVerifiedAt = Instant.now();
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(final UserStatus status) {
        this.status = status;
    }

    /**
     * Whether the account may authenticate (ACTIVE only).
     */
    public boolean isAuthenticationEligible() {
        return status == UserStatus.ACTIVE && !isDeleted();
    }

    /**
     * Activate for operational use: {@code PENDING} or {@code INACTIVE} → {@code ACTIVE}.
     */
    public void activate() {
        if (status != UserStatus.PENDING && status != UserStatus.INACTIVE) {
            throw new IllegalStateException("Cannot activate user from status " + status);
        }
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Deactivate: {@code ACTIVE} → {@code INACTIVE}.
     */
    public void deactivate() {
        if (status != UserStatus.ACTIVE) {
            throw new IllegalStateException("Cannot deactivate user from status " + status);
        }
        this.status = UserStatus.INACTIVE;
    }

    /**
     * Suspend: {@code ACTIVE} → {@code SUSPENDED}.
     */
    public void suspend() {
        if (status != UserStatus.ACTIVE) {
            throw new IllegalStateException("Cannot suspend user from status " + status);
        }
        this.status = UserStatus.SUSPENDED;
    }

    /**
     * Restore to active: {@code INACTIVE} or {@code SUSPENDED} → {@code ACTIVE}.
     *
     * <p>{@link UserStatus#LOCKED} is a security lock and must not be cleared via admin
     * lifecycle APIs (requires a dedicated unlock path when lockout is implemented).
     */
    public void restore() {
        if (status != UserStatus.INACTIVE && status != UserStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot restore user from status " + status);
        }
        this.status = UserStatus.ACTIVE;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(final Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public long getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(final long tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public void incrementTokenVersion() {
        this.tokenVersion += 1L;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }

    public Set<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(final Set<RefreshToken> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    public void addRole(final Role role) {
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(final Role role) {
        roles.remove(role);
        role.getUsers().remove(this);
    }
}
