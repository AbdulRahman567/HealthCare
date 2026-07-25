package com.healthcare.hms.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.hibernate.annotations.SQLRestriction;

/**
 * Doctor employment profile linked to a {@code User} with role {@code DOCTOR}.
 */
@Entity
@Table(
        name = "doctors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctors_tenant_employee_code", columnNames = {"tenant_id", "employee_code"}),
                @UniqueConstraint(name = "uk_doctors_tenant_user", columnNames = {"tenant_id", "user_id"}),
                @UniqueConstraint(name = "uk_doctors_tenant_license", columnNames = {"tenant_id", "license_number"})
        },
        indexes = {
                @Index(name = "idx_doctors_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_doctors_hospital_id", columnList = "hospital_id"),
                @Index(name = "idx_doctors_department_id", columnList = "department_id"),
                @Index(name = "idx_doctors_user_id", columnList = "user_id"),
                @Index(name = "idx_doctors_employment_status", columnList = "tenant_id, employment_status"),
                @Index(name = "idx_doctors_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Doctor extends Staff {

    @NotBlank
    @Size(max = 150)
    @Column(name = "specialization", nullable = false, length = 150)
    private String specialization;

    @NotBlank
    @Size(max = 100)
    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @Size(max = 255)
    @Column(name = "qualification", length = 255)
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "consultation_fee", precision = 12, scale = 2)
    private BigDecimal consultationFee;

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(final String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(final String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(final String qualification) {
        this.qualification = qualification;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(final Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(final BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }
}
