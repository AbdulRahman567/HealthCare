package com.healthcare.hms.organization.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.organization.dto.request.AssignDepartmentHeadRequest;
import com.healthcare.hms.organization.dto.request.AssignStaffRequest;
import com.healthcare.hms.organization.dto.request.TransferStaffRequest;
import com.healthcare.hms.organization.dto.response.DepartmentResponse;
import com.healthcare.hms.organization.dto.response.StaffAssignmentResponse;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Nurse;
import com.healthcare.hms.organization.entity.StaffDepartmentAssignment;
import com.healthcare.hms.organization.enums.AssignmentAction;
import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.DepartmentType;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.enums.EmploymentType;
import com.healthcare.hms.organization.enums.StaffType;
import com.healthcare.hms.organization.mapper.DepartmentMapper;
import com.healthcare.hms.organization.mapper.StaffAssignmentMapper;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.StaffDepartmentAssignmentRepository;
import com.healthcare.hms.organization.staff.StaffAdministrationSupport;
import com.healthcare.hms.organization.staff.StaffProfileDirectory;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.users.service.UserQueryService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("StaffAssignmentServiceImpl")
class StaffAssignmentServiceImplTest {

    @Mock
    private StaffDepartmentAssignmentRepository assignmentRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private StaffProfileDirectory staffDirectory;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private com.healthcare.hms.organization.service.DepartmentQueryService departmentQueryService;
    @Mock
    private StaffAssignmentMapper assignmentMapper;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private AuditLogService auditLogService;

    private StaffAssignmentServiceImpl service;
    private UUID tenantId;
    private UUID actorId;
    private UUID hospitalId;
    private UUID staffId;
    private UUID departmentId;
    private UUID otherDepartmentId;
    private UUID staffUserId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        staffId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        otherDepartmentId = UUID.randomUUID();
        staffUserId = UUID.randomUUID();

        final StaffAdministrationSupport support = new StaffAdministrationSupport(
                userQueryService,
                departmentQueryService
        );
        service = new StaffAssignmentServiceImpl(
                assignmentRepository,
                departmentRepository,
                staffDirectory,
                support,
                assignmentMapper,
                departmentMapper,
                auditLogService
        );

        TenantContextHolder.set(new TenantContext(tenantId, "city", TenantType.HOSPITAL, TenantStatus.ACTIVE));
        final AuthenticatedUser principal = new AuthenticatedUser(
                actorId,
                tenantId,
                "admin@city.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("STAFF_UPDATE", "STAFF_READ", "DEPARTMENT_UPDATE"),
                1L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("assign rejects duplicate department affiliation")
    void assign_duplicate_throws() {
        final Nurse nurse = nurse(departmentId);
        when(staffDirectory.require(StaffType.NURSE, staffId)).thenReturn(nurse);

        assertThatThrownBy(() -> service.assign(
                new AssignStaffRequest(StaffType.NURSE, staffId, departmentId, null),
                "127.0.0.1",
                "junit"
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already assigned");
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("assign requires transfer when staff already has another department")
    void assign_whenAlreadyAssignedElsewhere_throws() {
        final Nurse nurse = nurse(otherDepartmentId);
        when(staffDirectory.require(StaffType.NURSE, staffId)).thenReturn(nurse);

        assertThatThrownBy(() -> service.assign(
                new AssignStaffRequest(StaffType.NURSE, staffId, departmentId, "move"),
                "127.0.0.1",
                "junit"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo("STAFF_ALREADY_HAS_DEPARTMENT");
    }

    @Test
    @DisplayName("assign opens history when staff has no department")
    void assign_success() {
        final Nurse nurse = nurse(null);
        final Department department = activeDepartment(departmentId);
        final StaffDepartmentAssignment saved = openAssignment(AssignmentAction.ASSIGN, departmentId, null);
        final StaffAssignmentResponse response = sampleResponse(saved);

        when(staffDirectory.require(StaffType.NURSE, staffId)).thenReturn(nurse);
        when(assignmentRepository.findByTenantIdAndStaffTypeAndStaffIdAndEndedAtIsNull(
                tenantId, StaffType.NURSE, staffId)).thenReturn(Optional.empty());
        when(departmentRepository.findByIdAndTenantId(departmentId, tenantId)).thenReturn(Optional.of(department));
        when(staffDirectory.save(eq(StaffType.NURSE), any())).thenAnswer(inv -> inv.getArgument(1));
        when(assignmentRepository.save(any())).thenAnswer(inv -> {
            final StaffDepartmentAssignment row = inv.getArgument(0);
            row.setId(saved.getId());
            row.setTenantId(tenantId);
            return row;
        });
        when(assignmentMapper.toResponse(any())).thenReturn(response);

        final StaffAssignmentResponse result = service.assign(
                new AssignStaffRequest(StaffType.NURSE, staffId, departmentId, "onboard"),
                "127.0.0.1",
                "junit"
        );

        assertThat(result).isEqualTo(response);
        assertThat(nurse.getDepartmentId()).isEqualTo(departmentId);
        verify(auditLogService).record(
                eq(tenantId),
                eq(actorId),
                eq("STAFF_DEPARTMENT_ASSIGNMENT"),
                any(),
                eq(AuditAction.CREATE),
                isNull(),
                any(),
                eq("127.0.0.1"),
                eq("junit")
        );
    }

    @Test
    @DisplayName("transfer rejects same department")
    void transfer_sameDepartment_throws() {
        final Nurse nurse = nurse(departmentId);
        when(staffDirectory.require(StaffType.NURSE, staffId)).thenReturn(nurse);

        assertThatThrownBy(() -> service.transfer(
                new TransferStaffRequest(StaffType.NURSE, staffId, departmentId, null),
                "127.0.0.1",
                "junit"
        ))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("transfer moves staff and records TRANSFER history")
    void transfer_success() {
        final Nurse nurse = nurse(departmentId);
        final Department target = activeDepartment(otherDepartmentId);
        final StaffDepartmentAssignment open = openAssignment(AssignmentAction.ASSIGN, departmentId, null);
        final StaffDepartmentAssignment saved = openAssignment(AssignmentAction.TRANSFER, otherDepartmentId, departmentId);
        final StaffAssignmentResponse response = sampleResponse(saved);

        when(staffDirectory.require(StaffType.NURSE, staffId)).thenReturn(nurse);
        when(departmentRepository.findByIdAndTenantId(otherDepartmentId, tenantId)).thenReturn(Optional.of(target));
        when(departmentRepository.findByIdAndTenantId(departmentId, tenantId))
                .thenReturn(Optional.of(activeDepartment(departmentId)));
        when(assignmentRepository.findByTenantIdAndStaffTypeAndStaffIdAndEndedAtIsNull(
                tenantId, StaffType.NURSE, staffId)).thenReturn(Optional.of(open));
        when(staffDirectory.save(eq(StaffType.NURSE), any())).thenAnswer(inv -> inv.getArgument(1));
        when(assignmentRepository.save(any())).thenAnswer(inv -> {
            final StaffDepartmentAssignment row = inv.getArgument(0);
            if (row.getId() == null) {
                row.setId(saved.getId());
                row.setTenantId(tenantId);
            }
            return row;
        });
        when(assignmentMapper.toResponse(any())).thenReturn(response);

        final StaffAssignmentResponse result = service.transfer(
                new TransferStaffRequest(StaffType.NURSE, staffId, otherDepartmentId, "rotation"),
                "127.0.0.1",
                "junit"
        );

        assertThat(result).isEqualTo(response);
        assertThat(nurse.getDepartmentId()).isEqualTo(otherDepartmentId);
        assertThat(open.getEndedAt()).isNotNull();

        final ArgumentCaptor<StaffDepartmentAssignment> captor = ArgumentCaptor.forClass(StaffDepartmentAssignment.class);
        verify(assignmentRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().stream().anyMatch(a -> a.getAction() == AssignmentAction.TRANSFER)).isTrue();
    }

    @Test
    @DisplayName("assign department head requires staff in department")
    void assignHead_notInDepartment_throws() {
        final Department department = activeDepartment(departmentId);
        final Nurse nurse = nurse(otherDepartmentId);
        when(departmentRepository.findByIdAndTenantId(departmentId, tenantId)).thenReturn(Optional.of(department));
        when(staffDirectory.require(StaffType.NURSE, staffId)).thenReturn(nurse);

        assertThatThrownBy(() -> service.assignDepartmentHead(
                departmentId,
                new AssignDepartmentHeadRequest(StaffType.NURSE, staffId),
                "127.0.0.1",
                "junit"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo("DEPARTMENT_HEAD_NOT_IN_DEPARTMENT");
    }

    @Test
    @DisplayName("assign department head syncs staff and user ids")
    void assignHead_success() {
        final Department department = activeDepartment(departmentId);
        final Nurse nurse = nurse(departmentId);
        final DepartmentResponse mapped = new DepartmentResponse(
                departmentId, tenantId, hospitalId, "Cardiology", "CARD", null,
                DepartmentType.CLINICAL, DepartmentStatus.ACTIVE, null,
                staffUserId, staffId, StaffType.NURSE,
                null, null, null, null, 0L
        );

        when(departmentRepository.findByIdAndTenantId(departmentId, tenantId)).thenReturn(Optional.of(department));
        when(staffDirectory.require(StaffType.NURSE, staffId)).thenReturn(nurse);
        when(departmentRepository.save(department)).thenReturn(department);
        when(departmentMapper.toResponse(department)).thenReturn(mapped);

        final DepartmentResponse result = service.assignDepartmentHead(
                departmentId,
                new AssignDepartmentHeadRequest(StaffType.NURSE, staffId),
                "127.0.0.1",
                "junit"
        );

        assertThat(result.headStaffId()).isEqualTo(staffId);
        assertThat(department.getHeadStaffId()).isEqualTo(staffId);
        assertThat(department.getHeadStaffType()).isEqualTo(StaffType.NURSE);
        assertThat(department.getHeadUserId()).isEqualTo(staffUserId);
        verify(auditLogService).record(
                eq(tenantId),
                eq(actorId),
                eq("DEPARTMENT"),
                eq(departmentId.toString()),
                eq(AuditAction.UPDATE),
                any(),
                any(),
                eq("127.0.0.1"),
                eq("junit")
        );
    }

    private Nurse nurse(final UUID currentDepartmentId) {
        final Nurse nurse = new Nurse();
        nurse.setId(staffId);
        nurse.setTenantId(tenantId);
        nurse.setHospitalId(hospitalId);
        nurse.setUserId(staffUserId);
        nurse.setDepartmentId(currentDepartmentId);
        nurse.setEmployeeCode("NUR-001");
        nurse.setEmploymentStatus(EmploymentStatus.ACTIVE);
        nurse.setEmploymentType(EmploymentType.FULL_TIME);
        return nurse;
    }

    private Department activeDepartment(final UUID id) {
        final Department department = new Department();
        department.setId(id);
        department.setTenantId(tenantId);
        department.setHospitalId(hospitalId);
        department.setName("Dept-" + id.toString().substring(0, 8));
        department.setCode("D" + id.toString().substring(0, 6));
        department.setDepartmentType(DepartmentType.CLINICAL);
        department.setStatus(DepartmentStatus.ACTIVE);
        return department;
    }

    private StaffDepartmentAssignment openAssignment(
            final AssignmentAction action,
            final UUID department,
            final UUID from
    ) {
        final StaffDepartmentAssignment row = new StaffDepartmentAssignment();
        row.setId(UUID.randomUUID());
        row.setTenantId(tenantId);
        row.setHospitalId(hospitalId);
        row.setStaffType(StaffType.NURSE);
        row.setStaffId(staffId);
        row.setDepartmentId(department);
        row.setFromDepartmentId(from);
        row.setAction(action);
        row.setAssignedAt(java.time.Instant.now());
        row.setAssignedBy(actorId);
        return row;
    }

    private StaffAssignmentResponse sampleResponse(final StaffDepartmentAssignment row) {
        return new StaffAssignmentResponse(
                row.getId(),
                tenantId,
                hospitalId,
                row.getStaffType(),
                row.getStaffId(),
                row.getDepartmentId(),
                row.getFromDepartmentId(),
                row.getAction(),
                row.getReason(),
                row.getAssignedAt(),
                row.getEndedAt(),
                row.getAssignedBy(),
                row.getEndedBy(),
                row.isOpen(),
                null,
                null,
                0L
        );
    }
}
