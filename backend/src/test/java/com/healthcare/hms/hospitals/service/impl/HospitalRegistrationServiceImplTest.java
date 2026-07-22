package com.healthcare.hms.hospitals.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.auth.service.EmailVerificationEmailService;
import com.healthcare.hms.auth.service.EmailVerificationService;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.hospitals.bootstrap.TenantRoleProvisioner;
import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.enums.HospitalStatus;
import com.healthcare.hms.hospitals.mapper.HospitalRegistrationMapper;
import com.healthcare.hms.hospitals.repository.HospitalRepository;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.repository.TenantRepository;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("HospitalRegistrationServiceImpl")
class HospitalRegistrationServiceImplTest {

    private static final String IP = "127.0.0.1";
    private static final String UA = "junit";

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantRoleProvisioner tenantRoleProvisioner;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private HospitalRegistrationMapper hospitalRegistrationMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private EmailVerificationEmailService emailVerificationEmailService;

    @InjectMocks
    private HospitalRegistrationServiceImpl service;

    @Test
    @DisplayName("register atomically creates tenant, hospital, roles, and admin")
    void register_success() {
        final HospitalRegistrationRequest request = sampleRequest();
        final Tenant savedTenant = AuthTestData.activeTenant();
        savedTenant.setStatus(TenantStatus.PENDING);
        savedTenant.setCreatedAt(Instant.now());

        final Hospital savedHospital = new Hospital();
        savedHospital.setId(UUID.randomUUID());
        savedHospital.setTenantId(savedTenant.getId());
        savedHospital.setName(request.hospitalName());
        savedHospital.setCode("DEFAULT");
        savedHospital.setEmail(request.hospitalEmail());
        savedHospital.setDefaultHospital(true);
        savedHospital.setStatus(HospitalStatus.PENDING);

        final Role adminRole = AuthTestData.hospitalAdminRole();
        adminRole.setTenantId(savedTenant.getId());
        adminRole.setSystemRole(false);

        final User savedAdmin = AuthTestData.activeVerifiedUser("hash");
        savedAdmin.setEmailVerified(false);
        savedAdmin.setEmail(request.adminEmail());

        final HospitalRegistrationResponse expected = new HospitalRegistrationResponse(
                savedTenant.getId(),
                savedTenant.getSlug(),
                TenantStatus.PENDING,
                savedHospital.getId(),
                savedHospital.getName(),
                savedHospital.getCode(),
                HospitalStatus.PENDING,
                true,
                savedHospital.getEmail(),
                null,
                null,
                SubscriptionPlan.BASIC,
                savedAdmin.getId(),
                savedAdmin.getEmail(),
                false,
                List.of("HOSPITAL_ADMIN", "DOCTOR"),
                savedTenant.getCreatedAt()
        );

        when(tenantRepository.existsByEmailIgnoreCase(request.hospitalEmail())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(request.adminEmail())).thenReturn(false);
        when(tenantRepository.existsBySlugIgnoreCase(anyString())).thenReturn(false);
        when(tenantRepository.saveAndFlush(any(Tenant.class))).thenAnswer(invocation -> {
            final Tenant tenant = invocation.getArgument(0);
            tenant.setId(savedTenant.getId());
            tenant.setCreatedAt(savedTenant.getCreatedAt());
            return tenant;
        });
        when(hospitalRepository.saveAndFlush(any(Hospital.class))).thenAnswer(invocation -> {
            final Hospital hospital = invocation.getArgument(0);
            hospital.setId(savedHospital.getId());
            return hospital;
        });
        when(tenantRoleProvisioner.provisionDefaultRoles(savedTenant.getId()))
                .thenReturn(List.of(adminRole));
        when(passwordEncoder.encode(request.adminPassword())).thenReturn("hash");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            user.setId(savedAdmin.getId());
            return user;
        });
        when(emailVerificationService.issueVerificationToken(any(User.class), eq(IP), eq(UA)))
                .thenReturn("raw-token");
        when(hospitalRegistrationMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(expected);

        final HospitalRegistrationResponse response = service.register(request, IP, UA);

        assertThat(response).isEqualTo(expected);
        verify(tenantRoleProvisioner).provisionDefaultRoles(savedTenant.getId());
        verify(emailVerificationEmailService).sendVerificationLink(any(User.class), eq("raw-token"));
        verify(auditLogService, org.mockito.Mockito.atLeast(3))
                .record(any(), any(), anyString(), anyString(), eq(AuditAction.CREATE), isNull(), anyString(), eq(IP), eq(UA));

        final ArgumentCaptor<Hospital> hospitalCaptor = ArgumentCaptor.forClass(Hospital.class);
        verify(hospitalRepository).saveAndFlush(hospitalCaptor.capture());
        assertThat(hospitalCaptor.getValue().isDefaultHospital()).isTrue();
        assertThat(hospitalCaptor.getValue().getCode()).isEqualTo("DEFAULT");
    }

    @Test
    @DisplayName("register rejects duplicate hospital email")
    void register_duplicateHospitalEmail() {
        final HospitalRegistrationRequest request = sampleRequest();
        when(tenantRepository.existsByEmailIgnoreCase(request.hospitalEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.register(request, IP, UA))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Hospital email");

        verify(hospitalRepository, never()).saveAndFlush(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("register rejects duplicate administrator email")
    void register_duplicateAdminEmail() {
        final HospitalRegistrationRequest request = sampleRequest();
        when(tenantRepository.existsByEmailIgnoreCase(request.hospitalEmail())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(request.adminEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.register(request, IP, UA))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Administrator email");

        verify(tenantRepository, never()).saveAndFlush(any());
    }

    private static HospitalRegistrationRequest sampleRequest() {
        return new HospitalRegistrationRequest(
                "City General Hospital",
                "hospital@city.test",
                "+1-555-0100",
                "100 Main St",
                SubscriptionPlan.BASIC,
                "Jane",
                "Admin",
                "admin@city.test",
                AuthTestData.STRONG_PASSWORD,
                "+1-555-0101"
        );
    }
}
