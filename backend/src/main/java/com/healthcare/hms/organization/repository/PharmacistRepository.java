package com.healthcare.hms.organization.repository;

import com.healthcare.hms.organization.entity.Pharmacist;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacistRepository extends JpaRepository<Pharmacist, UUID>, JpaSpecificationExecutor<Pharmacist> {

    Optional<Pharmacist> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndEmployeeCodeIgnoreCase(UUID tenantId, String employeeCode);

    boolean existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot(UUID tenantId, String employeeCode, UUID id);

    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

    boolean existsByTenantIdAndUserIdAndIdNot(UUID tenantId, UUID userId, UUID id);

    boolean existsByTenantIdAndLicenseNumberIgnoreCase(UUID tenantId, String licenseNumber);

    boolean existsByTenantIdAndLicenseNumberIgnoreCaseAndIdNot(UUID tenantId, String licenseNumber, UUID id);

    boolean existsByUserId(UUID userId);

    boolean existsByDepartmentId(UUID departmentId);

    java.util.Optional<Pharmacist> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
