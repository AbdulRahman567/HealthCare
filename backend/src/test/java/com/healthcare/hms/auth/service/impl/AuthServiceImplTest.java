package com.healthcare.hms.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.auth.dto.request.ChangePasswordRequest;
import com.healthcare.hms.auth.dto.request.ForgotPasswordRequest;
import com.healthcare.hms.auth.dto.request.LoginRequest;
import com.healthcare.hms.auth.dto.request.RefreshTokenRequest;
import com.healthcare.hms.auth.dto.request.RegisterAdminRequest;
import com.healthcare.hms.auth.dto.request.RegisterHospitalRequest;
import com.healthcare.hms.auth.dto.request.ResendVerificationRequest;
import com.healthcare.hms.auth.dto.request.ResetPasswordRequest;
import com.healthcare.hms.auth.dto.request.UpdateProfileRequest;
import com.healthcare.hms.auth.dto.request.VerifyEmailRequest;
import com.healthcare.hms.auth.dto.response.AuthResponse;
import com.healthcare.hms.auth.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.auth.dto.response.UserProfileResponse;
import com.healthcare.hms.auth.mapper.AuthMapper;
import com.healthcare.hms.auth.service.EmailVerificationEmailService;
import com.healthcare.hms.auth.service.EmailVerificationService;
import com.healthcare.hms.auth.service.PasswordResetEmailService;
import com.healthcare.hms.auth.service.PasswordResetService;
import com.healthcare.hms.auth.service.RefreshTokenService;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.common.exception.auth.AccountNotActiveException;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.InvalidCredentialsException;
import com.healthcare.hms.hospitals.entity.Tenant;
import com.healthcare.hms.hospitals.enums.SubscriptionPlan;
import com.healthcare.hms.hospitals.enums.TenantStatus;
import com.healthcare.hms.hospitals.repository.TenantRepository;
import com.healthcare.hms.security.jwt.JwtClaims;
import com.healthcare.hms.security.jwt.JwtProperties;
import com.healthcare.hms.security.jwt.JwtService;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.RoleRepository;
import com.healthcare.hms.users.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String USER_AGENT = "junit";

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private PasswordResetService passwordResetService;
    @Mock
    private PasswordResetEmailService passwordResetEmailService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private EmailVerificationEmailService emailVerificationEmailService;

    @InjectMocks
    private AuthServiceImpl authService;

    // ------------------------------------------------------------------
    // login
    // ------------------------------------------------------------------

    @Test
    @DisplayName("login succeeds for an active, verified user with matching credentials")
    void login_success() {
        final User user = AuthTestData.activeVerifiedUser("hashed-password");
        user.addRole(AuthTestData.hospitalAdminRole());
        final LoginRequest request = new LoginRequest(user.getEmail(), AuthTestData.STRONG_PASSWORD);

        when(userRepository.findByEmailWithRolesAndPermissions(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(AuthTestData.STRONG_PASSWORD, "hashed-password")).thenReturn(true);
        when(tenantRepository.findById(user.getTenantId())).thenReturn(Optional.of(AuthTestData.activeTenant()));
        when(refreshTokenService.issueRefreshToken(user, IP_ADDRESS, USER_AGENT)).thenReturn("raw-refresh-token");
        when(jwtService.generateAccessToken(any(JwtClaims.class))).thenReturn("access-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        final UserProfileResponse profile = sampleProfile(user);
        when(authMapper.toUserProfile(user)).thenReturn(profile);

        final AuthResponse response = authService.login(request, IP_ADDRESS, USER_AGENT);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(Duration.ofMinutes(15).toSeconds());
        assertThat(response.refreshExpiresInSeconds()).isEqualTo(Duration.ofDays(7).toSeconds());
        assertThat(response.user()).isSameAs(profile);
        verify(userRepository).save(user);
        verify(auditLogService).record(
                eq(user.getTenantId()), eq(user.getId()), eq("USER"), eq(user.getId().toString()),
                eq(AuditAction.LOGIN), isNull(), isNull(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    @DisplayName("login rejects unknown email")
    void login_invalidCredentials_userNotFound() {
        when(userRepository.findByEmailWithRolesAndPermissions("missing@hospital.test")).thenReturn(Optional.empty());
        final LoginRequest request = new LoginRequest("missing@hospital.test", AuthTestData.STRONG_PASSWORD);

        assertThatThrownBy(() -> authService.login(request, IP_ADDRESS, USER_AGENT))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login rejects wrong password")
    void login_invalidCredentials_wrongPassword() {
        final User user = AuthTestData.activeVerifiedUser("hashed-password");
        when(userRepository.findByEmailWithRolesAndPermissions(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1!ab", "hashed-password")).thenReturn(false);
        final LoginRequest request = new LoginRequest(user.getEmail(), "WrongPass1!ab");

        assertThatThrownBy(() -> authService.login(request, IP_ADDRESS, USER_AGENT))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login rejects inactive account")
    void login_inactiveAccount() {
        final User user = AuthTestData.activeVerifiedUser("hashed-password");
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmailWithRolesAndPermissions(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(AuthTestData.STRONG_PASSWORD, "hashed-password")).thenReturn(true);
        final LoginRequest request = new LoginRequest(user.getEmail(), AuthTestData.STRONG_PASSWORD);

        assertThatThrownBy(() -> authService.login(request, IP_ADDRESS, USER_AGENT))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    @DisplayName("login rejects unverified email")
    void login_unverifiedEmail() {
        final User user = AuthTestData.activeVerifiedUser("hashed-password");
        user.setEmailVerified(false);
        when(userRepository.findByEmailWithRolesAndPermissions(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(AuthTestData.STRONG_PASSWORD, "hashed-password")).thenReturn(true);
        final LoginRequest request = new LoginRequest(user.getEmail(), AuthTestData.STRONG_PASSWORD);

        assertThatThrownBy(() -> authService.login(request, IP_ADDRESS, USER_AGENT))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    // ------------------------------------------------------------------
    // registerHospital
    // ------------------------------------------------------------------

    @Test
    @DisplayName("registerHospital creates a pending tenant with a unique slug")
    void registerHospital_success() {
        final RegisterHospitalRequest request = new RegisterHospitalRequest(
                "Test Hospital", "hospital@hospital.test", "+1-555-0100", "123 Main St", SubscriptionPlan.PREMIUM);

        when(tenantRepository.existsByEmailIgnoreCase("hospital@hospital.test")).thenReturn(false);
        when(tenantRepository.existsBySlugIgnoreCase("test-hospital")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            final Tenant tenant = invocation.getArgument(0);
            tenant.setId(AuthTestData.tenantId());
            return tenant;
        });
        final HospitalRegistrationResponse expected = new HospitalRegistrationResponse(
                AuthTestData.tenantId(), "Test Hospital", "test-hospital", "hospital@hospital.test",
                "+1-555-0100", "123 Main St", SubscriptionPlan.PREMIUM, TenantStatus.PENDING, Instant.now());
        when(authMapper.toHospitalRegistration(any(Tenant.class))).thenReturn(expected);

        final HospitalRegistrationResponse response = authService.registerHospital(request, IP_ADDRESS, USER_AGENT);

        assertThat(response).isSameAs(expected);
        final ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        final Tenant saved = captor.getValue();
        assertThat(saved.getSlug()).isEqualTo("test-hospital");
        assertThat(saved.getStatus()).isEqualTo(TenantStatus.PENDING);
        assertThat(saved.getSubscriptionPlan()).isEqualTo(SubscriptionPlan.PREMIUM);
        verify(auditLogService).record(
                eq(AuthTestData.tenantId()), isNull(), eq("TENANT"), eq(AuthTestData.tenantId().toString()),
                eq(AuditAction.CREATE), isNull(), anyString(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    @DisplayName("registerHospital rejects duplicate hospital email")
    void registerHospital_duplicateEmail() {
        final RegisterHospitalRequest request = new RegisterHospitalRequest(
                "Test Hospital", "hospital@hospital.test", null, null, null);
        when(tenantRepository.existsByEmailIgnoreCase("hospital@hospital.test")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerHospital(request, IP_ADDRESS, USER_AGENT))
                .isInstanceOf(ConflictException.class);

        verify(tenantRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // registerInitialAdmin
    // ------------------------------------------------------------------

    @Test
    @DisplayName("registerInitialAdmin creates the admin and sends a verification email")
    void registerInitialAdmin_success() {
        final Tenant tenant = AuthTestData.activeTenant();
        final RegisterAdminRequest request = new RegisterAdminRequest(
                tenant.getId(), "Jane", "Admin", "new-admin@hospital.test", AuthTestData.STRONG_PASSWORD, "+1-555-0200");

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.countByTenantIdAndRoleType(tenant.getId(), RoleType.HOSPITAL_ADMIN)).thenReturn(0L);
        when(userRepository.existsByEmailIgnoreCase("new-admin@hospital.test")).thenReturn(false);
        when(roleRepository.findSystemRoleWithPermissions(RoleType.HOSPITAL_ADMIN))
                .thenReturn(Optional.of(AuthTestData.hospitalAdminRole()));
        when(passwordEncoder.encode(AuthTestData.STRONG_PASSWORD)).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            final User admin = invocation.getArgument(0);
            admin.setId(AuthTestData.userId());
            return admin;
        });
        when(emailVerificationService.issueVerificationToken(any(User.class), eq(IP_ADDRESS), eq(USER_AGENT)))
                .thenReturn("raw-verify-token");
        final UserProfileResponse expectedProfile = sampleProfile(AuthTestData.activeVerifiedUser("encoded-hash"));
        when(authMapper.toUserProfile(any(User.class))).thenReturn(expectedProfile);

        final UserProfileResponse response = authService.registerInitialAdmin(request, IP_ADDRESS, USER_AGENT);

        assertThat(response).isSameAs(expectedProfile);
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        final User savedAdmin = userCaptor.getValue();
        assertThat(savedAdmin.getEmail()).isEqualTo("new-admin@hospital.test");
        assertThat(savedAdmin.getPasswordHash()).isEqualTo("encoded-hash");
        assertThat(savedAdmin.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedAdmin.isEmailVerified()).isFalse();
        assertThat(savedAdmin.getRoles()).extracting(Role::getType).containsExactly(RoleType.HOSPITAL_ADMIN);

        verify(emailVerificationEmailService).sendVerificationLink(savedAdmin, "raw-verify-token");
        final ArgumentCaptor<AuditAction> actionCaptor = ArgumentCaptor.forClass(AuditAction.class);
        verify(auditLogService, times(2))
                .record(any(), any(), any(), any(), actionCaptor.capture(), any(), any(), any(), any());
        assertThat(actionCaptor.getAllValues())
                .containsExactly(AuditAction.EMAIL_VERIFICATION_REQUEST, AuditAction.CREATE);
    }

    @Test
    @DisplayName("registerInitialAdmin rejects a tenant that already has an admin")
    void registerInitialAdmin_conflictWhenAdminExists() {
        final Tenant tenant = AuthTestData.activeTenant();
        final RegisterAdminRequest request = new RegisterAdminRequest(
                tenant.getId(), "Jane", "Admin", "dup@hospital.test", AuthTestData.STRONG_PASSWORD, null);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.countByTenantIdAndRoleType(tenant.getId(), RoleType.HOSPITAL_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> authService.registerInitialAdmin(request, IP_ADDRESS, USER_AGENT))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verify(emailVerificationService, never()).issueVerificationToken(any(), any(), any());
    }

    @Test
    @DisplayName("registerInitialAdmin rejects a duplicate email")
    void registerInitialAdmin_conflictWhenEmailExists() {
        final Tenant tenant = AuthTestData.activeTenant();
        final RegisterAdminRequest request = new RegisterAdminRequest(
                tenant.getId(), "Jane", "Admin", "dup@hospital.test", AuthTestData.STRONG_PASSWORD, null);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.countByTenantIdAndRoleType(tenant.getId(), RoleType.HOSPITAL_ADMIN)).thenReturn(0L);
        when(userRepository.existsByEmailIgnoreCase("dup@hospital.test")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerInitialAdmin(request, IP_ADDRESS, USER_AGENT))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // changePassword
    // ------------------------------------------------------------------

    @Test
    @DisplayName("changePassword rotates the hash and revokes sessions")
    void changePassword_success() {
        final User user = AuthTestData.activeVerifiedUser("current-hash");
        final AuthenticatedUser currentUser = toAuthenticatedUser(user);
        final ChangePasswordRequest request = new ChangePasswordRequest("OldPass1!ab", "NewPass2!cd");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!ab", "current-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPass2!cd", "current-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPass2!cd")).thenReturn("new-hash");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUser).thenReturn(currentUser);
            authService.changePassword(request, IP_ADDRESS, USER_AGENT);
        }

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getTokenVersion()).isEqualTo(1L);
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllForUser(user.getId());
        verify(auditLogService).record(
                eq(user.getTenantId()), eq(user.getId()), eq("USER"), eq(user.getId().toString()),
                eq(AuditAction.PASSWORD_CHANGE), isNull(), isNull(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    @DisplayName("changePassword rejects an incorrect current password")
    void changePassword_wrongCurrentPassword() {
        final User user = AuthTestData.activeVerifiedUser("current-hash");
        final AuthenticatedUser currentUser = toAuthenticatedUser(user);
        final ChangePasswordRequest request = new ChangePasswordRequest("WrongPass1!ab", "NewPass2!cd");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1!ab", "current-hash")).thenReturn(false);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUser).thenReturn(currentUser);

            assertThatThrownBy(() -> authService.changePassword(request, IP_ADDRESS, USER_AGENT))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword rejects reuse of the current password")
    void changePassword_reuseNewPassword() {
        final User user = AuthTestData.activeVerifiedUser("current-hash");
        final AuthenticatedUser currentUser = toAuthenticatedUser(user);
        final ChangePasswordRequest request = new ChangePasswordRequest("SamePass1!ab", "SamePass1!ab");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SamePass1!ab", "current-hash")).thenReturn(true);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUser).thenReturn(currentUser);

            assertThatThrownBy(() -> authService.changePassword(request, IP_ADDRESS, USER_AGENT))
                    .isInstanceOf(BusinessException.class);
        }

        verify(userRepository, never()).save(any());
        verify(refreshTokenService, never()).revokeAllForUser(any());
    }

    // ------------------------------------------------------------------
    // forgotPassword
    // ------------------------------------------------------------------

    @Test
    @DisplayName("forgotPassword issues and sends a reset link for a known active email")
    void forgotPassword_knownActiveEmail_sendsResetLink() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetService.issueResetToken(user, IP_ADDRESS, USER_AGENT)).thenReturn("raw-reset-token");

        authService.forgotPassword(new ForgotPasswordRequest(user.getEmail()), IP_ADDRESS, USER_AGENT);

        verify(passwordResetEmailService).sendResetLink(user, "raw-reset-token");
        verify(auditLogService).record(
                eq(user.getTenantId()), eq(user.getId()), eq("USER"), eq(user.getId().toString()),
                eq(AuditAction.PASSWORD_RESET_REQUEST), isNull(), isNull(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    @DisplayName("forgotPassword silently ignores an unknown email")
    void forgotPassword_unknownEmail_doesNothing() {
        when(userRepository.findByEmailIgnoreCase("missing@hospital.test")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("missing@hospital.test"), IP_ADDRESS, USER_AGENT);

        verifyNoInteractions(passwordResetService, passwordResetEmailService, auditLogService);
    }

    @Test
    @DisplayName("forgotPassword skips inactive accounts")
    void forgotPassword_inactiveAccount_skipsSend() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        authService.forgotPassword(new ForgotPasswordRequest(user.getEmail()), IP_ADDRESS, USER_AGENT);

        verifyNoInteractions(passwordResetService, passwordResetEmailService, auditLogService);
    }

    // ------------------------------------------------------------------
    // resetPassword
    // ------------------------------------------------------------------

    @Test
    @DisplayName("resetPassword rotates the hash, consumes the token, and revokes sessions")
    void resetPassword_success() {
        final User user = AuthTestData.activeVerifiedUser("old-hash");
        final ResetPasswordRequest request = new ResetPasswordRequest("raw-reset-token-1234567890123456", "NewPass9!zz");

        when(passwordResetService.requireValidResetToken(request.token())).thenReturn(user);
        when(passwordEncoder.matches("NewPass9!zz", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPass9!zz")).thenReturn("hashed-new");

        authService.resetPassword(request, IP_ADDRESS, USER_AGENT);

        assertThat(user.getPasswordHash()).isEqualTo("hashed-new");
        assertThat(user.getTokenVersion()).isEqualTo(1L);
        verify(userRepository).save(user);
        verify(passwordResetService).markTokenUsed(request.token());
        verify(refreshTokenService).revokeAllForUser(user.getId());
        verify(auditLogService).record(
                eq(user.getTenantId()), eq(user.getId()), eq("USER"), eq(user.getId().toString()),
                eq(AuditAction.PASSWORD_RESET), isNull(), isNull(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    @DisplayName("resetPassword rejects reuse of the current password")
    void resetPassword_reuseNewPassword() {
        final User user = AuthTestData.activeVerifiedUser("old-hash");
        final ResetPasswordRequest request = new ResetPasswordRequest("raw-reset-token-1234567890123456", "OldPass1!ab");

        when(passwordResetService.requireValidResetToken(request.token())).thenReturn(user);
        when(passwordEncoder.matches("OldPass1!ab", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.resetPassword(request, IP_ADDRESS, USER_AGENT))
                .isInstanceOf(BusinessException.class);

        verify(passwordResetService, never()).markTokenUsed(any());
        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // verifyEmail
    // ------------------------------------------------------------------

    @Test
    @DisplayName("verifyEmail marks the user verified and activates a pending account")
    void verifyEmail_success() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setEmailVerified(false);
        user.setStatus(UserStatus.PENDING);
        final VerifyEmailRequest request = new VerifyEmailRequest("verify-raw-token-1234567890123456");

        when(emailVerificationService.requireValidVerificationToken(request.token())).thenReturn(user);

        authService.verifyEmail(request, IP_ADDRESS, USER_AGENT);

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(user);
        verify(emailVerificationService).markTokenUsed(request.token());
        verify(auditLogService).record(
                eq(user.getTenantId()), eq(user.getId()), eq("USER"), eq(user.getId().toString()),
                eq(AuditAction.EMAIL_VERIFIED), isNull(), isNull(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    // ------------------------------------------------------------------
    // resendVerification
    // ------------------------------------------------------------------

    @Test
    @DisplayName("resendVerification issues a new token for a known unverified email")
    void resendVerification_knownUnverifiedEmail() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setEmailVerified(false);
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(emailVerificationService.issueVerificationToken(user, IP_ADDRESS, USER_AGENT))
                .thenReturn("raw-verify-token");

        authService.resendVerification(new ResendVerificationRequest(user.getEmail()), IP_ADDRESS, USER_AGENT);

        verify(emailVerificationEmailService).sendVerificationLink(user, "raw-verify-token");
        verify(auditLogService).record(
                eq(user.getTenantId()), eq(user.getId()), eq("USER"), eq(user.getId().toString()),
                eq(AuditAction.EMAIL_VERIFICATION_REQUEST), isNull(), isNull(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    @DisplayName("resendVerification skips an already-verified email")
    void resendVerification_alreadyVerified_doesNothing() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        authService.resendVerification(new ResendVerificationRequest(user.getEmail()), IP_ADDRESS, USER_AGENT);

        verifyNoInteractions(emailVerificationService, emailVerificationEmailService, auditLogService);
    }

    // ------------------------------------------------------------------
    // updateProfile / getCurrentUser
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateProfile updates and persists profile fields")
    void updateProfile_success() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        final AuthenticatedUser currentUser = toAuthenticatedUser(user);
        final UpdateProfileRequest request = new UpdateProfileRequest("NewFirst", "NewLast", "+1-555-9999");

        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        final UserProfileResponse expected = sampleProfile(user);
        when(authMapper.toUserProfile(user)).thenReturn(expected);

        final UserProfileResponse response;
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUser).thenReturn(currentUser);
            response = authService.updateProfile(request, IP_ADDRESS, USER_AGENT);
        }

        assertThat(response).isSameAs(expected);
        assertThat(user.getFirstName()).isEqualTo("NewFirst");
        assertThat(user.getLastName()).isEqualTo("NewLast");
        assertThat(user.getPhone()).isEqualTo("+1-555-9999");
        verify(userRepository).save(user);
        verify(auditLogService).record(
                eq(user.getTenantId()), eq(user.getId()), eq("USER"), eq(user.getId().toString()),
                eq(AuditAction.UPDATE), anyString(), anyString(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    @DisplayName("getCurrentUser resolves the profile for the authenticated principal")
    void getCurrentUser_success() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        final AuthenticatedUser currentUser = toAuthenticatedUser(user);

        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.of(user));
        final UserProfileResponse expected = sampleProfile(user);
        when(authMapper.toUserProfile(user)).thenReturn(expected);

        final UserProfileResponse response;
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUser).thenReturn(currentUser);
            response = authService.getCurrentUser();
        }

        assertThat(response).isSameAs(expected);
    }

    @Test
    @DisplayName("getCurrentUser rejects a principal whose user record has been removed")
    void getCurrentUser_missingUser() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        final AuthenticatedUser currentUser = toAuthenticatedUser(user);

        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUser).thenReturn(currentUser);

            assertThatThrownBy(authService::getCurrentUser).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ------------------------------------------------------------------
    // logout
    // ------------------------------------------------------------------

    @Test
    @DisplayName("logout revokes refresh tokens, bumps tokenVersion, and clears the security context")
    void logout_revokesTokensAndClearsContext() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        final AuthenticatedUser currentUser = toAuthenticatedUser(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUser).thenReturn(currentUser);
            authService.logout("raw-refresh-token", IP_ADDRESS, USER_AGENT);
        }

        verify(refreshTokenService).revokeToken("raw-refresh-token");
        verify(refreshTokenService).revokeAllForUser(user.getId());
        verify(userRepository).save(user);
        assertThat(user.getTokenVersion()).isEqualTo(1L);
        verify(auditLogService).record(
                eq(user.getTenantId()), eq(user.getId()), eq("USER"), eq(user.getId().toString()),
                eq(AuditAction.LOGOUT), isNull(), isNull(), eq(IP_ADDRESS), eq(USER_AGENT));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("logout skips revoking a blank refresh token")
    void logout_withBlankRefreshToken_skipsRevokeToken() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        final AuthenticatedUser currentUser = toAuthenticatedUser(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::requireCurrentUser).thenReturn(currentUser);
            authService.logout(" ", IP_ADDRESS, USER_AGENT);
        }

        verify(refreshTokenService, never()).revokeToken(anyString());
        verify(refreshTokenService).revokeAllForUser(user.getId());
        verify(userRepository).save(user);
    }

    // ------------------------------------------------------------------
    // refreshToken
    // ------------------------------------------------------------------

    @Test
    @DisplayName("refreshToken delegates rotation to RefreshTokenService")
    void refreshToken_delegatesToRefreshTokenService() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.addRole(AuthTestData.hospitalAdminRole());
        final RefreshTokenRequest request = new RefreshTokenRequest("old-raw-refresh-token");
        final RefreshTokenService.RotatedTokens rotated =
                new RefreshTokenService.RotatedTokens(user, "new-raw-refresh-token");

        when(refreshTokenService.rotate("old-raw-refresh-token", IP_ADDRESS, USER_AGENT)).thenReturn(rotated);
        when(jwtService.generateAccessToken(any(JwtClaims.class))).thenReturn("new-access-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        final UserProfileResponse profile = sampleProfile(user);
        when(authMapper.toUserProfile(user)).thenReturn(profile);

        final AuthResponse response = authService.refreshToken(request, IP_ADDRESS, USER_AGENT);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-raw-refresh-token");
        assertThat(response.user()).isSameAs(profile);
        verify(refreshTokenService).rotate("old-raw-refresh-token", IP_ADDRESS, USER_AGENT);
        verify(refreshTokenService, never()).issueRefreshToken(any(), any(), any());
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static AuthenticatedUser toAuthenticatedUser(final User user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                Set.of(),
                Set.of(),
                user.getTokenVersion());
    }

    private static UserProfileResponse sampleProfile(final User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getTenantId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.isEmailVerified(),
                user.getEmailVerifiedAt(),
                user.getStatus(),
                Set.of(),
                Set.of(),
                user.getLastLoginAt(),
                user.getCreatedAt());
    }
}
