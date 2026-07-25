package com.healthcare.hms.users.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.service.HospitalQueryService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.principal.CurrentUser;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.service.TenantAccessService;
import com.healthcare.hms.users.config.UserInvitationProperties;
import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.dto.request.AcceptInvitationRequest;
import com.healthcare.hms.users.dto.request.CreateInvitationRequest;
import com.healthcare.hms.users.dto.request.RejectInvitationRequest;
import com.healthcare.hms.users.dto.response.AcceptInvitationResponse;
import com.healthcare.hms.users.dto.response.InvitationPreviewResponse;
import com.healthcare.hms.users.dto.response.UserInvitationResponse;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.entity.UserInvitation;
import com.healthcare.hms.users.enums.InvitationStatus;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.mapper.UserInvitationMapper;
import com.healthcare.hms.users.rbac.RoleHierarchy;
import com.healthcare.hms.users.repository.RoleRepository;
import com.healthcare.hms.users.repository.UserInvitationRepository;
import com.healthcare.hms.users.repository.UserInvitationSpecifications;
import com.healthcare.hms.users.repository.UserRepository;
import com.healthcare.hms.users.service.InvitationEmailService;
import com.healthcare.hms.users.service.UserInvitationService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Invite → email → accept/reject/resend/cancel with hashed tokens and audit (Phase 4.5).
 */
@Service
public class UserInvitationServiceImpl implements UserInvitationService {

    private static final Logger log = LoggerFactory.getLogger(UserInvitationServiceImpl.class);
    private static final String ENTITY = "USER_INVITATION";
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORT_PROPERTIES = Set.of(
            "email", "status", "roleType", "expiresAt", "createdAt", "updatedAt"
    );

    private final UserInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HospitalQueryService hospitalQueryService;
    private final TenantAccessService tenantAccessService;
    private final TokenHashingService tokenHashingService;
    private final UserInvitationProperties invitationProperties;
    private final InvitationEmailService invitationEmailService;
    private final PasswordEncoder passwordEncoder;
    private final UserInvitationMapper invitationMapper;
    private final AuditLogService auditLogService;

    public UserInvitationServiceImpl(
            final UserInvitationRepository invitationRepository,
            final UserRepository userRepository,
            final RoleRepository roleRepository,
            final HospitalQueryService hospitalQueryService,
            final TenantAccessService tenantAccessService,
            final TokenHashingService tokenHashingService,
            final UserInvitationProperties invitationProperties,
            final InvitationEmailService invitationEmailService,
            final PasswordEncoder passwordEncoder,
            final UserInvitationMapper invitationMapper,
            final AuditLogService auditLogService
    ) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hospitalQueryService = hospitalQueryService;
        this.tenantAccessService = tenantAccessService;
        this.tokenHashingService = tokenHashingService;
        this.invitationProperties = invitationProperties;
        this.invitationEmailService = invitationEmailService;
        this.passwordEncoder = passwordEncoder;
        this.invitationMapper = invitationMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_CREATE)
    public UserInvitationResponse invite(
            final CreateInvitationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        final String email = normalizeEmail(request.email());
        assertAssignableRole(request.roleType());
        assertEmailAvailable(email);
        assertNoPendingInvitation(tenantId, email);

        final Hospital hospital = hospitalQueryService.requireDefaultHospital();
        requireTenantRole(tenantId, request.roleType());

        final String rawToken = tokenHashingService.generateRawToken();
        final Instant now = Instant.now();

        final UserInvitation invitation = new UserInvitation();
        invitation.setHospitalId(hospital.getId());
        invitation.setEmail(email);
        invitation.setFirstName(trimToNull(request.firstName()));
        invitation.setLastName(trimToNull(request.lastName()));
        invitation.setRoleType(request.roleType());
        invitation.setInvitedBy(actorId);
        invitation.setTokenHash(tokenHashingService.hash(rawToken));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(now.plus(invitationProperties.getTokenExpiration()));
        invitation.setMessage(trimToNull(request.message()));
        invitation.setIpAddress(truncate(ipAddress, 45));
        invitation.setUserAgent(truncate(userAgent, 512));

        final UserInvitation saved = invitationRepository.save(invitation);
        invitationEmailService.sendInvitation(saved, rawToken, hospital.getName());

        audit(saved, actorId, AuditAction.INVITATION_SENT, null, ipAddress, userAgent);
        log.info("User invitation created id={} email={} role={} tenantId={}",
                saved.getId(), email, request.roleType(), tenantId);
        return invitationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.USER_READ)
    public UserInvitationResponse getById(final UUID invitationId) {
        return invitationMapper.toResponse(requireTenantInvitation(invitationId));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_READ)
    public PageResponse<UserInvitationResponse> search(
            final InvitationStatus status,
            final String email,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        expireStalePendingForTenant(tenantId);
        return PageResponse.from(invitationRepository
                .findAll(
                        UserInvitationSpecifications.withFilters(tenantId, status, email),
                        sanitizePageable(pageable)
                )
                .map(invitationMapper::toResponse));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_CREATE)
    public UserInvitationResponse resend(
            final UUID invitationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UserInvitation invitation = requireTenantInvitation(invitationId);
        if (!invitation.isPending()) {
            throw new BusinessException(
                    "INVITATION_NOT_PENDING",
                    "Only pending invitations can be resent"
            );
        }

        final String oldSnapshot = snapshot(invitation);
        final String rawToken = tokenHashingService.generateRawToken();
        final Instant now = Instant.now();
        invitation.setTokenHash(tokenHashingService.hash(rawToken));
        invitation.setExpiresAt(now.plus(invitationProperties.getTokenExpiration()));
        invitation.setIpAddress(truncate(ipAddress, 45));
        invitation.setUserAgent(truncate(userAgent, 512));

        final UserInvitation saved = invitationRepository.save(invitation);
        final Hospital hospital = hospitalQueryService.requireDefaultHospital();
        invitationEmailService.sendInvitationResent(saved, rawToken, hospital.getName());

        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        audit(saved, actorId, AuditAction.INVITATION_RESENT, oldSnapshot, ipAddress, userAgent);
        log.info("User invitation resent id={} tenantId={}", saved.getId(), saved.getTenantId());
        return invitationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_UPDATE)
    public UserInvitationResponse cancel(
            final UUID invitationId,
            final String ipAddress,
            final String userAgent
    ) {
        final UserInvitation invitation = requireTenantInvitation(invitationId);
        if (!invitation.isPending()) {
            throw new BusinessException(
                    "INVITATION_NOT_PENDING",
                    "Only pending invitations can be cancelled"
            );
        }

        final String oldSnapshot = snapshot(invitation);
        invitation.markCancelled();
        // Invalidate token so the emailed link can no longer be used.
        invitation.setTokenHash(tokenHashingService.hash(tokenHashingService.generateRawToken()));
        final UserInvitation saved = invitationRepository.save(invitation);

        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        audit(saved, actorId, AuditAction.INVITATION_CANCELLED, oldSnapshot, ipAddress, userAgent);
        log.info("User invitation cancelled id={} tenantId={}", saved.getId(), saved.getTenantId());
        return invitationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AcceptInvitationResponse accept(
            final AcceptInvitationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UserInvitation invitation = requireAcceptableInvitation(request.token());
        final String oldSnapshot = snapshot(invitation);
        final UUID tenantId = invitation.getTenantId();
        final String email = invitation.getEmail();

        // Tenant must still be operational before creating an authenticated account.
        tenantAccessService.requireActiveTenant(tenantId);

        // Align with login/password-reset: emails are globally unique across tenants.
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(
                    "USER_EMAIL_EXISTS",
                    "A user with this email is already registered"
            );
        }

        final Role role = requireTenantRole(tenantId, invitation.getRoleType());
        final User user = new User();
        user.setTenantId(tenantId);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(trimToNull(request.phone()));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        user.addRole(role);
        final User savedUser = userRepository.save(user);

        invitation.markAccepted(savedUser.getId());
        invitation.setFirstName(savedUser.getFirstName());
        invitation.setLastName(savedUser.getLastName());
        invitationRepository.save(invitation);

        audit(invitation, savedUser.getId(), AuditAction.INVITATION_ACCEPTED, oldSnapshot, ipAddress, userAgent);
        log.info(
                "Invitation accepted invitationId={} userId={} tenantId={} role={}",
                invitation.getId(),
                savedUser.getId(),
                tenantId,
                invitation.getRoleType()
        );

        return new AcceptInvitationResponse(
                invitation.getId(),
                savedUser.getId(),
                tenantId,
                invitation.getHospitalId(),
                email,
                invitation.getRoleType(),
                "Invitation accepted; account created and role assigned"
        );
    }

    @Override
    @Transactional
    public InvitationPreviewResponse reject(
            final RejectInvitationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final UserInvitation invitation = requireAcceptableInvitation(request.token());
        final String oldSnapshot = snapshot(invitation);
        invitation.markRejected();
        invitation.setTokenHash(tokenHashingService.hash(tokenHashingService.generateRawToken()));
        final UserInvitation saved = invitationRepository.save(invitation);

        audit(saved, null, AuditAction.INVITATION_REJECTED, oldSnapshot, ipAddress, userAgent);
        log.info("Invitation rejected id={} tenantId={}", saved.getId(), saved.getTenantId());
        return toPreview(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationPreviewResponse previewByToken(final String rawToken) {
        return toPreview(requireAcceptableInvitation(rawToken));
    }

    private InvitationPreviewResponse toPreview(final UserInvitation invitation) {
        final String hospitalName = hospitalQueryService
                .findByIdAndTenantId(invitation.getHospitalId(), invitation.getTenantId())
                .map(Hospital::getName)
                .orElse("Hospital");
        return new InvitationPreviewResponse(
                invitation.getEmail(),
                invitation.getFirstName(),
                invitation.getLastName(),
                invitation.getRoleType(),
                hospitalName,
                invitation.getExpiresAt(),
                invitation.isExpired()
        );
    }

    private UserInvitation requireTenantInvitation(final UUID invitationId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UserInvitation invitation = invitationRepository.findByIdAndTenantId(invitationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        refreshExpiredStatus(invitation);
        return invitation;
    }

    private UserInvitation requireAcceptableInvitation(final String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Invitation token is required");
        }
        final String tokenHash = tokenHashingService.hash(rawToken.trim());
        final UserInvitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invitation token is invalid or no longer valid"));

        if (invitation.isPending() && invitation.isExpired()) {
            invitation.markExpired();
            invitationRepository.save(invitation);
            throw new ExpiredTokenException("Invitation has expired");
        }
        if (!invitation.isPending()) {
            throw new InvalidTokenException("Invitation is no longer pending");
        }
        return invitation;
    }

    private void refreshExpiredStatus(final UserInvitation invitation) {
        if (invitation.isPending() && invitation.isExpired()) {
            invitation.markExpired();
            invitationRepository.save(invitation);
        }
    }

    private void expireStalePendingForTenant(final UUID tenantId) {
        final List<UserInvitation> stale = invitationRepository.findStalePendingByTenantId(
                tenantId, Instant.now());
        for (final UserInvitation invitation : stale) {
            invitation.markExpired();
            invitationRepository.save(invitation);
        }
    }

    private void assertAssignableRole(final RoleType roleType) {
        final CurrentUser actor = SecurityUtils.requireCurrentUser();
        final boolean platformAdmin = actor.hasRole(RoleType.Names.SUPER_ADMIN);
        final boolean allowed = platformAdmin
                ? RoleHierarchy.isAssignable(roleType)
                : RoleHierarchy.isInvitableByTenantAdmin(roleType);
        if (!allowed) {
            throw new BusinessException(
                    "INVITATION_ROLE_NOT_ALLOWED",
                    "Role type cannot be invited: " + roleType
            );
        }
    }

    private void assertEmailAvailable(final String email) {
        // Global uniqueness matches registration + email-only login resolution.
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(
                    "USER_EMAIL_EXISTS",
                    "A user with this email is already registered"
            );
        }
    }

    private void assertNoPendingInvitation(final UUID tenantId, final String email) {
        expireStalePendingForTenant(tenantId);
        if (invitationRepository.existsActivePendingByTenantIdAndEmailIgnoreCase(
                tenantId, email, Instant.now())) {
            throw new ConflictException(
                    "INVITATION_ALREADY_PENDING",
                    "A pending invitation already exists for this email"
            );
        }
    }

    private Role requireTenantRole(final UUID tenantId, final RoleType roleType) {
        return roleRepository.findByTenantIdAndType(tenantId, roleType)
                .orElseThrow(() -> new BusinessException(
                        "INVITATION_ROLE_MISSING",
                        "Tenant role is not provisioned: " + roleType
                ));
    }

    private void audit(
            final UserInvitation invitation,
            final UUID actorId,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        auditLogService.record(
                invitation.getTenantId(),
                actorId,
                ENTITY,
                invitation.getId().toString(),
                action,
                oldSnapshot,
                snapshot(invitation),
                ipAddress,
                userAgent
        );
    }

    private static Pageable sanitizePageable(final Pageable pageable) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        final Sort safeSort = Sort.by(pageable.getSort().stream()
                .filter(order -> SORT_PROPERTIES.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList());
        if (safeSort.isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return PageRequest.of(page, size, safeSort);
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

    private static String truncate(final String value, final int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String snapshot(final UserInvitation invitation) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", invitation.getId());
        fields.put("email", invitation.getEmail());
        fields.put("roleType", invitation.getRoleType());
        fields.put("status", invitation.getStatus());
        fields.put("expiresAt", invitation.getExpiresAt());
        fields.put("acceptedUserId", invitation.getAcceptedUserId());
        fields.put("hospitalId", invitation.getHospitalId());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
