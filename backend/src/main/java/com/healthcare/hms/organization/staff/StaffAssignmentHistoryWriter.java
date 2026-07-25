package com.healthcare.hms.organization.staff;

import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Staff;
import com.healthcare.hms.organization.entity.StaffDepartmentAssignment;
import com.healthcare.hms.organization.enums.AssignmentAction;
import com.healthcare.hms.organization.enums.StaffType;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.StaffDepartmentAssignmentRepository;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Writes staff ↔ department assignment history rows when affiliation changes
 * (staff CRUD or dedicated assignment APIs).
 */
@Component
public class StaffAssignmentHistoryWriter {

    private final StaffDepartmentAssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;

    public StaffAssignmentHistoryWriter(
            final StaffDepartmentAssignmentRepository assignmentRepository,
            final DepartmentRepository departmentRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Synchronizes open assignment history with the staff profile's {@code departmentId}.
     *
     * @param previousDepartmentId affiliation before the change (null if none / create)
     */
    public void syncAfterDepartmentChange(
            final StaffType staffType,
            final Staff staff,
            final UUID previousDepartmentId
    ) {
        final UUID currentDepartmentId = staff.getDepartmentId();
        if (Objects.equals(previousDepartmentId, currentDepartmentId)) {
            return;
        }

        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        final Instant now = Instant.now();

        assignmentRepository
                .findByTenantIdAndStaffTypeAndStaffIdAndEndedAtIsNull(tenantId, staffType, staff.getId())
                .ifPresent(open -> {
                    open.close(now, actorId);
                    assignmentRepository.save(open);
                });

        if (currentDepartmentId == null) {
            return;
        }

        final AssignmentAction action = previousDepartmentId == null
                ? AssignmentAction.ASSIGN
                : AssignmentAction.TRANSFER;

        final StaffDepartmentAssignment row = new StaffDepartmentAssignment();
        row.setHospitalId(staff.getHospitalId());
        row.setStaffType(staffType);
        row.setStaffId(staff.getId());
        row.setDepartmentId(currentDepartmentId);
        row.setFromDepartmentId(previousDepartmentId);
        row.setAction(action);
        row.setAssignedAt(now);
        row.setAssignedBy(actorId);
        assignmentRepository.save(row);
    }

    /**
     * Ends any open assignment and clears department head pointers when a staff profile is soft-deleted.
     */
    public void closeOnSoftDelete(final StaffType staffType, final UUID staffId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        final Instant now = Instant.now();
        assignmentRepository
                .findByTenantIdAndStaffTypeAndStaffIdAndEndedAtIsNull(tenantId, staffType, staffId)
                .ifPresent(open -> {
                    open.close(now, actorId);
                    assignmentRepository.save(open);
                });

        final List<Department> headed = departmentRepository.findByTenantIdAndHeadStaffTypeAndHeadStaffId(
                tenantId, staffType, staffId);
        for (final Department department : headed) {
            department.clearHead();
            departmentRepository.save(department);
        }
    }
}
