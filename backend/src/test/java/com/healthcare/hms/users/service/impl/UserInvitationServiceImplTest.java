package com.healthcare.hms.users.service.impl;

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
import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.tenant.service.TenantAccessService;
import com.healthcare.hms.users.config.UserInvitationProperties;
import com.healthcare.hms.users.dto.request.AcceptInvitationRequest;
import com.healthcare.hms.users.dto.request.CreateInvitationRequest;
import com.healthcare.hms.users.dto.response.AcceptInvitationResponse;
import com.healthcare.hms.users.dto.response.UserInvitationResponse;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.entity.UserInvitation;
import com.healthcare.hms.users.enums.InvitationStatus;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.mapper.UserInvitationMapper;
import com.healthcare.hms.users.repository.RoleRepository;
import com.healthcare.hms.users.repository.UserInvitationRepository;
import com.healthcare.hms.users.repository.UserRepository;
import com.healthcare.hms.users.service.InvitationEmailService;
import java.time.Duration;
import java.time.Instant;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserInvitationServiceImpl")
class UserInvitationServiceImplTest {

    @Mock
    private UserInvitationRepository invitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private HospitalQueryService hospitalQueryService;
    @Mock
    private TenantAccessService tenantAccessService;
    @Mock
    private TokenHashingService tokenHashingService;
    @Mock
    private InvitationEmailService invitationEmailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserInvitationMapper invitationMapper;
    @Mock
    private AuditLogService auditLogService;

    private UserInvitationServiceImpl service;
    private UserInvitationProperties properties;
    private UUID tenantId;
    private UUID actorId;
    private UUID hospitalId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        properties = new UserInvitationProperties();
        properties.setTokenExpiration(Duration.ofHours(72));
        properties.setFrontendBaseUrl("http://localhost:3000");

        service = new UserInvitationServiceImpl(
                invitationRepository,
                userRepository,
                roleRepository,
                hospitalQueryService,
                tenantAccessService,
                tokenHashingService,
                properties,
                invitationEmailService,
                passwordEncoder,
                invitationMapper,
                auditLogService
        );

        TenantContextHolder.set(new TenantContext(tenantId, "city", TenantType.HOSPITAL, TenantStatus.ACTIVE));
        final AuthenticatedUser principal = new AuthenticatedUser(
                actorId,
                tenantId,
                "admin@city.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("USER_CREATE", "USER_READ", "USER_UPDATE"),
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
    @DisplayName("invite creates pending invitation and sends email")
    void invite_success() {
        final Hospital hospital = new Hospital();
        hospital.setId(hospitalId);
        hospital.setName("City Hospital");
        final Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setType(RoleType.DOCTOR);
        final UserInvitationResponse mapped = sampleResponse(InvitationStatus.PENDING);

        when(userRepository.existsByEmailIgnoreCase("doc@city.test")).thenReturn(false);
        when(invitationRepository.findStalePendingByTenantId(eq(tenantId), any()))
                .thenReturn(java.util.List.of());
        when(invitationRepository.existsActivePendingByTenantIdAndEmailIgnoreCase(
                eq(tenantId), eq("doc@city.test"), any())).thenReturn(false);
        when(hospitalQueryService.requireDefaultHospital()).thenReturn(hospital);
        when(roleRepository.findByTenantIdAndType(tenantId, RoleType.DOCTOR)).thenReturn(Optional.of(role));
        when(tokenHashingService.generateRawToken()).thenReturn("raw-token-value-0123456789abcdef0123456789");
        when(tokenHashingService.hash("raw-token-value-0123456789abcdef0123456789")).thenReturn("hash-1");
        when(invitationRepository.save(any())).thenAnswer(inv -> {
            final UserInvitation row = inv.getArgument(0);
            row.setId(UUID.randomUUID());
            row.setTenantId(tenantId);
            return row;
        });
        when(invitationMapper.toResponse(any())).thenReturn(mapped);

        final UserInvitationResponse result = service.invite(
                new CreateInvitationRequest("Doc@City.test", "Ada", "Lovelace", RoleType.DOCTOR, "Welcome"),
                "127.0.0.1",
                "junit"
        );

        assertThat(result.status()).isEqualTo(InvitationStatus.PENDING);
        verify(invitationEmailService).sendInvitation(any(), eq("raw-token-value-0123456789abcdef0123456789"), eq("City Hospital"));
        verify(auditLogService).record(
                eq(tenantId),
                eq(actorId),
                eq("USER_INVITATION"),
                any(),
                eq(AuditAction.INVITATION_SENT),
                isNull(),
                any(),
                eq("127.0.0.1"),
                eq("junit")
        );
    }

    @Test
    @DisplayName("invite rejects duplicate pending invitation")
    void invite_duplicatePending_throws() {
        when(userRepository.existsByEmailIgnoreCase("doc@city.test")).thenReturn(false);
        when(invitationRepository.findStalePendingByTenantId(eq(tenantId), any()))
                .thenReturn(java.util.List.of());
        when(invitationRepository.existsActivePendingByTenantIdAndEmailIgnoreCase(
                eq(tenantId), eq("doc@city.test"), any())).thenReturn(true);

        assertThatThrownBy(() -> service.invite(
                new CreateInvitationRequest("doc@city.test", null, null, RoleType.NURSE, null),
                "127.0.0.1",
                "junit"
        )).isInstanceOf(ConflictException.class);

        verify(invitationRepository, never()).save(any());
    }

    @Test
    @DisplayName("accept creates user, assigns role, and joins tenant")
    void accept_success() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();

        final UUID invitationId = UUID.randomUUID();
        final UserInvitation invitation = pendingInvitation(invitationId);
        final Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setType(RoleType.NURSE);

        when(tokenHashingService.hash("accept-token-0123456789abcdef0123456789abcd")).thenReturn("hash-accept");
        when(invitationRepository.findByTokenHash("hash-accept")).thenReturn(Optional.of(invitation));
        when(tenantAccessService.requireActiveTenant(tenantId)).thenReturn(null);
        when(userRepository.existsByEmailIgnoreCase("nurse@city.test")).thenReturn(false);
        when(roleRepository.findByTenantIdAndType(tenantId, RoleType.NURSE)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Str0ng!Passw0rd")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> {
            final User user = inv.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(invitationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final AcceptInvitationResponse result = service.accept(
                new AcceptInvitationRequest(
                        "accept-token-0123456789abcdef0123456789abcd",
                        "Florence",
                        "Nightingale",
                        "Str0ng!Passw0rd",
                        null
                ),
                "127.0.0.1",
                "junit"
        );

        assertThat(result.email()).isEqualTo("nurse@city.test");
        assertThat(result.roleType()).isEqualTo(RoleType.NURSE);
        assertThat(result.tenantId()).isEqualTo(tenantId);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getTenantId()).isEqualTo(tenantId);
        assertThat(userCaptor.getValue().isEmailVerified()).isTrue();
        assertThat(userCaptor.getValue().getRoles()).contains(role);
        verify(auditLogService).record(
                eq(tenantId),
                any(),
                eq("USER_INVITATION"),
                eq(invitationId.toString()),
                eq(AuditAction.INVITATION_ACCEPTED),
                any(),
                any(),
                eq("127.0.0.1"),
                eq("junit")
        );
    }

    @Test
    @DisplayName("accept rejects expired invitation")
    void accept_expired_throws() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();

        final UserInvitation invitation = pendingInvitation(UUID.randomUUID());
        invitation.setExpiresAt(Instant.now().minusSeconds(60));

        when(tokenHashingService.hash("expired-token-0123456789abcdef0123456789ab")).thenReturn("hash-expired");
        when(invitationRepository.findByTokenHash("hash-expired")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.accept(
                new AcceptInvitationRequest(
                        "expired-token-0123456789abcdef0123456789ab",
                        "A",
                        "B",
                        "Str0ng!Passw0rd",
                        null
                ),
                "127.0.0.1",
                "junit"
        )).isInstanceOf(ExpiredTokenException.class);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
        verify(invitationRepository).save(invitation);
        verify(userRepository, never()).save(any());
    }

    private UserInvitation pendingInvitation(final UUID id) {
        final UserInvitation invitation = new UserInvitation();
        invitation.setId(id);
        invitation.setTenantId(tenantId);
        invitation.setHospitalId(hospitalId);
        invitation.setEmail("nurse@city.test");
        invitation.setRoleType(RoleType.NURSE);
        invitation.setInvitedBy(actorId);
        invitation.setTokenHash("hash-accept");
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().plus(Duration.ofHours(24)));
        return invitation;
    }

    private UserInvitationResponse sampleResponse(final InvitationStatus status) {
        return new UserInvitationResponse(
                UUID.randomUUID(),
                tenantId,
                hospitalId,
                "doc@city.test",
                "Ada",
                "Lovelace",
                RoleType.DOCTOR,
                actorId,
                status,
                Instant.now().plus(Duration.ofHours(72)),
                null,
                null,
                null,
                null,
                "Welcome",
                false,
                Instant.now(),
                Instant.now(),
                0L
        );
    }
}
