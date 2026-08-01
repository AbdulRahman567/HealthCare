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
import com.healthcare.hms.common.api.PageResponse;
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
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentServiceImpl")
class DepartmentServiceImplTest {

    private static final String IP = "127.0.0.1";
    private static final String UA = "junit";

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private HospitalQueryService hospitalQueryService;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private com.healthcare.hms.organization.staff.StaffMembershipGuard staffMembershipGuard;

    @InjectMocks
    private DepartmentServiceImpl service;

    private UUID tenantId;
    private UUID userId;
    private UUID hospitalId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();

        TenantContextHolder.set(new TenantContext(
                tenantId,
                "city-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        final AuthenticatedUser principal = new AuthenticatedUser(
                userId,
                tenantId,
                "admin@city.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of(
                        "DEPARTMENT_READ",
                        "DEPARTMENT_CREATE",
                        "DEPARTMENT_UPDATE",
                        "DEPARTMENT_DELETE"
                ),
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
    @DisplayName("create rejects duplicate department code")
    void create_duplicateCode_throwsConflict() {
        final CreateDepartmentRequest request = new CreateDepartmentRequest(
                "Cardiology",
                "cardio",
                "Heart care",
                DepartmentType.CLINICAL,
                DepartmentStatus.ACTIVE,
                "Building A",
                null
        );
        when(hospitalQueryService.requireDefaultHospitalId()).thenReturn(hospitalId);
        when(departmentRepository.existsByTenantIdAndCodeIgnoreCase(tenantId, "CARDIO")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request, IP, UA))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("code");
        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create persists department and audits")
    void create_success() {
        final CreateDepartmentRequest request = new CreateDepartmentRequest(
                "Cardiology",
                "cardio",
                "Heart care",
                DepartmentType.CLINICAL,
                DepartmentStatus.ACTIVE,
                "Building A",
                null
        );
        final Department saved = sampleDepartment();
        final DepartmentResponse response = sampleResponse(saved);

        when(hospitalQueryService.requireDefaultHospitalId()).thenReturn(hospitalId);
        when(departmentRepository.existsByTenantIdAndCodeIgnoreCase(tenantId, "CARDIO")).thenReturn(false);
        when(departmentRepository.existsByTenantIdAndNameIgnoreCase(tenantId, "Cardiology")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            final Department dept = invocation.getArgument(0);
            dept.setId(saved.getId());
            dept.setTenantId(tenantId);
            return dept;
        });
        when(departmentMapper.toResponse(any(Department.class))).thenReturn(response);

        final DepartmentResponse result = service.create(request, IP, UA);

        assertThat(result.code()).isEqualTo("CARDIO");
        verify(departmentMapper).applyCreate(eq(request), any(Department.class));
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("DEPARTMENT"),
                eq(saved.getId().toString()),
                eq(AuditAction.CREATE),
                isNull(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    @Test
    @DisplayName("getById throws when department missing in tenant")
    void getById_missing_throwsNotFound() {
        final UUID id = UUID.randomUUID();
        when(departmentRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("search returns page response")
    void search_returnsPage() {
        final Department department = sampleDepartment();
        final DepartmentResponse response = sampleResponse(department);
        final Page<Department> page = new PageImpl<>(List.of(department), PageRequest.of(0, 20), 1);

        when(departmentRepository.findAll(
                        org.mockito.ArgumentMatchers.<Specification<Department>>any(),
                        any(Pageable.class)))
                .thenReturn(page);
        when(departmentMapper.toResponse(department)).thenReturn(response);

        final PageResponse<DepartmentResponse> result = service.search(
                "card",
                DepartmentStatus.ACTIVE,
                DepartmentType.CLINICAL,
                hospitalId,
                PageRequest.of(0, 20, Sort.by("name"))
        );

        assertThat(result.content()).containsExactly(response);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("delete soft-deletes and frees unique code/name")
    void delete_softDeletesAndSuffixesIdentity() {
        final Department department = sampleDepartment();
        when(departmentRepository.findByIdAndTenantId(department.getId(), tenantId))
                .thenReturn(Optional.of(department));
        when(staffMembershipGuard.hasAffiliatedStaff(department.getId())).thenReturn(false);
        when(departmentRepository.save(department)).thenReturn(department);

        service.delete(department.getId(), IP, UA);

        assertThat(department.isDeleted()).isTrue();
        assertThat(department.getCode()).contains("__DEL__");
        assertThat(department.getName()).contains("__DEL__");
        assertThat(department.getHeadUserId()).isNull();
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("DEPARTMENT"),
                eq(department.getId().toString()),
                eq(AuditAction.DELETE),
                any(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    @Test
    @DisplayName("update rejects duplicate name")
    void update_duplicateName_throwsConflict() {
        final Department department = sampleDepartment();
        final UpdateDepartmentRequest request = new UpdateDepartmentRequest(
                "Neurology",
                "CARDIO",
                null,
                DepartmentType.CLINICAL,
                DepartmentStatus.ACTIVE,
                null,
                null
        );
        when(departmentRepository.findByIdAndTenantId(department.getId(), tenantId))
                .thenReturn(Optional.of(department));
        when(departmentRepository.existsByTenantIdAndCodeIgnoreCaseAndIdNot(tenantId, "CARDIO", department.getId()))
                .thenReturn(false);
        when(departmentRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(tenantId, "Neurology", department.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.update(department.getId(), request, IP, UA))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("name");
    }

    private Department sampleDepartment() {
        final Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setTenantId(tenantId);
        department.setHospitalId(hospitalId);
        department.setName("Cardiology");
        department.setCode("CARDIO");
        department.setDepartmentType(DepartmentType.CLINICAL);
        department.setStatus(DepartmentStatus.ACTIVE);
        return department;
    }

    private static DepartmentResponse sampleResponse(final Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getTenantId(),
                department.getHospitalId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getDepartmentType(),
                department.getStatus(),
                department.getLocation(),
                department.getHeadUserId(),
                department.getHeadStaffId(),
                department.getHeadStaffType(),
                null,
                null,
                null,
                null,
                0L
        );
    }
}
