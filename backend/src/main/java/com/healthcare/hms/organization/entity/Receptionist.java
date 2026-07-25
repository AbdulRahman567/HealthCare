package com.healthcare.hms.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLRestriction;

/**
 * Receptionist employment profile linked to a {@code User} with role {@code RECEPTIONIST}.
 */
@Entity
@Table(
        name = "receptionists",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_receptionists_tenant_employee_code", columnNames = {"tenant_id", "employee_code"}),
                @UniqueConstraint(name = "uk_receptionists_tenant_user", columnNames = {"tenant_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_receptionists_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_receptionists_hospital_id", columnList = "hospital_id"),
                @Index(name = "idx_receptionists_department_id", columnList = "department_id"),
                @Index(name = "idx_receptionists_user_id", columnList = "user_id"),
                @Index(name = "idx_receptionists_employment_status", columnList = "tenant_id, employment_status"),
                @Index(name = "idx_receptionists_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Receptionist extends Staff {

    @Size(max = 150)
    @Column(name = "desk_location", length = 150)
    private String deskLocation;

    @Size(max = 255)
    @Column(name = "languages", length = 255)
    private String languages;

    public String getDeskLocation() {
        return deskLocation;
    }

    public void setDeskLocation(final String deskLocation) {
        this.deskLocation = deskLocation;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(final String languages) {
        this.languages = languages;
    }
}
