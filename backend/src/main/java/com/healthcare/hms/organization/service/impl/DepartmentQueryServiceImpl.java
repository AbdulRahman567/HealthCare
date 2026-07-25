package com.healthcare.hms.organization.service.impl;

import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.service.DepartmentQueryService;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentQueryServiceImpl implements DepartmentQueryService {

    private final DepartmentRepository departmentRepository;

    public DepartmentQueryServiceImpl(final DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Department requireById(final UUID departmentId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public void assertBelongsToTenant(final UUID departmentId, final UUID tenantId) {
        final Department department = departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        if (!Objects.equals(tenantId, department.getTenantId())) {
            throw new BusinessException("DEPARTMENT_TENANT_MISMATCH", "Department does not belong to the current tenant");
        }
    }
}
