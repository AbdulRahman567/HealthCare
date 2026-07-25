package com.healthcare.hms.organization.repository;

import com.healthcare.hms.organization.entity.StaffDepartmentAssignment;
import com.healthcare.hms.organization.enums.StaffType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffDepartmentAssignmentRepository
        extends JpaRepository<StaffDepartmentAssignment, UUID>,
        JpaSpecificationExecutor<StaffDepartmentAssignment> {

    Optional<StaffDepartmentAssignment> findByTenantIdAndStaffTypeAndStaffIdAndEndedAtIsNull(
            UUID tenantId,
            StaffType staffType,
            UUID staffId
    );

    boolean existsByTenantIdAndStaffTypeAndStaffIdAndDepartmentIdAndEndedAtIsNull(
            UUID tenantId,
            StaffType staffType,
            UUID staffId,
            UUID departmentId
    );

    Page<StaffDepartmentAssignment> findByTenantIdAndStaffTypeAndStaffIdOrderByAssignedAtDesc(
            UUID tenantId,
            StaffType staffType,
            UUID staffId,
            Pageable pageable
    );

    Page<StaffDepartmentAssignment> findByTenantIdAndDepartmentIdOrderByAssignedAtDesc(
            UUID tenantId,
            UUID departmentId,
            Pageable pageable
    );
}
