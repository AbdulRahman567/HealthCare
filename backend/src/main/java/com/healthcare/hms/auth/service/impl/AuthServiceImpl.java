package com.healthcare.hms.auth.service.impl;

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
import com.healthcare.hms.auth.service.AuthService;
import com.healthcare.hms.auth.service.EmailVerificationEmailService;
import com.healthcare.hms.auth.service.EmailVerificationService;
import com.healthcare.hms.auth.service.PasswordResetEmailService;
import com.healthcare.hms.auth.service.PasswordResetService;
import com.healthcare.hms.auth.service.RefreshTokenService;
import com.healthcare.hms.common.email.EmailDeliveryException;
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
import com.healthcare.hms.security.jwt.JwtTokenType;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.RoleRepository;
import com.healthcare.hms.users.repository.UserRepository;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication flows including refresh-token issuance and rotation.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String ENTITY_USER = "USER";
    private static final String ENTITY_TENANT = "TENANT";
    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthMapper authMapper;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final PasswordResetEmailService passwordResetEmailService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationEmailService emailVerificationEmailService;

    public AuthServiceImpl(
            final UserRepository userRepository,
            final RoleRepository roleRepository,
            final TenantRepository tenantRepository,
            final PasswordEncoder passwordEncoder,
            final JwtService jwtService,
            final JwtProperties jwtProperties,
            final AuthMapper authMapper,
            final AuditLogService auditLogService,
            final RefreshTokenService refreshTokenService,
            final PasswordResetService passwordResetService,
            final PasswordResetEmailService passwordResetEmailService,
            final EmailVerificationService emailVerificationService,
            final EmailVerificationEmailService emailVerificationEmailService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.authMapper = authMapper;
        this.auditLogService = auditLogService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetService = passwordResetService;
        this.passwordResetEmailService = passwordResetEmailService;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationEmailService = emailVerificationEmailService;
    }

    @Override
    public AuthResponse login(final LoginRequest request, final String ipAddress, final String userAgent) {
        final User user = userRepository.findByEmailWithRolesAndPermissions(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login attempt for email={}", user.getEmail());
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        if (user.getTenantId() != null) {
            final Tenant tenant = tenantRepository.findById(user.getTenantId())
                    .orElseThrow(() -> new BusinessException("TENANT_MISSING", "User tenant is unavailable"));
            if (tenant.getStatus() != TenantStatus.ACTIVE) {
                throw new BusinessException("TENANT_NOT_ACTIVE", "Hospital tenant is not active");
            }
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        auditLogService.record(
                user.getTenantId(),
                user.getId(),
                ENTITY_USER,
                user.getId().toString(),
                AuditAction.LOGIN,
                null,
                null,
                ipAddress,
                userAgent
        );

        log.info("User logged in successfully userId={}", user.getId());
        return buildAuthResponse(user, ipAddress, userAgent);
    }

    @Override
    public HospitalRegistrationResponse registerHospital(
            final RegisterHospitalRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final String email = normalizeEmail(request.email());
        if (tenantRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "Hospital email is already registered");
        }

        final String slug = generateUniqueSlug(request.hospitalName());
        final Tenant tenant = new Tenant();
        tenant.setName(request.hospitalName().trim());
        tenant.setSlug(slug);
        tenant.setEmail(email);
        tenant.setPhone(trimToNull(request.phone()));
        tenant.setAddress(trimToNull(request.address()));
        tenant.setSubscriptionPlan(
                request.subscriptionPlan() == null ? SubscriptionPlan.BASIC : request.subscriptionPlan());
        // Stay PENDING until the initial hospital admin verifies email — prevents open tenant takeover.
        tenant.setStatus(TenantStatus.PENDING);

        final Tenant savedTenant = tenantRepository.save(tenant);

        auditLogService.record(
                savedTenant.getId(),
                null,
                ENTITY_TENANT,
                savedTenant.getId().toString(),
                AuditAction.CREATE,
                null,
                "Hospital registered: " + savedTenant.getName(),
                ipAddress,
                userAgent
        );

        log.info("Hospital tenant registered tenantId={} slug={}", savedTenant.getId(), savedTenant.getSlug());
        return authMapper.toHospitalRegistration(savedTenant);
    }

    @Override
    public UserProfileResponse registerInitialAdmin(
            final RegisterAdminRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital tenant not found"));

        if (tenant.getStatus() != TenantStatus.PENDING && tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new BusinessException("TENANT_NOT_ACTIVE", "Hospital tenant is not eligible for onboarding");
        }

        final long existingAdmins = userRepository.countByTenantIdAndRoleType(
                tenant.getId(),
                RoleType.HOSPITAL_ADMIN
        );
        if (existingAdmins > 0) {
            throw new ConflictException(
                    "INITIAL_ADMIN_EXISTS",
                    "Initial hospital administrator is already registered for this tenant"
            );
        }

        final String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "Email is already registered");
        }

        final Role hospitalAdminRole = roleRepository.findSystemRoleWithPermissions(RoleType.HOSPITAL_ADMIN)
                .orElseThrow(() -> new BusinessException(
                        "ROLE_NOT_CONFIGURED",
                        "Hospital Admin role is not configured"
                ));

        final User admin = new User();
        admin.setTenantId(tenant.getId());
        admin.setFirstName(request.firstName().trim());
        admin.setLastName(request.lastName().trim());
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setPhone(trimToNull(request.phone()));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(false);
        admin.addRole(hospitalAdminRole);

        final User savedAdmin = userRepository.save(admin);

        try {
            final String rawToken = emailVerificationService.issueVerificationToken(
                    savedAdmin,
                    ipAddress,
                    userAgent
            );
            emailVerificationEmailService.sendVerificationLink(savedAdmin, rawToken);

            auditLogService.record(
                    tenant.getId(),
                    savedAdmin.getId(),
                    ENTITY_USER,
                    savedAdmin.getId().toString(),
                    AuditAction.EMAIL_VERIFICATION_REQUEST,
                    null,
                    "Verification email sent on registration",
                    ipAddress,
                    userAgent
            );
        } catch (final EmailDeliveryException exception) {
            log.error(
                    "Verification email could not be delivered for userId={}",
                    savedAdmin.getId(),
                    exception
            );
        }

        auditLogService.record(
                tenant.getId(),
                savedAdmin.getId(),
                ENTITY_USER,
                savedAdmin.getId().toString(),
                AuditAction.CREATE,
                null,
                "Initial hospital admin registered (email verification pending)",
                ipAddress,
                userAgent
        );

        log.info(
                "Initial hospital admin registered userId={} tenantId={} (awaiting email verification)",
                savedAdmin.getId(),
                tenant.getId()
        );
        return authMapper.toUserProfile(savedAdmin);
    }

    @Override
    public AuthResponse refreshToken(
            final RefreshTokenRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final RefreshTokenService.RotatedTokens rotated =
                refreshTokenService.rotate(request.refreshToken(), ipAddress, userAgent);
        return buildAuthResponse(rotated.user(), rotated.newRefreshToken());
    }

    @Override
    public void logout(final String refreshToken, final String ipAddress, final String userAgent) {
        final AuthenticatedUser currentUser = SecurityUtils.requireCurrentUser();

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revokeToken(refreshToken);
        }
        refreshTokenService.revokeAllForUser(currentUser.getUserId());

        // Invalidate outstanding access JWTs immediately (JwtPrincipalValidator checks tokenVersion).
        userRepository.findById(currentUser.getUserId()).ifPresent(user -> {
            user.incrementTokenVersion();
            userRepository.save(user);
        });

        auditLogService.record(
                currentUser.getTenantId(),
                currentUser.getUserId(),
                ENTITY_USER,
                currentUser.getUserId().toString(),
                AuditAction.LOGOUT,
                null,
                null,
                ipAddress,
                userAgent
        );

        SecurityContextHolder.clearContext();
        log.info("User logged out userId={}", currentUser.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser() {
        final AuthenticatedUser currentUser = SecurityUtils.requireCurrentUser();
        final User user = userRepository.findByIdWithRolesAndPermissions(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return authMapper.toUserProfile(user);
    }

    @Override
    public UserProfileResponse updateProfile(
            final UpdateProfileRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final AuthenticatedUser currentUser = SecurityUtils.requireCurrentUser();
        final User user = userRepository.findByIdWithRolesAndPermissions(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        final String previousProfile = user.getFirstName() + " " + user.getLastName() + "|" + user.getPhone();

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(trimToNull(request.phone()));

        final User savedUser = userRepository.save(user);

        auditLogService.record(
                savedUser.getTenantId(),
                savedUser.getId(),
                ENTITY_USER,
                savedUser.getId().toString(),
                AuditAction.UPDATE,
                previousProfile,
                savedUser.getFirstName() + " " + savedUser.getLastName() + "|" + savedUser.getPhone(),
                ipAddress,
                userAgent
        );

        return authMapper.toUserProfile(savedUser);
    }

    @Override
    public void changePassword(
            final ChangePasswordRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final AuthenticatedUser currentUser = SecurityUtils.requireCurrentUser();
        final User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    "PASSWORD_REUSE",
                    "New password must be different from the current password"
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.incrementTokenVersion();
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user.getId());

        auditLogService.record(
                user.getTenantId(),
                user.getId(),
                ENTITY_USER,
                user.getId().toString(),
                AuditAction.PASSWORD_CHANGE,
                null,
                null,
                ipAddress,
                userAgent
        );

        log.info("Password changed for userId={}", user.getId());
    }

    @Override
    public void forgotPassword(
            final ForgotPasswordRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        // Always return success to the client (anti-enumeration). Only active accounts receive email.
        final String email = normalizeEmail(request.email());
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.info("Password reset skipped for inactive account userId={}", user.getId());
                return;
            }

            try {
                final String rawToken = passwordResetService.issueResetToken(user, ipAddress, userAgent);
                passwordResetEmailService.sendResetLink(user, rawToken);

                auditLogService.record(
                        user.getTenantId(),
                        user.getId(),
                        ENTITY_USER,
                        user.getId().toString(),
                        AuditAction.PASSWORD_RESET_REQUEST,
                        null,
                        null,
                        ipAddress,
                        userAgent
                );
            } catch (final EmailDeliveryException exception) {
                log.error(
                        "Password reset email could not be delivered for userId={}",
                        user.getId(),
                        exception
                );
            }
        });
    }

    @Override
    public void resetPassword(
            final ResetPasswordRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final User user = passwordResetService.requireValidResetToken(request.token());

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    "PASSWORD_REUSE",
                    "New password must be different from the current password"
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.incrementTokenVersion();
        userRepository.save(user);
        passwordResetService.markTokenUsed(request.token());
        refreshTokenService.revokeAllForUser(user.getId());

        auditLogService.record(
                user.getTenantId(),
                user.getId(),
                ENTITY_USER,
                user.getId().toString(),
                AuditAction.PASSWORD_RESET,
                null,
                null,
                ipAddress,
                userAgent
        );

        log.info("Password reset completed for userId={}", user.getId());
    }

    @Override
    public void verifyEmail(
            final VerifyEmailRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final User user = emailVerificationService.requireValidVerificationToken(request.token());

        user.markEmailVerified();
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
        emailVerificationService.markTokenUsed(request.token());

        // Activate pending hospital tenant once the initial admin verifies email.
        if (user.getTenantId() != null) {
            tenantRepository.findById(user.getTenantId()).ifPresent(tenant -> {
                if (tenant.getStatus() == TenantStatus.PENDING) {
                    tenant.setStatus(TenantStatus.ACTIVE);
                    tenantRepository.save(tenant);
                    log.info("Hospital tenant activated after admin verification tenantId={}", tenant.getId());
                }
            });
        }

        auditLogService.record(
                user.getTenantId(),
                user.getId(),
                ENTITY_USER,
                user.getId().toString(),
                AuditAction.EMAIL_VERIFIED,
                null,
                null,
                ipAddress,
                userAgent
        );

        log.info("Email verified for userId={}", user.getId());
    }

    @Override
    public void resendVerification(
            final ResendVerificationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        // Always return success to the client (anti-enumeration).
        final String email = normalizeEmail(request.email());
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            if (user.isEmailVerified()) {
                log.info("Resend verification skipped; already verified userId={}", user.getId());
                return;
            }

            if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.LOCKED) {
                log.info("Resend verification skipped for non-eligible status userId={}", user.getId());
                return;
            }

            try {
                final String rawToken = emailVerificationService.issueVerificationToken(
                        user,
                        ipAddress,
                        userAgent
                );
                emailVerificationEmailService.sendVerificationLink(user, rawToken);

                auditLogService.record(
                        user.getTenantId(),
                        user.getId(),
                        ENTITY_USER,
                        user.getId().toString(),
                        AuditAction.EMAIL_VERIFICATION_REQUEST,
                        null,
                        null,
                        ipAddress,
                        userAgent
                );
            } catch (final EmailDeliveryException exception) {
                log.error(
                        "Verification email could not be delivered for userId={}",
                        user.getId(),
                        exception
                );
            }
        });
    }

    private AuthResponse buildAuthResponse(
            final User user,
            final String ipAddress,
            final String userAgent
    ) {
        final String refreshToken = refreshTokenService.issueRefreshToken(user, ipAddress, userAgent);
        return buildAuthResponse(user, refreshToken);
    }

    private AuthResponse buildAuthResponse(final User user, final String refreshToken) {
        final Set<String> roles = user.getRoles().stream()
                .map(role -> role.getType().name())
                .collect(Collectors.toUnmodifiableSet());
        final Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toUnmodifiableSet());

        if (user.getTenantId() == null) {
            throw new BusinessException(
                    "TENANT_REQUIRED",
                    "Access token issuance requires a tenant-scoped user in this phase"
            );
        }

        final JwtClaims claims = new JwtClaims(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                roles,
                permissions,
                user.getTokenVersion(),
                JwtTokenType.ACCESS
        );

        final String accessToken = jwtService.generateAccessToken(claims);
        final long expiresInSeconds = jwtProperties.getAccessTokenExpiration().toSeconds();
        final long refreshExpiresInSeconds = jwtProperties.getRefreshTokenExpiration().toSeconds();

        return new AuthResponse(
                accessToken,
                refreshToken,
                TOKEN_TYPE_BEARER,
                expiresInSeconds,
                refreshExpiresInSeconds,
                authMapper.toUserProfile(user)
        );
    }

    private String generateUniqueSlug(final String hospitalName) {
        String baseSlug = toSlug(hospitalName);
        if (baseSlug.isBlank()) {
            baseSlug = "hospital";
        }

        String candidate = baseSlug;
        int suffix = 1;
        while (tenantRepository.existsBySlugIgnoreCase(candidate)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private static String toSlug(final String value) {
        final String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        final String dashed = NON_ALPHANUMERIC.matcher(normalized).replaceAll("-");
        return MULTI_DASH.matcher(dashed).replaceAll("-").replaceAll("^-|-$", "");
    }

    private static String normalizeEmail(final String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
