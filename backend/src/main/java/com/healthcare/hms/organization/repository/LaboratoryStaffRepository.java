package com.healthcare.hms.organization.repository;

import com.healthcare.hms.organization.entity.LaboratoryStaff;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LaboratoryStaffRepository
        extends JpaRepository<LaboratoryStaff, UUID>, JpaSpecificationExecutor<LaboratoryStaff> {

    Optional<LaboratoryStaff> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndEmployeeCodeIgnoreCase(UUID tenantId, String employeeCode);

    boolean existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot(UUID tenantId, String employeeCode, UUID id);

    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

    boolean existsByTenantIdAndUserIdAndIdNot(UUID tenantId, UUID userId, UUID id);

    boolean existsByUserId(UUID userId);

    boolean existsByDepartmentId(UUID departmentId);

    java.util.Optional<LaboratoryStaff> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
