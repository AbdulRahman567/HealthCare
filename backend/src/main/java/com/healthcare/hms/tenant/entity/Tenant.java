package com.healthcare.hms.tenant.entity;

import com.healthcare.hms.common.persistence.BaseEntity;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.tenant.exception.TenantInvalidTransitionException;
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
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Tenant aggregate root — one hospital (or clinic) on the shared HMS platform.
 *
 * <p>Isolation strategy: Shared Database + Shared Schema + {@code tenant_id} discriminator
 * on every business table. This entity itself has no {@code tenant_id}; it <em>is</em> the
 * tenant boundary.
 *
 * <p>Lifecycle transitions are enforced by domain methods; callers must not mutate
 * {@link #status} directly outside this aggregate.
 */
@Entity
@Table(
        name = "tenants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tenants_slug", columnNames = {"slug"}),
                @UniqueConstraint(name = "uk_tenants_email", columnNames = {"email"})
        },
        indexes = {
                @Index(name = "idx_tenants_status", columnList = "status"),
                @Index(name = "idx_tenants_tenant_type", columnList = "tenant_type"),
                @Index(name = "idx_tenants_deleted", columnList = "deleted"),
                @Index(name = "idx_tenants_status_deleted", columnList = "status, deleted")
        }
)
@SQLRestriction("deleted = false")
public class Tenant extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotBlank
    @Size(max = 120)
    @Column(name = "slug", nullable = false, length = 120)
    private String slug;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_type", nullable = false, length = 50)
    private TenantType tenantType = TenantType.HOSPITAL;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 500)
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false, length = 50)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TenantStatus status = TenantStatus.PENDING;

    /**
     * Whether this tenant may serve authenticated business traffic.
     */
    public boolean isOperational() {
        return status == TenantStatus.ACTIVE && !isDeleted();
    }

    /**
     * Activates the tenant after onboarding or reactivation.
     * Allowed from {@link TenantStatus#PENDING}, {@link TenantStatus#SUSPENDED},
     * or {@link TenantStatus#INACTIVE}.
     */
    public void activate() {
        if (status != TenantStatus.PENDING
                && status != TenantStatus.SUSPENDED
                && status != TenantStatus.INACTIVE) {
            throw new TenantInvalidTransitionException(status, TenantStatus.ACTIVE);
        }
        this.status = TenantStatus.ACTIVE;
    }

    /**
     * Temporarily blocks tenant access while retaining all clinical data.
     */
    public void suspend() {
        if (status != TenantStatus.ACTIVE) {
            throw new TenantInvalidTransitionException(status, TenantStatus.SUSPENDED);
        }
        this.status = TenantStatus.SUSPENDED;
    }

    /**
     * Gracefully deactivates an active or suspended tenant (offboarding).
     */
    public void deactivate() {
        if (status != TenantStatus.ACTIVE && status != TenantStatus.SUSPENDED) {
            throw new TenantInvalidTransitionException(status, TenantStatus.INACTIVE);
        }
        this.status = TenantStatus.INACTIVE;
    }

    /**
     * Soft-deletes the tenant aggregate. Clinical child data is retained for compliance;
     * hard delete is never performed from application code.
     */
    public void softDelete(final UUID actorId) {
        markDeleted(actorId);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public TenantType getTenantType() {
        return tenantType;
    }

    public void setTenantType(final TenantType tenantType) {
        this.tenantType = tenantType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(final SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public TenantStatus getStatus() {
        return status;
    }

    /**
     * Persistence / mapper hook. Prefer {@link #activate()}, {@link #suspend()},
     * and {@link #deactivate()} for status changes in application code.
     */
    public void setStatus(final TenantStatus status) {
        this.status = status;
    }
}
