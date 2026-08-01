package com.healthcare.hms.hospitals.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.auth.service.EmailVerificationEmailService;
import com.healthcare.hms.auth.service.EmailVerificationService;
import com.healthcare.hms.common.email.EmailDeliveryException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.hospitals.bootstrap.TenantRoleProvisioner;
import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.enums.HospitalStatus;
import com.healthcare.hms.hospitals.mapper.HospitalRegistrationMapper;
import com.healthcare.hms.hospitals.repository.HospitalRepository;
import com.healthcare.hms.hospitals.service.HospitalRegistrationService;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.tenant.repository.TenantRepository;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.UserRepository;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Atomic hospital registration: tenant, default hospital, roles/permissions, initial admin.
 *
 * <p>DB operations run in a single transaction. Verification email delivery is deferred
 * until after the transaction commits, keeping the transaction boundary tight and
 * preventing any FK lock contention from REQUIRES_NEW sub-transactions.
 */
@Service
public class HospitalRegistrationServiceImpl implements HospitalRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(HospitalRegistrationServiceImpl.class);
    private static final String ENTITY_TENANT = "TENANT";
    private static final String ENTITY_HOSPITAL = "HOSPITAL";
    private static final String ENTITY_USER = "USER";
    private static final String DEFAULT_HOSPITAL_CODE = "DEFAULT";
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");
    private static final long TRIAL_DURATION_DAYS = 14;

    /** Plans that qualify for a free trial period. */
    private static final List<SubscriptionPlan> TRIAL_ELIGIBLE_PLANS = List.of(
            SubscriptionPlan.STANDARD,
            SubscriptionPlan.PREMIUM
    );

    private final TenantRepository tenantRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final TenantRoleProvisioner tenantRoleProvisioner;
    private final PasswordEncoder passwordEncoder;
    private final HospitalRegistrationMapper hospitalRegistrationMapper;
    private final AuditLogService auditLogService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationEmailService emailVerificationEmailService;
    private final TransactionTemplate transactionTemplate;

    public HospitalRegistrationServiceImpl(
            final TenantRepository tenantRepository,
            final HospitalRepository hospitalRepository,
            final UserRepository userRepository,
            final TenantRoleProvisioner tenantRoleProvisioner,
            final PasswordEncoder passwordEncoder,
            final HospitalRegistrationMapper hospitalRegistrationMapper,
            final AuditLogService auditLogService,
            final EmailVerificationService emailVerificationService,
            final EmailVerificationEmailService emailVerificationEmailService,
            final PlatformTransactionManager transactionManager
    ) {
        this.tenantRepository = tenantRepository;
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
        this.tenantRoleProvisioner = tenantRoleProvisioner;
        this.passwordEncoder = passwordEncoder;
        this.hospitalRegistrationMapper = hospitalRegistrationMapper;
        this.auditLogService = auditLogService;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationEmailService = emailVerificationEmailService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public HospitalRegistrationResponse register(
            final HospitalRegistrationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final String hospitalEmail = normalizeEmail(request.hospitalEmail());
        final String adminEmail = normalizeEmail(request.adminEmail());

        if (tenantRepository.existsByEmailIgnoreCase(hospitalEmail)
                || userRepository.existsByEmailIgnoreCase(adminEmail)) {
            throw new ConflictException(
                    "EMAIL_ALREADY_EXISTS",
                    "Registration could not be completed with the provided email addresses"
            );
        }

        // All DB writes run inside a single transaction that commits before we
        // proceed to send the verification email (avoids FK lock contention from
        // REQUIRES_NEW sub-transactions and keeps the transaction boundary tight).
        final RegistrationContext ctx = transactionTemplate.execute(status -> {
            final Tenant tenant = createTenant(request, hospitalEmail);
            final Hospital hospital = createDefaultHospital(tenant, request, hospitalEmail);
            final List<Role> roles = tenantRoleProvisioner.provisionDefaultRoles(tenant.getId());
            final Role hospitalAdminRole = roles.stream()
                    .filter(role -> role.getType() == RoleType.HOSPITAL_ADMIN)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Hospital Admin role was not provisioned"));
            final User admin = createInitialAdmin(tenant, request, adminEmail, hospitalAdminRole);

            auditLogService.record(
                    tenant.getId(), admin.getId(),
                    ENTITY_TENANT, tenant.getId().toString(),
                    AuditAction.CREATE, null,
                    "Hospital tenant registered: " + tenant.getName(),
                    ipAddress, userAgent
            );
            auditLogService.record(
                    tenant.getId(), admin.getId(),
                    ENTITY_HOSPITAL, hospital.getId().toString(),
                    AuditAction.CREATE, null,
                    "Default hospital created: " + hospital.getName(),
                    ipAddress, userAgent
            );
            auditLogService.record(
                    tenant.getId(), admin.getId(),
                    ENTITY_USER, admin.getId().toString(),
                    AuditAction.CREATE, null,
                    "Initial hospital admin registered (email verification pending)",
                    ipAddress, userAgent
            );

            return new RegistrationContext(tenant, hospital, admin, roles);
        });

        // At this point the transaction has committed — all DB locks released.
        // Send verification email in its own transaction context.
        sendVerificationEmail(ctx.admin, ipAddress, userAgent);

        final List<String> provisionedRoleNames = ctx.roles.stream()
                .map(role -> role.getType().name())
                .toList();

        log.info(
                "Hospital registration completed tenantId={} hospitalId={} adminUserId={} roles={}",
                ctx.tenant.getId(),
                ctx.hospital.getId(),
                ctx.admin.getId(),
                provisionedRoleNames
        );

        return hospitalRegistrationMapper.toResponse(ctx.tenant, ctx.hospital, ctx.admin, provisionedRoleNames);
    }

    private Tenant createTenant(final HospitalRegistrationRequest request, final String hospitalEmail) {
        final SubscriptionPlan plan = request.subscriptionPlan() != null
                ? request.subscriptionPlan()
                : SubscriptionPlan.BASIC;

        final Tenant tenant = new Tenant();
        tenant.setName(request.hospitalName().trim());
        tenant.setSlug(generateUniqueSlug(request.hospitalName()));
        tenant.setTenantType(TenantType.HOSPITAL);
        tenant.setEmail(hospitalEmail);
        tenant.setPhone(trimToNull(request.hospitalPhone()));
        tenant.setAddress(trimToNull(request.hospitalAddress()));
        tenant.setSubscriptionPlan(plan);

        // Set free trial for eligible paid plans
        if (TRIAL_ELIGIBLE_PLANS.contains(plan)) {
            tenant.setTrialEndsAt(Instant.now().plus(TRIAL_DURATION_DAYS, ChronoUnit.DAYS));
        }

        tenant.setStatus(TenantStatus.PENDING);
        return tenantRepository.save(tenant);
    }

    private Hospital createDefaultHospital(
            final Tenant tenant,
            final HospitalRegistrationRequest request,
            final String hospitalEmail
    ) {
        final Hospital hospital = new Hospital();
        hospital.setTenantId(tenant.getId());
        hospital.setName(request.hospitalName().trim());
        hospital.setCode(DEFAULT_HOSPITAL_CODE);
        hospital.setEmail(hospitalEmail);
        hospital.setPhone(trimToNull(request.hospitalPhone()));
        hospital.setAddress(trimToNull(request.hospitalAddress()));
        hospital.setTimezone(Hospital.DEFAULT_TIMEZONE);
        hospital.setCurrency(Hospital.DEFAULT_CURRENCY);
        hospital.setLanguage(Hospital.DEFAULT_LANGUAGE);
        hospital.setDefaultHospital(true);
        hospital.setStatus(HospitalStatus.PENDING);
        return hospitalRepository.save(hospital);
    }

    private User createInitialAdmin(
            final Tenant tenant,
            final HospitalRegistrationRequest request,
            final String adminEmail,
            final Role hospitalAdminRole
    ) {
        final User admin = new User();
        admin.setTenantId(tenant.getId());
        admin.setFirstName(request.adminFirstName().trim());
        admin.setLastName(request.adminLastName().trim());
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(request.adminPassword()));
        admin.setPhone(trimToNull(request.adminPhone()));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(false);
        admin.addRole(hospitalAdminRole);
        return userRepository.save(admin);
    }

    private void sendVerificationEmail(final User admin, final String ipAddress, final String userAgent) {
        // Concurrent hospital registrations issue verification tokens in separate,
        // post-commit transactions. Those concurrent INSERTs into
        // email_verification_tokens can deadlock in MySQL (error 1213) and surface as
        // CannotAcquireLockException. Retrying the token-issuance transaction is the
        // documented MySQL remedy — a fresh transaction is started on each attempt.
        final int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                final String rawToken = emailVerificationService.issueVerificationToken(admin, ipAddress, userAgent);
                emailVerificationEmailService.sendVerificationLink(admin, rawToken);
                auditLogService.record(
                        admin.getTenantId(),
                        admin.getId(),
                        ENTITY_USER,
                        admin.getId().toString(),
                        AuditAction.EMAIL_VERIFICATION_REQUEST,
                        null,
                        "Verification email sent on hospital registration",
                        ipAddress,
                        userAgent
                );
                return;
            } catch (final CannotAcquireLockException deadlock) {
                if (attempt >= maxAttempts) {
                    log.error(
                            "Verification email skipped after {} deadlock retries for adminUserId={}",
                            attempt,
                            admin.getId(),
                            deadlock
                    );
                    return;
                }
                log.warn(
                        "Verification token issuance deadlocked (attempt {}/{}), retrying for adminUserId={}",
                        attempt,
                        maxAttempts,
                        admin.getId()
                );
                try {
                    Thread.sleep(50L * attempt);
                } catch (final InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (final EmailDeliveryException exception) {
                log.error(
                        "Verification email could not be delivered for adminUserId={}",
                        admin.getId(),
                        exception
                );
                return;
            }
        }
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

    /** Holder for objects created inside the transaction callback. */
    private record RegistrationContext(Tenant tenant, Hospital hospital, User admin, List<Role> roles) {}
}
