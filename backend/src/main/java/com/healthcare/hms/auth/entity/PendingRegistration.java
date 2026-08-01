package com.healthcare.hms.auth.entity;

import com.healthcare.hms.common.persistence.BaseEntity;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.hibernate.annotations.SQLRestriction;

/**
 * Lightweight, transient record capturing the full hospital signup payload BEFORE any real
 * tenant / hospital / admin account is created (Phase 7).
 *
 * <p>Written when the single-page registration form is submitted; the real records are
 * created only when the emailed verification token is clicked. Expired, unverified rows are
 * hard-deleted by a scheduled cleanup job. Intentionally NOT tenant-owned — no tenant exists yet.
 */
@Entity
@Table(
        name = "pending_registrations",
        indexes = {
                @Index(name = "idx_pending_registrations_email", columnList = "email"),
                @Index(name = "idx_pending_registrations_token_expires_at", columnList = "token_expires_at"),
                @Index(name = "idx_pending_registrations_verified_at", columnList = "verified_at"),
                @Index(name = "idx_pending_registrations_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class PendingRegistration extends BaseEntity {

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
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @NotBlank
    @Size(max = 200)
    @Column(name = "hospital_name", nullable = false, length = 200)
    private String hospitalName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "hospital_email", nullable = false, length = 255)
    private String hospitalEmail;

    @Size(max = 30)
    @Column(name = "hospital_phone", length = 30)
    private String hospitalPhone;

    @Size(max = 500)
    @Column(name = "hospital_address", length = 500)
    private String hospitalAddress;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false, length = 20)
    private SubscriptionPlan subscriptionPlan;

    @NotBlank
    @Size(max = 128)
    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @NotNull
    @Column(name = "token_expires_at", nullable = false)
    private Instant tokenExpiresAt;

    @NotNull
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

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

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(final String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalEmail() {
        return hospitalEmail;
    }

    public void setHospitalEmail(final String hospitalEmail) {
        this.hospitalEmail = hospitalEmail;
    }

    public String getHospitalPhone() {
        return hospitalPhone;
    }

    public void setHospitalPhone(final String hospitalPhone) {
        this.hospitalPhone = hospitalPhone;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public void setHospitalAddress(final String hospitalAddress) {
        this.hospitalAddress = hospitalAddress;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(final SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(final String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(final Instant tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(final Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(final Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public boolean isActive() {
        return !isDeleted() && verifiedAt == null && Instant.now().isBefore(tokenExpiresAt);
    }
}
