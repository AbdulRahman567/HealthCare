package com.healthcare.hms.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLRestriction;

/**
 * Pharmacist employment profile linked to a {@code User} with role {@code PHARMACIST}.
 */
@Entity
@Table(
        name = "pharmacists",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pharmacists_tenant_employee_code", columnNames = {"tenant_id", "employee_code"}),
                @UniqueConstraint(name = "uk_pharmacists_tenant_user", columnNames = {"tenant_id", "user_id"}),
                @UniqueConstraint(name = "uk_pharmacists_tenant_license", columnNames = {"tenant_id", "license_number"})
        },
        indexes = {
                @Index(name = "idx_pharmacists_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_pharmacists_hospital_id", columnList = "hospital_id"),
                @Index(name = "idx_pharmacists_department_id", columnList = "department_id"),
                @Index(name = "idx_pharmacists_user_id", columnList = "user_id"),
                @Index(name = "idx_pharmacists_employment_status", columnList = "tenant_id, employment_status"),
                @Index(name = "idx_pharmacists_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Pharmacist extends Staff {

    @NotBlank
    @Size(max = 100)
    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @Size(max = 150)
    @Column(name = "pharmacy_location", length = 150)
    private String pharmacyLocation;

    @Size(max = 255)
    @Column(name = "qualification", length = 255)
    private String qualification;

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(final String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getPharmacyLocation() {
        return pharmacyLocation;
    }

    public void setPharmacyLocation(final String pharmacyLocation) {
        this.pharmacyLocation = pharmacyLocation;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(final String qualification) {
        this.qualification = qualification;
    }
}
