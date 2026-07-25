package com.healthcare.hms.organization.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.organization.enums.AssignmentAction;
import com.healthcare.hms.organization.enums.StaffType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Immutable-style history of staff ↔ department affiliations (Phase 4.4).
 *
 * <p>At most one open row per staff ({@code ended_at IS NULL}), enforced by
 * generated {@code open_slot} uniqueness in Flyway V14.
 */
@Entity
@Table(
        name = "staff_department_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_staff_open_assignment",
                        columnNames = {"tenant_id", "staff_type", "staff_id", "open_slot"}
                )
        },
        indexes = {
                @Index(name = "idx_sda_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_sda_staff", columnList = "tenant_id, staff_type, staff_id"),
                @Index(name = "idx_sda_department", columnList = "tenant_id, department_id"),
                @Index(name = "idx_sda_assigned_at", columnList = "tenant_id, assigned_at"),
                @Index(name = "idx_sda_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class StaffDepartmentAssignment extends TenantOwnedEntity {

    @NotNull
    @Column(name = "hospital_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID hospitalId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "staff_type", nullable = false, length = 30, updatable = false)
    private StaffType staffType;

    @NotNull
    @Column(name = "staff_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID staffId;

    @NotNull
    @Column(name = "department_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID departmentId;

    @Column(name = "from_department_id", columnDefinition = "CHAR(36)")
    private UUID fromDepartmentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30, updatable = false)
    private AssignmentAction action;

    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;

    @NotNull
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "assigned_by", columnDefinition = "CHAR(36)", updatable = false)
    private UUID assignedBy;

    @Column(name = "ended_by", columnDefinition = "CHAR(36)")
    private UUID endedBy;

    /**
     * Generated column — mapped read-only for Hibernate schema awareness.
     * Do not set in application code.
     */
    @Column(name = "open_slot", length = 1, insertable = false, updatable = false)
    private String openSlot;

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(final UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public StaffType getStaffType() {
        return staffType;
    }

    public void setStaffType(final StaffType staffType) {
        this.staffType = staffType;
    }

    public UUID getStaffId() {
        return staffId;
    }

    public void setStaffId(final UUID staffId) {
        this.staffId = staffId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(final UUID departmentId) {
        this.departmentId = departmentId;
    }

    public UUID getFromDepartmentId() {
        return fromDepartmentId;
    }

    public void setFromDepartmentId(final UUID fromDepartmentId) {
        this.fromDepartmentId = fromDepartmentId;
    }

    public AssignmentAction getAction() {
        return action;
    }

    public void setAction(final AssignmentAction action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(final Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(final Instant endedAt) {
        this.endedAt = endedAt;
    }

    public UUID getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(final UUID assignedBy) {
        this.assignedBy = assignedBy;
    }

    public UUID getEndedBy() {
        return endedBy;
    }

    public void setEndedBy(final UUID endedBy) {
        this.endedBy = endedBy;
    }

    public String getOpenSlot() {
        return openSlot;
    }

    public boolean isOpen() {
        return endedAt == null;
    }

    public void close(final Instant endedAt, final UUID endedBy) {
        this.endedAt = endedAt;
        this.endedBy = endedBy;
    }
}
