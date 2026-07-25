package com.healthcare.hms.organization.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.organization.dto.request.CreateLaboratoryStaffRequest;
import com.healthcare.hms.organization.dto.request.UpdateLaboratoryStaffRequest;
import com.healthcare.hms.organization.dto.response.LaboratoryStaffResponse;
import com.healthcare.hms.organization.entity.LaboratoryStaff;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.mapper.StaffResponseMapper;
import com.healthcare.hms.organization.repository.LaboratoryStaffRepository;
import com.healthcare.hms.organization.repository.StaffSpecifications;
import com.healthcare.hms.organization.service.LaboratoryStaffService;
import com.healthcare.hms.organization.enums.StaffType;
import com.healthcare.hms.organization.staff.StaffAdministrationSupport;
import com.healthcare.hms.organization.staff.StaffAssignmentHistoryWriter;
import com.healthcare.hms.organization.staff.StaffMembershipGuard;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.enums.RoleType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LaboratoryStaffServiceImpl implements LaboratoryStaffService {

    private static final String ENTITY = "LABORATORY_STAFF";

    private final LaboratoryStaffRepository laboratoryStaffRepository;
    private final HospitalQueryService hospitalQueryService;
    private final StaffAdministrationSupport staffSupport;
    private final StaffMembershipGuard membershipGuard;
    private final StaffAssignmentHistoryWriter assignmentHistoryWriter;
    private final StaffResponseMapper responseMapper;
    private final AuditLogService auditLogService;

    public LaboratoryStaffServiceImpl(
            final LaboratoryStaffRepository laboratoryStaffRepository,
            final HospitalQueryService hospitalQueryService,
            final StaffAdministrationSupport staffSupport,
            final StaffMembershipGuard membershipGuard,
            final StaffAssignmentHistoryWriter assignmentHistoryWriter,
            final StaffResponseMapper responseMapper,
            final AuditLogService auditLogService
    ) {
        this.laboratoryStaffRepository = laboratoryStaffRepository;
        this.hospitalQueryService = hospitalQueryService;
        this.staffSupport = staffSupport;
        this.membershipGuard = membershipGuard;
        this.assignmentHistoryWriter = assignmentHistoryWriter;
        this.responseMapper = responseMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_CREATE)
    public LaboratoryStaffResponse create(final CreateLaboratoryStaffRequest request, final String ipAddress, final String userAgent) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID hospitalId = hospitalQueryService.requireDefaultHospitalId();
        staffSupport.assertUserAndDepartment(tenantId, request.userId(), RoleType.LAB_TECHNICIAN, request.departmentId());
        staffSupport.assertUserNotLinkedElsewhere(request.userId(), membershipGuard::isEmployedElsewhereExcludingLaboratoryStaff);
        staffSupport.assertUniqueUserLink(tenantId, request.userId(), null,
                laboratoryStaffRepository::existsByTenantIdAndUserId, laboratoryStaffRepository::existsByTenantIdAndUserIdAndIdNot);
        staffSupport.assertUniqueEmployeeCode(tenantId, request.employeeCode(), null,
                laboratoryStaffRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                laboratoryStaffRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot);

        final LaboratoryStaff entity = new LaboratoryStaff();
        staffSupport.applyEmployment(entity, hospitalId, request.userId(), request.departmentId(), request.employeeCode(),
                request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), true);
        entity.setSpecialtyArea(StaffAdministrationSupport.trimToNull(request.specialtyArea()));
        entity.setLicenseNumber(StaffAdministrationSupport.trimToNull(request.licenseNumber()));
        entity.setCertification(StaffAdministrationSupport.trimToNull(request.certification()));

        final LaboratoryStaff saved = laboratoryStaffRepository.save(entity);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.LABORATORY_STAFF, saved, null);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        return responseMapper.toLaboratoryStaffResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public LaboratoryStaffResponse getById(final UUID staffId) {
        return responseMapper.toLaboratoryStaffResponse(require(staffId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public PageResponse<LaboratoryStaffResponse> search(
            final String search, final EmploymentStatus employmentStatus, final UUID departmentId, final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return PageResponse.from(laboratoryStaffRepository.findAll(
                StaffSpecifications.withFilters(tenantId, search, employmentStatus, departmentId,
                        "employeeCode", "jobTitle", "specialtyArea", "licenseNumber", "certification"),
                staffSupport.sanitizePageable(pageable, StaffAdministrationSupport.DEFAULT_SORT_PROPERTIES)
        ).map(responseMapper::toLaboratoryStaffResponse));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    public LaboratoryStaffResponse update(
            final UUID staffId, final UpdateLaboratoryStaffRequest request, final String ipAddress, final String userAgent
    ) {
        final LaboratoryStaff entity = require(staffId);
        final String old = snapshot(entity);
        final UUID previousDepartmentId = entity.getDepartmentId();
        if (!entity.getUserId().equals(request.userId())) {
            throw new ConflictException("STAFF_USER_IMMUTABLE", "Staff user link cannot be changed after creation");
        }
        staffSupport.assertUserAndDepartment(entity.getTenantId(), entity.getUserId(), RoleType.LAB_TECHNICIAN, request.departmentId());
        staffSupport.assertUniqueEmployeeCode(entity.getTenantId(), request.employeeCode(), entity.getId(),
                laboratoryStaffRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                laboratoryStaffRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot);

        staffSupport.applyEmployment(entity, entity.getHospitalId(), entity.getUserId(), request.departmentId(),
                request.employeeCode(), request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), false);
        entity.setSpecialtyArea(StaffAdministrationSupport.trimToNull(request.specialtyArea()));
        entity.setLicenseNumber(StaffAdministrationSupport.trimToNull(request.licenseNumber()));
        entity.setCertification(StaffAdministrationSupport.trimToNull(request.certification()));

        final LaboratoryStaff saved = laboratoryStaffRepository.save(entity);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.LABORATORY_STAFF, saved, previousDepartmentId);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        return responseMapper.toLaboratoryStaffResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_DELETE)
    public void delete(final UUID staffId, final String ipAddress, final String userAgent) {
        final LaboratoryStaff entity = require(staffId);
        final String old = snapshot(entity);
        assignmentHistoryWriter.closeOnSoftDelete(StaffType.LABORATORY_STAFF, entity.getId());
        staffSupport.markSoftDeleted(entity, SecurityUtils.requireCurrentUser().getUserId());
        laboratoryStaffRepository.save(entity);
        audit(entity, AuditAction.DELETE, old, ipAddress, userAgent);
    }

    private LaboratoryStaff require(final UUID id) {
        return laboratoryStaffRepository.findByIdAndTenantId(id, TenantContextHolder.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Laboratory staff not found"));
    }

    private void audit(final LaboratoryStaff entity, final AuditAction action, final String oldSnapshot,
                       final String ipAddress, final String userAgent) {
        auditLogService.record(entity.getTenantId(), SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY, entity.getId().toString(), action, oldSnapshot, snapshot(entity), ipAddress, userAgent);
    }

    private static String snapshot(final LaboratoryStaff entity) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", entity.getId());
        fields.put("userId", entity.getUserId());
        fields.put("departmentId", entity.getDepartmentId());
        fields.put("employeeCode", entity.getEmployeeCode());
        fields.put("employmentStatus", entity.getEmploymentStatus());
        fields.put("deleted", entity.isDeleted());
        return fields.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
