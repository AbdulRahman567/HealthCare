package com.healthcare.hms.organization.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.organization.dto.request.CreateNurseRequest;
import com.healthcare.hms.organization.dto.request.UpdateNurseRequest;
import com.healthcare.hms.organization.dto.response.NurseResponse;
import com.healthcare.hms.organization.entity.Nurse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.mapper.StaffResponseMapper;
import com.healthcare.hms.organization.repository.NurseRepository;
import com.healthcare.hms.organization.repository.StaffSpecifications;
import com.healthcare.hms.organization.service.NurseService;
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
public class NurseServiceImpl implements NurseService {

    private static final String ENTITY = "NURSE";

    private final NurseRepository nurseRepository;
    private final HospitalQueryService hospitalQueryService;
    private final StaffAdministrationSupport staffSupport;
    private final StaffMembershipGuard membershipGuard;
    private final StaffAssignmentHistoryWriter assignmentHistoryWriter;
    private final StaffResponseMapper responseMapper;
    private final AuditLogService auditLogService;

    public NurseServiceImpl(
            final NurseRepository nurseRepository,
            final HospitalQueryService hospitalQueryService,
            final StaffAdministrationSupport staffSupport,
            final StaffMembershipGuard membershipGuard,
            final StaffAssignmentHistoryWriter assignmentHistoryWriter,
            final StaffResponseMapper responseMapper,
            final AuditLogService auditLogService
    ) {
        this.nurseRepository = nurseRepository;
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
    public NurseResponse create(final CreateNurseRequest request, final String ipAddress, final String userAgent) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID hospitalId = hospitalQueryService.requireDefaultHospitalId();
        staffSupport.assertUserAndDepartment(tenantId, request.userId(), RoleType.NURSE, request.departmentId());
        staffSupport.assertUserNotLinkedElsewhere(request.userId(), membershipGuard::isEmployedElsewhereExcludingNurse);
        staffSupport.assertUniqueUserLink(tenantId, request.userId(), null,
                nurseRepository::existsByTenantIdAndUserId, nurseRepository::existsByTenantIdAndUserIdAndIdNot);
        staffSupport.assertUniqueEmployeeCode(tenantId, request.employeeCode(), null,
                nurseRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                nurseRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot);

        final Nurse nurse = new Nurse();
        staffSupport.applyEmployment(nurse, hospitalId, request.userId(), request.departmentId(), request.employeeCode(),
                request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), true);
        nurse.setShift(request.shift());
        nurse.setQualification(StaffAdministrationSupport.trimToNull(request.qualification()));
        nurse.setLicenseNumber(StaffAdministrationSupport.trimToNull(request.licenseNumber()));

        final Nurse saved = nurseRepository.save(nurse);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.NURSE, saved, null);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        return responseMapper.toNurseResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public NurseResponse getById(final UUID nurseId) {
        return responseMapper.toNurseResponse(require(nurseId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public PageResponse<NurseResponse> search(
            final String search, final EmploymentStatus employmentStatus, final UUID departmentId, final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return PageResponse.from(nurseRepository.findAll(
                StaffSpecifications.withFilters(tenantId, search, employmentStatus, departmentId,
                        "employeeCode", "jobTitle", "qualification", "licenseNumber"),
                staffSupport.sanitizePageable(pageable, StaffAdministrationSupport.DEFAULT_SORT_PROPERTIES)
        ).map(responseMapper::toNurseResponse));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    public NurseResponse update(
            final UUID nurseId, final UpdateNurseRequest request, final String ipAddress, final String userAgent
    ) {
        final Nurse nurse = require(nurseId);
        final String old = snapshot(nurse);
        final UUID previousDepartmentId = nurse.getDepartmentId();
        if (!nurse.getUserId().equals(request.userId())) {
            throw new ConflictException("STAFF_USER_IMMUTABLE", "Staff user link cannot be changed after creation");
        }
        staffSupport.assertUserAndDepartment(nurse.getTenantId(), nurse.getUserId(), RoleType.NURSE, request.departmentId());
        staffSupport.assertUniqueEmployeeCode(nurse.getTenantId(), request.employeeCode(), nurse.getId(),
                nurseRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                nurseRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot);

        staffSupport.applyEmployment(nurse, nurse.getHospitalId(), nurse.getUserId(), request.departmentId(),
                request.employeeCode(), request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), false);
        nurse.setShift(request.shift());
        nurse.setQualification(StaffAdministrationSupport.trimToNull(request.qualification()));
        nurse.setLicenseNumber(StaffAdministrationSupport.trimToNull(request.licenseNumber()));

        final Nurse saved = nurseRepository.save(nurse);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.NURSE, saved, previousDepartmentId);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        return responseMapper.toNurseResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_DELETE)
    public void delete(final UUID nurseId, final String ipAddress, final String userAgent) {
        final Nurse nurse = require(nurseId);
        final String old = snapshot(nurse);
        assignmentHistoryWriter.closeOnSoftDelete(StaffType.NURSE, nurse.getId());
        staffSupport.markSoftDeleted(nurse, SecurityUtils.requireCurrentUser().getUserId());
        nurseRepository.save(nurse);
        audit(nurse, AuditAction.DELETE, old, ipAddress, userAgent);
    }

    private Nurse require(final UUID nurseId) {
        return nurseRepository.findByIdAndTenantId(nurseId, TenantContextHolder.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Nurse not found"));
    }

    private void audit(final Nurse nurse, final AuditAction action, final String oldSnapshot,
                       final String ipAddress, final String userAgent) {
        auditLogService.record(nurse.getTenantId(), SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY, nurse.getId().toString(), action, oldSnapshot, snapshot(nurse), ipAddress, userAgent);
    }

    private static String snapshot(final Nurse nurse) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", nurse.getId());
        fields.put("userId", nurse.getUserId());
        fields.put("departmentId", nurse.getDepartmentId());
        fields.put("employeeCode", nurse.getEmployeeCode());
        fields.put("shift", nurse.getShift());
        fields.put("employmentStatus", nurse.getEmploymentStatus());
        fields.put("deleted", nurse.isDeleted());
        return fields.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
