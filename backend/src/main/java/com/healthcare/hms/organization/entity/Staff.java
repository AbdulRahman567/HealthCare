package com.healthcare.hms.organization.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.enums.EmploymentType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Base employment abstraction for all hospital personnel.
 *
 * <p>Phase 4.1 defines the shared contract. Concrete specializations
 * (Doctor, Nurse, Receptionist, LaboratoryStaff, Pharmacist) are Phase 4.3
 * entity tables that inherit these fields.
 *
 * <p>Identity vs employment:
 * <ul>
 *   <li>{@code userId} — login / RBAC identity ({@code users})</li>
 *   <li>This type — hospital employment, reporting line, and department affiliation</li>
 * </ul>
 *
 * <p>Cross-cutting guarantees (inherited from {@link TenantOwnedEntity} /
 * {@link com.healthcare.hms.common.persistence.BaseEntity}):
 * UUID primary key, {@code tenant_id}, created/updated audit, soft delete, optimistic lock.
 *
 * <p>Department affiliation is required by staff administration services (Phase 4.3)
 * via {@link #departmentId}.
 */
@MappedSuperclass
public abstract class Staff extends TenantOwnedEntity {

    /**
     * Owning hospital profile within the tenant ({@code hospitals.id}).
     * Required: every staff member belongs to exactly one hospital.
     */
    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    /**
     * Linked platform user account ({@code users.id}).
     * One employment row per user per hospital (enforced when concrete tables appear).
     */
    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID userId;

    /**
     * Optional department affiliation ({@code departments.id} — Phase 4.x / 3.x).
     * Null until departments are provisioned or for hospital-wide roles.
     */
    @Column(name = "department_id", columnDefinition = "CHAR(36)")
    private UUID departmentId;

    /**
     * Optional reporting manager (another staff row id within the same tenant).
     * Forms the staff reporting hierarchy; null for root roles (e.g. Hospital Admin).
     */
    @Column(name = "reports_to_staff_id", columnDefinition = "CHAR(36)")
    private UUID reportsToStaffId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "employee_code", nullable = false, length = 50)
    private String employeeCode;

    @Size(max = 150)
    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus = EmploymentStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 30)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Column(name = "hired_at")
    private LocalDate hiredAt;

    @Column(name = "terminated_at")
    private LocalDate terminatedAt;

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(final UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(final UUID departmentId) {
        this.departmentId = departmentId;
    }

    public UUID getReportsToStaffId() {
        return reportsToStaffId;
    }

    public void setReportsToStaffId(final UUID reportsToStaffId) {
        this.reportsToStaffId = reportsToStaffId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(final String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(final String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(final EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(final EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getHiredAt() {
        return hiredAt;
    }

    public void setHiredAt(final LocalDate hiredAt) {
        this.hiredAt = hiredAt;
    }

    public LocalDate getTerminatedAt() {
        return terminatedAt;
    }

    public void setTerminatedAt(final LocalDate terminatedAt) {
        this.terminatedAt = terminatedAt;
    }

    /**
     * Whether this employment record is currently eligible for operational work.
     */
    public boolean isOperationallyActive() {
        return employmentStatus == EmploymentStatus.ACTIVE && !isDeleted();
    }
}
