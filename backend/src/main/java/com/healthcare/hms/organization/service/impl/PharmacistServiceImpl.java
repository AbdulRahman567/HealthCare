package com.healthcare.hms.organization.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.organization.dto.request.CreatePharmacistRequest;
import com.healthcare.hms.organization.dto.request.UpdatePharmacistRequest;
import com.healthcare.hms.organization.dto.response.PharmacistResponse;
import com.healthcare.hms.organization.entity.Pharmacist;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.mapper.StaffResponseMapper;
import com.healthcare.hms.organization.repository.PharmacistRepository;
import com.healthcare.hms.organization.repository.StaffSpecifications;
import com.healthcare.hms.organization.service.PharmacistService;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PharmacistServiceImpl implements PharmacistService {

    private static final String ENTITY = "PHARMACIST";

    private final PharmacistRepository pharmacistRepository;
    private final HospitalQueryService hospitalQueryService;
    private final StaffAdministrationSupport staffSupport;
    private final StaffMembershipGuard membershipGuard;
    private final StaffAssignmentHistoryWriter assignmentHistoryWriter;
    private final StaffResponseMapper responseMapper;
    private final AuditLogService auditLogService;

    public PharmacistServiceImpl(
            final PharmacistRepository pharmacistRepository,
            final HospitalQueryService hospitalQueryService,
            final StaffAdministrationSupport staffSupport,
            final StaffMembershipGuard membershipGuard,
            final StaffAssignmentHistoryWriter assignmentHistoryWriter,
            final StaffResponseMapper responseMapper,
            final AuditLogService auditLogService
    ) {
        this.pharmacistRepository = pharmacistRepository;
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
    public PharmacistResponse create(final CreatePharmacistRequest request, final String ipAddress, final String userAgent) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID hospitalId = hospitalQueryService.requireDefaultHospitalId();
        staffSupport.assertUserAndDepartment(tenantId, request.userId(), RoleType.PHARMACIST, request.departmentId());
        staffSupport.assertUserNotLinkedElsewhere(request.userId(), membershipGuard::isEmployedElsewhereExcludingPharmacist);
        staffSupport.assertUniqueUserLink(tenantId, request.userId(), null,
                pharmacistRepository::existsByTenantIdAndUserId, pharmacistRepository::existsByTenantIdAndUserIdAndIdNot);
        staffSupport.assertUniqueEmployeeCode(tenantId, request.employeeCode(), null,
                pharmacistRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                pharmacistRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot);
        assertUniqueLicense(tenantId, request.licenseNumber(), null);

        final Pharmacist entity = new Pharmacist();
        staffSupport.applyEmployment(entity, hospitalId, request.userId(), request.departmentId(), request.employeeCode(),
                request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), true);
        entity.setLicenseNumber(normalizeLicense(request.licenseNumber()));
        entity.setPharmacyLocation(StaffAdministrationSupport.trimToNull(request.pharmacyLocation()));
        entity.setQualification(StaffAdministrationSupport.trimToNull(request.qualification()));

        final Pharmacist saved = pharmacistRepository.save(entity);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.PHARMACIST, saved, null);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        return responseMapper.toPharmacistResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public PharmacistResponse getById(final UUID pharmacistId) {
        return responseMapper.toPharmacistResponse(require(pharmacistId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public PageResponse<PharmacistResponse> search(
            final String search, final EmploymentStatus employmentStatus, final UUID departmentId, final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return PageResponse.from(pharmacistRepository.findAll(
                StaffSpecifications.withFilters(tenantId, search, employmentStatus, departmentId,
                        "employeeCode", "jobTitle", "licenseNumber", "pharmacyLocation", "qualification"),
                staffSupport.sanitizePageable(pageable, StaffAdministrationSupport.DEFAULT_SORT_PROPERTIES)
        ).map(responseMapper::toPharmacistResponse));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    public PharmacistResponse update(
            final UUID pharmacistId, final UpdatePharmacistRequest request, final String ipAddress, final String userAgent
    ) {
        final Pharmacist entity = require(pharmacistId);
        final String old = snapshot(entity);
        final UUID previousDepartmentId = entity.getDepartmentId();
        if (!entity.getUserId().equals(request.userId())) {
            throw new ConflictException("STAFF_USER_IMMUTABLE", "Staff user link cannot be changed after creation");
        }
        staffSupport.assertUserAndDepartment(entity.getTenantId(), entity.getUserId(), RoleType.PHARMACIST, request.departmentId());
        staffSupport.assertUniqueEmployeeCode(entity.getTenantId(), request.employeeCode(), entity.getId(),
                pharmacistRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                pharmacistRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot);
        assertUniqueLicense(entity.getTenantId(), request.licenseNumber(), entity.getId());

        staffSupport.applyEmployment(entity, entity.getHospitalId(), entity.getUserId(), request.departmentId(),
                request.employeeCode(), request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), false);
        entity.setLicenseNumber(normalizeLicense(request.licenseNumber()));
        entity.setPharmacyLocation(StaffAdministrationSupport.trimToNull(request.pharmacyLocation()));
        entity.setQualification(StaffAdministrationSupport.trimToNull(request.qualification()));

        final Pharmacist saved = pharmacistRepository.save(entity);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.PHARMACIST, saved, previousDepartmentId);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        return responseMapper.toPharmacistResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_DELETE)
    public void delete(final UUID pharmacistId, final String ipAddress, final String userAgent) {
        final Pharmacist entity = require(pharmacistId);
        final String old = snapshot(entity);
        assignmentHistoryWriter.closeOnSoftDelete(StaffType.PHARMACIST, entity.getId());
        staffSupport.markSoftDeleted(entity, SecurityUtils.requireCurrentUser().getUserId());
        entity.setLicenseNumber(StaffAdministrationSupport.releaseUniqueValue(
                entity.getLicenseNumber(), entity.getId(), 100));
        pharmacistRepository.save(entity);
        audit(entity, AuditAction.DELETE, old, ipAddress, userAgent);
    }

    private Pharmacist require(final UUID id) {
        return pharmacistRepository.findByIdAndTenantId(id, TenantContextHolder.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacist not found"));
    }

    private void assertUniqueLicense(final UUID tenantId, final String licenseNumber, final UUID excludeId) {
        final String normalized = normalizeLicense(licenseNumber);
        final boolean exists = excludeId == null
                ? pharmacistRepository.existsByTenantIdAndLicenseNumberIgnoreCase(tenantId, normalized)
                : pharmacistRepository.existsByTenantIdAndLicenseNumberIgnoreCaseAndIdNot(tenantId, normalized, excludeId);
        if (exists) {
            throw new ConflictException("PHARMACIST_LICENSE_EXISTS", "Pharmacist license number is already in use for this tenant");
        }
    }

    private void audit(final Pharmacist entity, final AuditAction action, final String oldSnapshot,
                       final String ipAddress, final String userAgent) {
        auditLogService.record(entity.getTenantId(), SecurityUtils.requireCurrentUser().getUserId(),
                ENTITY, entity.getId().toString(), action, oldSnapshot, snapshot(entity), ipAddress, userAgent);
    }

    private static String normalizeLicense(final String licenseNumber) {
        return licenseNumber.trim().toUpperCase(Locale.ROOT);
    }

    private static String snapshot(final Pharmacist entity) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", entity.getId());
        fields.put("userId", entity.getUserId());
        fields.put("departmentId", entity.getDepartmentId());
        fields.put("employeeCode", entity.getEmployeeCode());
        fields.put("licenseNumber", entity.getLicenseNumber());
        fields.put("employmentStatus", entity.getEmploymentStatus());
        fields.put("deleted", entity.isDeleted());
        return fields.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
