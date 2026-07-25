package com.healthcare.hms.organization.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.DepartmentType;
import com.healthcare.hms.organization.enums.StaffType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Tenant-owned hospital department (organizational unit).
 *
 * <p>Head of department is a Staff specialization reference (Phase 4.4) with
 * {@code head_user_id} kept in sync for identity lookups.
 */
@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_departments_tenant_code", columnNames = {"tenant_id", "code"}),
                @UniqueConstraint(name = "uk_departments_tenant_name", columnNames = {"tenant_id", "name"})
        },
        indexes = {
                @Index(name = "idx_departments_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_departments_hospital_id", columnList = "hospital_id"),
                @Index(name = "idx_departments_status", columnList = "tenant_id, status"),
                @Index(name = "idx_departments_type", columnList = "tenant_id, department_type"),
                @Index(name = "idx_departments_head_user_id", columnList = "head_user_id"),
                @Index(name = "idx_departments_head_staff_id", columnList = "head_staff_id"),
                @Index(name = "idx_departments_deleted", columnList = "deleted"),
                @Index(name = "idx_departments_name", columnList = "tenant_id, name")
        }
)
@SQLRestriction("deleted = false")
public class Department extends TenantOwnedEntity {

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "department_type", nullable = false, length = 30)
    private DepartmentType departmentType = DepartmentType.CLINICAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    @Size(max = 255)
    @Column(name = "location", length = 255)
    private String location;

    /**
     * Optional head of department ({@code users.id}) within the same tenant.
     * Kept in sync with {@link #headStaffId} when assigned via Phase 4.4 APIs.
     */
    @Column(name = "head_user_id", columnDefinition = "CHAR(36)")
    private UUID headUserId;

    /**
     * Optional head of department staff row id (polymorphic with {@link #headStaffType}).
     */
    @Column(name = "head_staff_id", columnDefinition = "CHAR(36)")
    private UUID headStaffId;

    @Enumerated(EnumType.STRING)
    @Column(name = "head_staff_type", length = 30)
    private StaffType headStaffType;

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(final UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public DepartmentType getDepartmentType() {
        return departmentType;
    }

    public void setDepartmentType(final DepartmentType departmentType) {
        this.departmentType = departmentType;
    }

    public DepartmentStatus getStatus() {
        return status;
    }

    public void setStatus(final DepartmentStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public UUID getHeadUserId() {
        return headUserId;
    }

    public void setHeadUserId(final UUID headUserId) {
        this.headUserId = headUserId;
    }

    public UUID getHeadStaffId() {
        return headStaffId;
    }

    public void setHeadStaffId(final UUID headStaffId) {
        this.headStaffId = headStaffId;
    }

    public StaffType getHeadStaffType() {
        return headStaffType;
    }

    public void setHeadStaffType(final StaffType headStaffType) {
        this.headStaffType = headStaffType;
    }

    public void clearHead() {
        this.headUserId = null;
        this.headStaffId = null;
        this.headStaffType = null;
    }

    public void assignHead(final UUID staffId, final StaffType staffType, final UUID userId) {
        this.headStaffId = staffId;
        this.headStaffType = staffType;
        this.headUserId = userId;
    }
}
