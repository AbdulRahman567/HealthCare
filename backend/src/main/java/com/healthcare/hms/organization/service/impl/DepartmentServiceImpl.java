package com.healthcare.hms.organization.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.organization.dto.request.CreateDepartmentRequest;
import com.healthcare.hms.organization.dto.request.UpdateDepartmentRequest;
import com.healthcare.hms.organization.dto.response.DepartmentResponse;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.DepartmentType;
import com.healthcare.hms.organization.mapper.DepartmentMapper;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.DepartmentSpecifications;
import com.healthcare.hms.organization.service.DepartmentService;
import com.healthcare.hms.organization.staff.StaffMembershipGuard;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tenant-isolated department management (Phase 4.2).
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    private static final String ENTITY_DEPARTMENT = "DEPARTMENT";
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "name",
            "code",
            "status",
            "departmentType",
            "location",
            "createdAt",
            "updatedAt"
    );

    private final DepartmentRepository departmentRepository;
    private final HospitalQueryService hospitalQueryService;
    private final DepartmentMapper departmentMapper;
    private final AuditLogService auditLogService;
    private final StaffMembershipGuard staffMembershipGuard;

    public DepartmentServiceImpl(
            final DepartmentRepository departmentRepository,
            final HospitalQueryService hospitalQueryService,
            final DepartmentMapper departmentMapper,
            final AuditLogService auditLogService,
            final StaffMembershipGuard staffMembershipGuard
    ) {
        this.departmentRepository = departmentRepository;
        this.hospitalQueryService = hospitalQueryService;
        this.departmentMapper = departmentMapper;
        this.auditLogService = auditLogService;
        this.staffMembershipGuard = staffMembershipGuard;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.DEPARTMENT_CREATE)
    public DepartmentResponse create(
            final CreateDepartmentRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID hospitalId = hospitalQueryService.requireDefaultHospitalId();
        final String normalizedCode = normalizeCode(request.code());
        final String normalizedName = request.name().trim();

        assertUniqueCode(tenantId, normalizedCode, null);
        assertUniqueName(tenantId, normalizedName, null);

        final Department department = new Department();
        department.setHospitalId(hospitalId);
        departmentMapper.applyCreate(request, department);

        final Department saved = departmentRepository.save(department);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_DEPARTMENT,
                saved.getId().toString(),
                AuditAction.CREATE,
                null,
                snapshot(saved),
                ipAddress,
                userAgent
        );

        log.info(
                "Department created id={} code={} tenantId={} actorId={}",
                saved.getId(),
                saved.getCode(),
                saved.getTenantId(),
                actorId
        );
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.DEPARTMENT_READ)
    public DepartmentResponse getById(final UUID departmentId) {
        return departmentMapper.toResponse(requireDepartment(departmentId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.DEPARTMENT_READ)
    public PageResponse<DepartmentResponse> search(
            final String search,
            final DepartmentStatus status,
            final DepartmentType departmentType,
            final UUID hospitalId,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Pageable safePageable = sanitizePageable(pageable);
        final Page<DepartmentResponse> page = departmentRepository
                .findAll(
                        DepartmentSpecifications.withFilters(
                                tenantId,
                                search,
                                status,
                                departmentType,
                                hospitalId
                        ),
                        safePageable
                )
                .map(departmentMapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.DEPARTMENT_UPDATE)
    public DepartmentResponse update(
            final UUID departmentId,
            final UpdateDepartmentRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final Department department = requireDepartment(departmentId);
        final UUID tenantId = department.getTenantId();
        final String oldSnapshot = snapshot(department);

        final String normalizedCode = normalizeCode(request.code());
        final String normalizedName = request.name().trim();
        assertUniqueCode(tenantId, normalizedCode, department.getId());
        assertUniqueName(tenantId, normalizedName, department.getId());

        departmentMapper.applyUpdate(request, department);
        final Department saved = departmentRepository.save(department);

        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_DEPARTMENT,
                saved.getId().toString(),
                AuditAction.UPDATE,
                oldSnapshot,
                snapshot(saved),
                ipAddress,
                userAgent
        );

        log.info(
                "Department updated id={} tenantId={} actorId={}",
                saved.getId(),
                saved.getTenantId(),
                actorId
        );
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.DEPARTMENT_DELETE)
    public void delete(final UUID departmentId, final String ipAddress, final String userAgent) {
        final Department department = requireDepartment(departmentId);
        if (staffMembershipGuard.hasAffiliatedStaff(department.getId())) {
            throw new BusinessException(
                    "DEPARTMENT_HAS_STAFF",
                    "Reassign or remove affiliated staff before deleting this department"
            );
        }
        final String oldSnapshot = snapshot(department);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();

        // Free unique (tenant_id, code/name) so soft-deleted codes may be reused.
        department.setCode(releaseUniqueValue(department.getCode(), department.getId(), 50));
        department.setName(releaseUniqueValue(department.getName(), department.getId(), 200));
        department.clearHead();
        department.markDeleted(actorId);
        departmentRepository.save(department);

        auditLogService.record(
                department.getTenantId(),
                actorId,
                ENTITY_DEPARTMENT,
                department.getId().toString(),
                AuditAction.DELETE,
                oldSnapshot,
                snapshot(department),
                ipAddress,
                userAgent
        );

        log.info(
                "Department soft-deleted id={} tenantId={} actorId={}",
                department.getId(),
                department.getTenantId(),
                actorId
        );
    }

    private Department requireDepartment(final UUID departmentId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    private void assertUniqueCode(final UUID tenantId, final String code, final UUID excludeId) {
        final boolean exists = excludeId == null
                ? departmentRepository.existsByTenantIdAndCodeIgnoreCase(tenantId, code)
                : departmentRepository.existsByTenantIdAndCodeIgnoreCaseAndIdNot(tenantId, code, excludeId);
        if (exists) {
            throw new ConflictException("DEPARTMENT_CODE_EXISTS", "Department code is already in use for this tenant");
        }
    }

    private void assertUniqueName(final UUID tenantId, final String name, final UUID excludeId) {
        final boolean exists = excludeId == null
                ? departmentRepository.existsByTenantIdAndNameIgnoreCase(tenantId, name)
                : departmentRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(tenantId, name, excludeId);
        if (exists) {
            throw new ConflictException("DEPARTMENT_NAME_EXISTS", "Department name is already in use for this tenant");
        }
    }

    /**
     * Produces a guaranteed-unique soft-delete marker that never truncates back to the original value.
     */
    private static String releaseUniqueValue(final String original, final UUID id, final int maxLength) {
        final String compactId = id.toString().replace("-", "");
        final String suffix = "__DEL__" + compactId.substring(0, 8);
        if (original.length() + suffix.length() <= maxLength) {
            return original + suffix;
        }
        final String replacement = "DEL-" + compactId;
        return replacement.length() <= maxLength ? replacement : replacement.substring(0, maxLength);
    }

    private static Pageable sanitizePageable(final Pageable pageable) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        }

        final Sort safeSort = Sort.by(pageable.getSort().stream()
                .filter(order -> ALLOWED_SORT_PROPERTIES.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList());

        if (safeSort.isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        }
        return PageRequest.of(page, size, safeSort);
    }

    private static String normalizeCode(final String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static String snapshot(final Department department) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", department.getId());
        fields.put("hospitalId", department.getHospitalId());
        fields.put("name", department.getName());
        fields.put("code", department.getCode());
        fields.put("description", department.getDescription());
        fields.put("departmentType", department.getDepartmentType());
        fields.put("status", department.getStatus());
        fields.put("location", department.getLocation());
        fields.put("headUserId", department.getHeadUserId());
        fields.put("headStaffId", department.getHeadStaffId());
        fields.put("headStaffType", department.getHeadStaffType());
        fields.put("deleted", department.isDeleted());
        return fields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
