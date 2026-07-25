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
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.organization.dto.request.CreateDoctorRequest;
import com.healthcare.hms.organization.dto.response.DoctorResponse;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.enums.EmploymentType;
import com.healthcare.hms.organization.mapper.StaffResponseMapper;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.organization.staff.StaffAdministrationSupport;
import com.healthcare.hms.organization.staff.StaffAssignmentHistoryWriter;
import com.healthcare.hms.organization.staff.StaffMembershipGuard;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.service.UserQueryService;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorServiceImpl")
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private HospitalQueryService hospitalQueryService;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private com.healthcare.hms.organization.service.DepartmentQueryService departmentQueryService;
    @Mock
    private StaffMembershipGuard membershipGuard;
    @Mock
    private StaffAssignmentHistoryWriter assignmentHistoryWriter;
    @Mock
    private StaffResponseMapper responseMapper;
    @Mock
    private AuditLogService auditLogService;

    private DoctorServiceImpl service;
    private UUID tenantId;
    private UUID userId;
    private UUID hospitalId;
    private UUID departmentId;
    private UUID doctorUserId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        doctorUserId = UUID.randomUUID();

        final StaffAdministrationSupport support = new StaffAdministrationSupport(
                userQueryService,
                departmentQueryService
        );
        service = new DoctorServiceImpl(
                doctorRepository,
                hospitalQueryService,
                support,
                membershipGuard,
                assignmentHistoryWriter,
                responseMapper,
                auditLogService
        );

        TenantContextHolder.set(new TenantContext(tenantId, "city", TenantType.HOSPITAL, TenantStatus.ACTIVE));
        final AuthenticatedUser principal = new AuthenticatedUser(
                userId,
                tenantId,
                "admin@city.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("DOCTOR_CREATE", "DOCTOR_READ", "DOCTOR_UPDATE", "DOCTOR_DELETE"),
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
    @DisplayName("create rejects duplicate employee code")
    void create_duplicateCode_throws() {
        final CreateDoctorRequest request = sampleCreate();
        when(hospitalQueryService.requireDefaultHospitalId()).thenReturn(hospitalId);
        when(doctorRepository.existsByTenantIdAndEmployeeCodeIgnoreCase(tenantId, "DOC-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request, "127.0.0.1", "junit"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Employee code");
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("create persists doctor and audits")
    void create_success() {
        final CreateDoctorRequest request = sampleCreate();
        final Doctor saved = new Doctor();
        saved.setId(UUID.randomUUID());
        saved.setTenantId(tenantId);
        saved.setEmployeeCode("DOC-001");

        when(hospitalQueryService.requireDefaultHospitalId()).thenReturn(hospitalId);
        when(membershipGuard.isEmployedElsewhereExcludingDoctor(doctorUserId)).thenReturn(false);
        when(doctorRepository.existsByTenantIdAndUserId(tenantId, doctorUserId)).thenReturn(false);
        when(doctorRepository.existsByTenantIdAndEmployeeCodeIgnoreCase(tenantId, "DOC-001")).thenReturn(false);
        when(doctorRepository.existsByTenantIdAndLicenseNumberIgnoreCase(tenantId, "LIC-9")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> {
            final Doctor d = inv.getArgument(0);
            d.setId(saved.getId());
            d.setTenantId(tenantId);
            return d;
        });
        when(responseMapper.toDoctorResponse(any(Doctor.class))).thenReturn(sampleResponse(saved.getId()));

        final DoctorResponse response = service.create(request, "127.0.0.1", "junit");

        assertThat(response.employeeCode()).isEqualTo("DOC-001");
        verify(userQueryService).requireTenantUserWithRole(tenantId, doctorUserId, RoleType.DOCTOR);
        verify(departmentQueryService).assertBelongsToTenant(departmentId, tenantId);
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("DOCTOR"),
                eq(saved.getId().toString()),
                eq(AuditAction.CREATE),
                isNull(),
                any(),
                eq("127.0.0.1"),
                eq("junit")
        );
    }

    private CreateDoctorRequest sampleCreate() {
        return new CreateDoctorRequest(
                doctorUserId,
                departmentId,
                "doc-001",
                "Consultant",
                EmploymentStatus.ACTIVE,
                EmploymentType.FULL_TIME,
                null,
                null,
                null,
                "Cardiology",
                "lic-9",
                "MD",
                10,
                new BigDecimal("50.00")
        );
    }

    private DoctorResponse sampleResponse(final UUID id) {
        return new DoctorResponse(
                id, tenantId, hospitalId, doctorUserId, departmentId, null,
                "DOC-001", "Consultant", EmploymentStatus.ACTIVE, EmploymentType.FULL_TIME,
                null, null, null, null, null, null, 0L,
                "Cardiology", "LIC-9", "MD", 10, new BigDecimal("50.00")
        );
    }
}
