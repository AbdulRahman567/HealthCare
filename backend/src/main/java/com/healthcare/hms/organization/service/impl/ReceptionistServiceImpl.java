package com.healthcare.hms.organization.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.organization.dto.request.CreateReceptionistRequest;
import com.healthcare.hms.organization.dto.request.UpdateReceptionistRequest;
import com.healthcare.hms.organization.dto.response.ReceptionistResponse;
import com.healthcare.hms.organization.entity.Receptionist;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.mapper.StaffResponseMapper;
import com.healthcare.hms.organization.repository.ReceptionistRepository;
import com.healthcare.hms.organization.repository.StaffSpecifications;
import com.healthcare.hms.organization.service.ReceptionistService;
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
public class ReceptionistServiceImpl implements ReceptionistService {

    private static final String ENTITY = "RECEPTIONIST";

    private final ReceptionistRepository receptionistRepository;
    private final HospitalQueryService hospitalQueryService;
    private final StaffAdministrationSupport staffSupport;
    private final StaffMembershipGuard membershipGuard;
    private final StaffAssignmentHistoryWriter assignmentHistoryWriter;
    private final StaffResponseMapper responseMapper;
    private final AuditLogService auditLogService;

    public ReceptionistServiceImpl(
            final ReceptionistRepository receptionistRepository,
            final HospitalQueryService hospitalQueryService,
            final StaffAdministrationSupport staffSupport,
            final StaffMembershipGuard membershipGuard,
            final StaffAssignmentHistoryWriter assignmentHistoryWriter,
            final StaffResponseMapper responseMapper,
            final AuditLogService auditLogService
    ) {
        this.receptionistRepository = receptionistRepository;
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
    public ReceptionistResponse create(final CreateReceptionistRequest request, final String ipAddress, final String userAgent) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID hospitalId = hospitalQueryService.requireDefaultHospitalId();
        staffSupport.assertUserAndDepartment(tenantId, request.userId(), RoleType.RECEPTIONIST, request.departmentId());
        staffSupport.assertUserNotLinkedElsewhere(request.userId(), membershipGuard::isEmployedElsewhereExcludingReceptionist);
        staffSupport.assertUniqueUserLink(tenantId, request.userId(), null,
                receptionistRepository::existsByTenantIdAndUserId, receptionistRepository::existsByTenantIdAndUserIdAndIdNot);
        staffSupport.assertUniqueEmployeeCode(tenantId, request.employeeCode(), null,
                receptionistRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                receptionistRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot);

        final Receptionist entity = new Receptionist();
        staffSupport.applyEmployment(entity, hospitalId, request.userId(), request.departmentId(), request.employeeCode(),
                request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), true);
        entity.setDeskLocation(StaffAdministrationSupport.trimToNull(request.deskLocation()));
        entity.setLanguages(StaffAdministrationSupport.trimToNull(request.languages()));

        final Receptionist saved = receptionistRepository.save(entity);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.RECEPTIONIST, saved, null);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        return responseMapper.toReceptionistResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public ReceptionistResponse getById(final UUID receptionistId) {
        return responseMapper.toReceptionistResponse(require(receptionistId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public PageResponse<ReceptionistResponse> search(
            final String search, final EmploymentStatus employmentStatus, final UUID departmentId, final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return PageResponse.from(receptionistRepository.findAll(
                StaffSpecifications.withFilters(tenantId, search, employmentStatus, departmentId,
                        "employeeCode", "jobTitle", "deskLocation", "languages"),
                staffSupport.sanitizePageable(pageable, StaffAdministrationSupport.DEFAULT_SORT_PROPERTIES)
        ).map(responseMapper::toReceptionistResponse));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    public ReceptionistResponse update(
            final UUID receptionistId, final UpdateReceptionistRequest request, final String ipAddress, final String userAgent
    ) {
        final Receptionist entity = require(receptionistId);
        final String old = snapshot(entity);
        final UUID previousDepartmentId = entity.getDepartmentId();
        if (!entity.getUserId().equals(request.userId())) {
            throw new ConflictException("STAFF_USER_IMMUTABLE", "Staff user link cannot be changed after creation");
        }
        staffSupport.assertUserAndDepartment(entity.getTenantId(), entity.getUserId(), RoleType.RECEPTIONIST, request.departmentId());
        staffSupport.assertUniqueEmployeeCode(entity.getTenantId(), request.employeeCode(), entity.getId(),
                receptionistRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                receptionistRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot);

        staffSupport.applyEmployment(entity, entity.getHospitalId(), entity.getUserId(), request.departmentId(),
                request.employeeCode(), request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), false);
        entity.setDeskLocation(StaffAdministrationSupport.trimToNull(request.deskLocation()));
        entity.setLanguages(StaffAdministrationSupport.trimToNull(request.languages()));

        final Receptionist saved = receptionistRepository.save(entity);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.RECEPTIONIST, saved, previousDepartmentId);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        return responseMapper.toReceptionistResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_DELETE)
    public void delete(final UUID receptionistId, final String ipAddress, final String userAgent) {
        final Receptionist entity = require(receptionistId);
        final String old = snapshot(entity);
        assignmentHistoryWriter.closeOnSoftDelete(StaffType.RECEPTIONIST, entity.getId());
        staffSupport.markSoftDeleted(entity, SecurityUtils.requireCurrentUser().getUserId());
        receptionistRepository.save(entity);
        audit(entity, AuditAction.DELETE, old, ipAddress, userAgent);
    }

    private Receptionist require(final UUID id) {
        return receptionistRepository.findByIdAndTenantId(id, TenantContextHolder.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found"));
    }

    private void audit(final Receptionist entity, final AuditAction action, final String oldSnapshot,
                       final String ipAddress, final String userAgent) {
        auditLogService.record(entity.getTenantId(), SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY, entity.getId().toString(), action, oldSnapshot, snapshot(entity), ipAddress, userAgent);
    }

    private static String snapshot(final Receptionist entity) {
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
