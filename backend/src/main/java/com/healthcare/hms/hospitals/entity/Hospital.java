package com.healthcare.hms.hospitals.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.hospitals.enums.HospitalStatus;
import com.healthcare.hms.hospitals.model.WorkingHours;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

/**
 * Operational hospital profile belonging to a tenant.
 *
 * <p>Created during Phase 2.5 registration as the tenant's default hospital.
 * Phase 2.6 exposes settings (profile, locale, contact, address, working hours)
 * against this aggregate while remaining strictly tenant-scoped.
 */
@Entity
@Table(
        name = "hospitals",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_hospitals_tenant_code", columnNames = {"tenant_id", "code"}),
                @UniqueConstraint(name = "uk_hospitals_tenant_name", columnNames = {"tenant_id", "name"})
        },
        indexes = {
                @Index(name = "idx_hospitals_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_hospitals_status", columnList = "status"),
                @Index(name = "idx_hospitals_is_default", columnList = "tenant_id, is_default"),
                @Index(name = "idx_hospitals_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Hospital extends TenantOwnedEntity {

    public static final String DEFAULT_TIMEZONE = "UTC";
    public static final String DEFAULT_CURRENCY = "USD";
    public static final String DEFAULT_LANGUAGE = "en";

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, length = 50)
    private String code;

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

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 500)
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @NotBlank
    @Size(max = 100)
    @Column(name = "timezone", nullable = false, length = 100)
    private String timezone = DEFAULT_TIMEZONE;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = DEFAULT_CURRENCY;

    @NotBlank
    @Size(max = 10)
    @Column(name = "language", nullable = false, length = 10)
    private String language = DEFAULT_LANGUAGE;

    @Size(max = 500)
    @Column(name = "website", length = 500)
    private String website;

    @Size(max = 30)
    @Column(name = "secondary_phone", length = 30)
    private String secondaryPhone;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 100)
    @Column(name = "state_province", length = 100)
    private String stateProvince;

    @Size(max = 100)
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 20)
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_hours", columnDefinition = "json")
    private WorkingHours workingHours;

    @Column(name = "is_default", nullable = false)
    private boolean defaultHospital = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private HospitalStatus status = HospitalStatus.PENDING;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(final String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(final String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public WorkingHours getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(final WorkingHours workingHours) {
        this.workingHours = workingHours;
    }

    public boolean isDefaultHospital() {
        return defaultHospital;
    }

    public void setDefaultHospital(final boolean defaultHospital) {
        this.defaultHospital = defaultHospital;
    }

    public HospitalStatus getStatus() {
        return status;
    }

    public void setStatus(final HospitalStatus status) {
        this.status = status;
    }
}
