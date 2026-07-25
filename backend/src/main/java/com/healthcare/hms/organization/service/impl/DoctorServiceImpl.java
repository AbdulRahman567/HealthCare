package com.healthcare.hms.organization.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.organization.dto.request.CreateDoctorRequest;
import com.healthcare.hms.organization.dto.request.UpdateDoctorRequest;
import com.healthcare.hms.organization.dto.response.DoctorResponse;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.mapper.StaffResponseMapper;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.organization.repository.StaffSpecifications;
import com.healthcare.hms.organization.service.DoctorService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorServiceImpl implements DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorServiceImpl.class);
    private static final String ENTITY = "DOCTOR";

    private final DoctorRepository doctorRepository;
    private final HospitalQueryService hospitalQueryService;
    private final StaffAdministrationSupport staffSupport;
    private final StaffMembershipGuard membershipGuard;
    private final StaffAssignmentHistoryWriter assignmentHistoryWriter;
    private final StaffResponseMapper responseMapper;
    private final AuditLogService auditLogService;

    public DoctorServiceImpl(
            final DoctorRepository doctorRepository,
            final HospitalQueryService hospitalQueryService,
            final StaffAdministrationSupport staffSupport,
            final StaffMembershipGuard membershipGuard,
            final StaffAssignmentHistoryWriter assignmentHistoryWriter,
            final StaffResponseMapper responseMapper,
            final AuditLogService auditLogService
    ) {
        this.doctorRepository = doctorRepository;
        this.hospitalQueryService = hospitalQueryService;
        this.staffSupport = staffSupport;
        this.membershipGuard = membershipGuard;
        this.assignmentHistoryWriter = assignmentHistoryWriter;
        this.responseMapper = responseMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.DOCTOR_CREATE)
    public DoctorResponse create(
            final CreateDoctorRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID hospitalId = hospitalQueryService.requireDefaultHospitalId();
        staffSupport.assertUserAndDepartment(tenantId, request.userId(), RoleType.DOCTOR, request.departmentId());
        staffSupport.assertUserNotLinkedElsewhere(
                request.userId(),
                membershipGuard::isEmployedElsewhereExcludingDoctor
        );
        staffSupport.assertUniqueUserLink(
                tenantId,
                request.userId(),
                null,
                doctorRepository::existsByTenantIdAndUserId,
                doctorRepository::existsByTenantIdAndUserIdAndIdNot
        );
        staffSupport.assertUniqueEmployeeCode(
                tenantId,
                request.employeeCode(),
                null,
                doctorRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                doctorRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot
        );
        assertUniqueLicense(tenantId, request.licenseNumber(), null);

        final Doctor doctor = new Doctor();
        applyRequest(doctor, hospitalId, request.userId(), request.departmentId(), request.employeeCode(),
                request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), true);
        doctor.setSpecialization(request.specialization().trim());
        doctor.setLicenseNumber(normalizeLicense(request.licenseNumber()));
        doctor.setQualification(StaffAdministrationSupport.trimToNull(request.qualification()));
        doctor.setExperienceYears(request.experienceYears());
        doctor.setConsultationFee(request.consultationFee());

        final Doctor saved = doctorRepository.save(doctor);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.DOCTOR, saved, null);
        audit(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info("Doctor created id={} tenantId={}", saved.getId(), saved.getTenantId());
        return responseMapper.toDoctorResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.DOCTOR_READ)
    public DoctorResponse getById(final UUID doctorId) {
        return responseMapper.toDoctorResponse(require(doctorId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.DOCTOR_READ)
    public PageResponse<DoctorResponse> search(
            final String search,
            final EmploymentStatus employmentStatus,
            final UUID departmentId,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return PageResponse.from(doctorRepository.findAll(
                StaffSpecifications.withFilters(
                        tenantId,
                        search,
                        employmentStatus,
                        departmentId,
                        "employeeCode", "jobTitle", "specialization", "licenseNumber", "qualification"
                ),
                staffSupport.sanitizePageable(pageable, StaffAdministrationSupport.DEFAULT_SORT_PROPERTIES)
        ).map(responseMapper::toDoctorResponse));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.DOCTOR_UPDATE)
    public DoctorResponse update(
            final UUID doctorId,
            final UpdateDoctorRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final Doctor doctor = require(doctorId);
        final String old = snapshot(doctor);
        final UUID previousDepartmentId = doctor.getDepartmentId();
        final UUID tenantId = doctor.getTenantId();

        staffSupport.assertUserAndDepartment(tenantId, doctor.getUserId(), RoleType.DOCTOR, request.departmentId());
        if (!doctor.getUserId().equals(request.userId())) {
            throw new ConflictException("STAFF_USER_IMMUTABLE", "Staff user link cannot be changed after creation");
        }
        staffSupport.assertUniqueEmployeeCode(
                tenantId,
                request.employeeCode(),
                doctor.getId(),
                doctorRepository::existsByTenantIdAndEmployeeCodeIgnoreCase,
                doctorRepository::existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot
        );
        assertUniqueLicense(tenantId, request.licenseNumber(), doctor.getId());

        applyRequest(doctor, doctor.getHospitalId(), doctor.getUserId(), request.departmentId(), request.employeeCode(),
                request.jobTitle(), request.employmentStatus(), request.employmentType(),
                request.hiredAt(), request.terminatedAt(), request.reportsToStaffId(), false);
        doctor.setSpecialization(request.specialization().trim());
        doctor.setLicenseNumber(normalizeLicense(request.licenseNumber()));
        doctor.setQualification(StaffAdministrationSupport.trimToNull(request.qualification()));
        doctor.setExperienceYears(request.experienceYears());
        doctor.setConsultationFee(request.consultationFee());

        final Doctor saved = doctorRepository.save(doctor);
        assignmentHistoryWriter.syncAfterDepartmentChange(StaffType.DOCTOR, saved, previousDepartmentId);
        audit(saved, AuditAction.UPDATE, old, ipAddress, userAgent);
        return responseMapper.toDoctorResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.DOCTOR_DELETE)
    public void delete(final UUID doctorId, final String ipAddress, final String userAgent) {
        final Doctor doctor = require(doctorId);
        final String old = snapshot(doctor);
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        assignmentHistoryWriter.closeOnSoftDelete(StaffType.DOCTOR, doctor.getId());
        staffSupport.markSoftDeleted(doctor, actorId);
        doctor.setLicenseNumber(StaffAdministrationSupport.releaseUniqueValue(
                doctor.getLicenseNumber(), doctor.getId(), 100));
        doctorRepository.save(doctor);
        audit(doctor, AuditAction.DELETE, old, ipAddress, userAgent);
    }

    private Doctor require(final UUID doctorId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return doctorRepository.findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private void assertUniqueLicense(final UUID tenantId, final String licenseNumber, final UUID excludeId) {
        final String normalized = normalizeLicense(licenseNumber);
        final boolean exists = excludeId == null
                ? doctorRepository.existsByTenantIdAndLicenseNumberIgnoreCase(tenantId, normalized)
                : doctorRepository.existsByTenantIdAndLicenseNumberIgnoreCaseAndIdNot(tenantId, normalized, excludeId);
        if (exists) {
            throw new ConflictException("DOCTOR_LICENSE_EXISTS", "Doctor license number is already in use for this tenant");
        }
    }

    private void applyRequest(
            final Doctor doctor,
            final UUID hospitalId,
            final UUID userId,
            final UUID departmentId,
            final String employeeCode,
            final String jobTitle,
            final EmploymentStatus employmentStatus,
            final com.healthcare.hms.organization.enums.EmploymentType employmentType,
            final java.time.LocalDate hiredAt,
            final java.time.LocalDate terminatedAt,
            final UUID reportsToStaffId,
            final boolean create
    ) {
        staffSupport.applyEmployment(
                doctor, hospitalId, userId, departmentId, employeeCode, jobTitle,
                employmentStatus, employmentType, hiredAt, terminatedAt, reportsToStaffId, create
        );
    }

    private void audit(
            final Doctor doctor,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                doctor.getTenantId(),
                actorId,
                ENTITY,
                doctor.getId().toString(),
                action,
                oldSnapshot,
                snapshot(doctor),
                ipAddress,
                userAgent
        );
    }

    private static String normalizeLicense(final String licenseNumber) {
        return licenseNumber.trim().toUpperCase(Locale.ROOT);
    }

    private static String snapshot(final Doctor doctor) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", doctor.getId());
        fields.put("userId", doctor.getUserId());
        fields.put("departmentId", doctor.getDepartmentId());
        fields.put("employeeCode", doctor.getEmployeeCode());
        fields.put("specialization", doctor.getSpecialization());
        fields.put("licenseNumber", doctor.getLicenseNumber());
        fields.put("employmentStatus", doctor.getEmploymentStatus());
        fields.put("deleted", doctor.isDeleted());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
