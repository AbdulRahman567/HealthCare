package com.healthcare.hms.organization.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.dto.request.AssignDepartmentHeadRequest;
import com.healthcare.hms.organization.dto.request.AssignStaffRequest;
import com.healthcare.hms.organization.dto.request.TransferStaffRequest;
import com.healthcare.hms.organization.dto.response.DepartmentResponse;
import com.healthcare.hms.organization.dto.response.StaffAssignmentResponse;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Staff;
import com.healthcare.hms.organization.entity.StaffDepartmentAssignment;
import com.healthcare.hms.organization.enums.AssignmentAction;
import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.enums.StaffType;
import com.healthcare.hms.organization.mapper.DepartmentMapper;
import com.healthcare.hms.organization.mapper.StaffAssignmentMapper;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.StaffDepartmentAssignmentRepository;
import com.healthcare.hms.organization.service.StaffAssignmentService;
import com.healthcare.hms.organization.staff.StaffAdministrationSupport;
import com.healthcare.hms.organization.staff.StaffProfileDirectory;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional staff department assignment, transfer, head designation, and history (Phase 4.4).
 */
@Service
public class StaffAssignmentServiceImpl implements StaffAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(StaffAssignmentServiceImpl.class);
    private static final String ENTITY_ASSIGNMENT = "STAFF_DEPARTMENT_ASSIGNMENT";
    private static final String ENTITY_DEPARTMENT = "DEPARTMENT";
    private static final Set<String> HISTORY_SORT = Set.of("assignedAt", "endedAt", "createdAt", "action");

    private final StaffDepartmentAssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final StaffProfileDirectory staffDirectory;
    private final StaffAdministrationSupport staffSupport;
    private final StaffAssignmentMapper assignmentMapper;
    private final DepartmentMapper departmentMapper;
    private final AuditLogService auditLogService;

    public StaffAssignmentServiceImpl(
            final StaffDepartmentAssignmentRepository assignmentRepository,
            final DepartmentRepository departmentRepository,
            final StaffProfileDirectory staffDirectory,
            final StaffAdministrationSupport staffSupport,
            final StaffAssignmentMapper assignmentMapper,
            final DepartmentMapper departmentMapper,
            final AuditLogService auditLogService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.departmentRepository = departmentRepository;
        this.staffDirectory = staffDirectory;
        this.staffSupport = staffSupport;
        this.assignmentMapper = assignmentMapper;
        this.departmentMapper = departmentMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    public StaffAssignmentResponse assign(
            final AssignStaffRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Staff staff = staffDirectory.require(request.staffType(), request.staffId());
        assertStaffAssignable(staff);

        if (staff.getDepartmentId() != null) {
            if (Objects.equals(staff.getDepartmentId(), request.departmentId())) {
                throw new ConflictException(
                        "STAFF_ALREADY_ASSIGNED",
                        "Staff is already assigned to this department"
                );
            }
            throw new BusinessException(
                    "STAFF_ALREADY_HAS_DEPARTMENT",
                    "Staff already belongs to a department; use transfer instead"
            );
        }

        if (assignmentRepository.findByTenantIdAndStaffTypeAndStaffIdAndEndedAtIsNull(
                tenantId, request.staffType(), request.staffId()).isPresent()) {
            throw new ConflictException(
                    "STAFF_OPEN_ASSIGNMENT_EXISTS",
                    "Staff already has an open department assignment"
            );
        }

        final Department department = requireAssignableDepartment(request.departmentId(), staff.getHospitalId());
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        final Instant now = Instant.now();

        staff.setDepartmentId(department.getId());
        staffDirectory.save(request.staffType(), staff);

        final StaffDepartmentAssignment assignment = openAssignment(
                staff,
                request.staffType(),
                department.getId(),
                null,
                AssignmentAction.ASSIGN,
                StaffAdministrationSupport.trimToNull(request.reason()),
                actorId,
                now
        );
        final StaffDepartmentAssignment saved = assignmentRepository.save(assignment);

        auditAssignment(saved, AuditAction.CREATE, null, ipAddress, userAgent);
        log.info(
                "Staff assigned staffType={} staffId={} departmentId={} tenantId={} actorId={}",
                request.staffType(),
                request.staffId(),
                department.getId(),
                tenantId,
                actorId
        );
        return assignmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    public StaffAssignmentResponse transfer(
            final TransferStaffRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Staff staff = staffDirectory.require(request.staffType(), request.staffId());
        assertStaffAssignable(staff);

        final UUID fromDepartmentId = staff.getDepartmentId();
        if (fromDepartmentId == null) {
            throw new BusinessException(
                    "STAFF_NOT_ASSIGNED",
                    "Staff has no current department; use assign instead"
            );
        }
        if (Objects.equals(fromDepartmentId, request.toDepartmentId())) {
            throw new ConflictException(
                    "STAFF_ALREADY_ASSIGNED",
                    "Staff is already assigned to this department"
            );
        }

        final Department toDepartment = requireAssignableDepartment(
                request.toDepartmentId(),
                staff.getHospitalId()
        );
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        final Instant now = Instant.now();

        closeOpenAssignment(tenantId, request.staffType(), request.staffId(), actorId, now);
        clearHeadIfLeaving(fromDepartmentId, request.staffType(), request.staffId(), actorId, ipAddress, userAgent);

        staff.setDepartmentId(toDepartment.getId());
        staffDirectory.save(request.staffType(), staff);

        final StaffDepartmentAssignment assignment = openAssignment(
                staff,
                request.staffType(),
                toDepartment.getId(),
                fromDepartmentId,
                AssignmentAction.TRANSFER,
                StaffAdministrationSupport.trimToNull(request.reason()),
                actorId,
                now
        );
        final StaffDepartmentAssignment saved = assignmentRepository.save(assignment);

        auditAssignment(saved, AuditAction.UPDATE, null, ipAddress, userAgent);
        log.info(
                "Staff transferred staffType={} staffId={} from={} to={} tenantId={} actorId={}",
                request.staffType(),
                request.staffId(),
                fromDepartmentId,
                toDepartment.getId(),
                tenantId,
                actorId
        );
        return assignmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.DEPARTMENT_UPDATE)
    public DepartmentResponse assignDepartmentHead(
            final UUID departmentId,
            final AssignDepartmentHeadRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final Department department = requireDepartment(departmentId);
        final Staff staff = staffDirectory.require(request.staffType(), request.staffId());
        assertStaffAssignable(staff);

        if (!Objects.equals(staff.getHospitalId(), department.getHospitalId())) {
            throw new BusinessException(
                    "DEPARTMENT_HEAD_HOSPITAL_MISMATCH",
                    "Department head must belong to the same hospital as the department"
            );
        }
        if (!Objects.equals(staff.getDepartmentId(), department.getId())) {
            throw new BusinessException(
                    "DEPARTMENT_HEAD_NOT_IN_DEPARTMENT",
                    "Department head must be currently assigned to the department"
            );
        }

        final String oldSnapshot = departmentSnapshot(department);
        department.assignHead(staff.getId(), request.staffType(), staff.getUserId());
        final Department saved = departmentRepository.save(department);

        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_DEPARTMENT,
                saved.getId().toString(),
                AuditAction.UPDATE,
                oldSnapshot,
                departmentSnapshot(saved),
                ipAddress,
                userAgent
        );

        log.info(
                "Department head assigned departmentId={} staffType={} staffId={} actorId={}",
                departmentId,
                request.staffType(),
                request.staffId(),
                actorId
        );
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.DEPARTMENT_UPDATE)
    public DepartmentResponse clearDepartmentHead(
            final UUID departmentId,
            final String ipAddress,
            final String userAgent
    ) {
        final Department department = requireDepartment(departmentId);
        final String oldSnapshot = departmentSnapshot(department);
        department.clearHead();
        final Department saved = departmentRepository.save(department);

        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                saved.getTenantId(),
                actorId,
                ENTITY_DEPARTMENT,
                saved.getId().toString(),
                AuditAction.UPDATE,
                oldSnapshot,
                departmentSnapshot(saved),
                ipAddress,
                userAgent
        );

        log.info("Department head cleared departmentId={} actorId={}", departmentId, actorId);
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public StaffAssignmentResponse getCurrent(final StaffType staffType, final UUID staffId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        staffDirectory.require(staffType, staffId);
        return assignmentRepository
                .findByTenantIdAndStaffTypeAndStaffIdAndEndedAtIsNull(tenantId, staffType, staffId)
                .map(assignmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No open department assignment for staff"));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.STAFF_READ)
    public PageResponse<StaffAssignmentResponse> history(
            final StaffType staffType,
            final UUID staffId,
            final UUID departmentId,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final Pageable safe = staffSupport.sanitizePageable(pageable, HISTORY_SORT);

        if (staffType != null && staffId != null) {
            staffDirectory.require(staffType, staffId);
            final Page<StaffAssignmentResponse> page = assignmentRepository
                    .findByTenantIdAndStaffTypeAndStaffIdOrderByAssignedAtDesc(
                            tenantId, staffType, staffId, safe)
                    .map(assignmentMapper::toResponse);
            return PageResponse.from(page);
        }

        if (departmentId != null) {
            requireDepartment(departmentId);
            final Page<StaffAssignmentResponse> page = assignmentRepository
                    .findByTenantIdAndDepartmentIdOrderByAssignedAtDesc(tenantId, departmentId, safe)
                    .map(assignmentMapper::toResponse);
            return PageResponse.from(page);
        }

        throw new BusinessException(
                "ASSIGNMENT_HISTORY_FILTER_REQUIRED",
                "Provide staffType+staffId or departmentId to list assignment history"
        );
    }

    private void closeOpenAssignment(
            final UUID tenantId,
            final StaffType staffType,
            final UUID staffId,
            final UUID actorId,
            final Instant endedAt
    ) {
        final Optional<StaffDepartmentAssignment> open = assignmentRepository
                .findByTenantIdAndStaffTypeAndStaffIdAndEndedAtIsNull(tenantId, staffType, staffId);
        open.ifPresent(assignment -> {
            assignment.close(endedAt, actorId);
            assignmentRepository.save(assignment);
        });
    }

    private void clearHeadIfLeaving(
            final UUID fromDepartmentId,
            final StaffType staffType,
            final UUID staffId,
            final UUID actorId,
            final String ipAddress,
            final String userAgent
    ) {
        final Department from = requireDepartment(fromDepartmentId);
        if (Objects.equals(from.getHeadStaffId(), staffId)
                && Objects.equals(from.getHeadStaffType(), staffType)) {
            final String oldSnapshot = departmentSnapshot(from);
            from.clearHead();
            departmentRepository.save(from);
            auditLogService.record(
                    from.getTenantId(),
                    actorId,
                    ENTITY_DEPARTMENT,
                    from.getId().toString(),
                    AuditAction.UPDATE,
                    oldSnapshot,
                    departmentSnapshot(from),
                    ipAddress,
                    userAgent
            );
        }
    }

    private StaffDepartmentAssignment openAssignment(
            final Staff staff,
            final StaffType staffType,
            final UUID departmentId,
            final UUID fromDepartmentId,
            final AssignmentAction action,
            final String reason,
            final UUID actorId,
            final Instant assignedAt
    ) {
        final StaffDepartmentAssignment assignment = new StaffDepartmentAssignment();
        assignment.setHospitalId(staff.getHospitalId());
        assignment.setStaffType(staffType);
        assignment.setStaffId(staff.getId());
        assignment.setDepartmentId(departmentId);
        assignment.setFromDepartmentId(fromDepartmentId);
        assignment.setAction(action);
        assignment.setReason(reason);
        assignment.setAssignedAt(assignedAt);
        assignment.setAssignedBy(actorId);
        return assignment;
    }

    private Department requireAssignableDepartment(final UUID departmentId, final UUID hospitalId) {
        final Department department = requireDepartment(departmentId);
        if (department.getStatus() != DepartmentStatus.ACTIVE) {
            throw new BusinessException(
                    "DEPARTMENT_NOT_ACTIVE",
                    "Staff can only be assigned to an active department"
            );
        }
        if (!Objects.equals(department.getHospitalId(), hospitalId)) {
            throw new BusinessException(
                    "DEPARTMENT_HOSPITAL_MISMATCH",
                    "Department must belong to the same hospital as the staff member"
            );
        }
        return department;
    }

    private Department requireDepartment(final UUID departmentId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    private static void assertStaffAssignable(final Staff staff) {
        if (staff.getEmploymentStatus() == EmploymentStatus.TERMINATED) {
            throw new BusinessException(
                    "STAFF_TERMINATED",
                    "Terminated staff cannot be assigned to a department"
            );
        }
    }

    private void auditAssignment(
            final StaffDepartmentAssignment assignment,
            final AuditAction action,
            final String oldValue,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                assignment.getTenantId(),
                actorId,
                ENTITY_ASSIGNMENT,
                assignment.getId().toString(),
                action,
                oldValue,
                assignmentSnapshot(assignment),
                ipAddress,
                userAgent
        );
    }

    private static String assignmentSnapshot(final StaffDepartmentAssignment assignment) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", assignment.getId());
        fields.put("staffType", assignment.getStaffType());
        fields.put("staffId", assignment.getStaffId());
        fields.put("departmentId", assignment.getDepartmentId());
        fields.put("fromDepartmentId", assignment.getFromDepartmentId());
        fields.put("action", assignment.getAction());
        fields.put("reason", assignment.getReason());
        fields.put("assignedAt", assignment.getAssignedAt());
        fields.put("endedAt", assignment.getEndedAt());
        fields.put("open", assignment.isOpen());
        return fields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String departmentSnapshot(final Department department) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", department.getId());
        fields.put("code", department.getCode());
        fields.put("headUserId", department.getHeadUserId());
        fields.put("headStaffId", department.getHeadStaffId());
        fields.put("headStaffType", department.getHeadStaffType());
        return fields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
