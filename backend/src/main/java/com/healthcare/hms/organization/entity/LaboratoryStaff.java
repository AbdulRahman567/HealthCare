package com.healthcare.hms.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLRestriction;

/**
 * Laboratory staff employment profile linked to a {@code User} with role {@code LAB_TECHNICIAN}.
 */
@Entity
@Table(
        name = "laboratory_staff",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_laboratory_staff_tenant_employee_code", columnNames = {"tenant_id", "employee_code"}),
                @UniqueConstraint(name = "uk_laboratory_staff_tenant_user", columnNames = {"tenant_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_laboratory_staff_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_laboratory_staff_hospital_id", columnList = "hospital_id"),
                @Index(name = "idx_laboratory_staff_department_id", columnList = "department_id"),
                @Index(name = "idx_laboratory_staff_user_id", columnList = "user_id"),
                @Index(name = "idx_laboratory_staff_employment_status", columnList = "tenant_id, employment_status"),
                @Index(name = "idx_laboratory_staff_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class LaboratoryStaff extends Staff {

    @Size(max = 150)
    @Column(name = "specialty_area", length = 150)
    private String specialtyArea;

    @Size(max = 100)
    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Size(max = 255)
    @Column(name = "certification", length = 255)
    private String certification;

    public String getSpecialtyArea() {
        return specialtyArea;
    }

    public void setSpecialtyArea(final String specialtyArea) {
        this.specialtyArea = specialtyArea;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(final String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(final String certification) {
        this.certification = certification;
    }
}
