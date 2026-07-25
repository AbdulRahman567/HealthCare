package com.healthcare.hms.organization.entity;

import com.healthcare.hms.organization.enums.StaffShift;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLRestriction;

/**
 * Nurse employment profile linked to a {@code User} with role {@code NURSE}.
 */
@Entity
@Table(
        name = "nurses",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_nurses_tenant_employee_code", columnNames = {"tenant_id", "employee_code"}),
                @UniqueConstraint(name = "uk_nurses_tenant_user", columnNames = {"tenant_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_nurses_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_nurses_hospital_id", columnList = "hospital_id"),
                @Index(name = "idx_nurses_department_id", columnList = "department_id"),
                @Index(name = "idx_nurses_user_id", columnList = "user_id"),
                @Index(name = "idx_nurses_employment_status", columnList = "tenant_id, employment_status"),
                @Index(name = "idx_nurses_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Nurse extends Staff {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false, length = 30)
    private StaffShift shift = StaffShift.ROTATING;

    @Size(max = 255)
    @Column(name = "qualification", length = 255)
    private String qualification;

    @Size(max = 100)
    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    public StaffShift getShift() {
        return shift;
    }

    public void setShift(final StaffShift shift) {
        this.shift = shift;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(final String qualification) {
        this.qualification = qualification;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(final String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}
