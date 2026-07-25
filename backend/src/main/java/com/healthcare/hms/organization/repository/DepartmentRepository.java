package com.healthcare.hms.organization.repository;

import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.enums.StaffType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {

    Optional<Department> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

    boolean existsByTenantIdAndCodeIgnoreCaseAndIdNot(UUID tenantId, String code, UUID id);

    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);

    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(UUID tenantId, String name, UUID id);

    List<Department> findByTenantIdAndHeadStaffTypeAndHeadStaffId(
            UUID tenantId,
            StaffType headStaffType,
            UUID headStaffId
    );

    List<Department> findByTenantIdAndHeadUserId(UUID tenantId, UUID headUserId);
}
