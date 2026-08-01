package com.healthcare.hms.auth.service.impl;

import com.healthcare.hms.auth.config.EmailVerificationProperties;
import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.auth.dto.request.RegistrationRequest;
import com.healthcare.hms.auth.dto.response.PendingRegistrationResponse;
import com.healthcare.hms.auth.entity.PendingRegistration;
import com.healthcare.hms.auth.repository.PendingRegistrationRepository;
import com.healthcare.hms.auth.service.PendingRegistrationEmailService;
import com.healthcare.hms.auth.service.PendingRegistrationService;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.hospitals.service.HospitalRegistrationService;
import com.healthcare.hms.tenant.repository.TenantRepository;
import com.healthcare.hms.users.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Phase 7 registration: persists only a lightweight pending record on submit and creates the
 * real tenant/hospital/admin records only when the emailed verification link is consumed.
 */
@Service
public class PendingRegistrationServiceImpl implements PendingRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(PendingRegistrationServiceImpl.class);

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TokenHashingService tokenHashingService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationProperties emailVerificationProperties;
    private final PendingRegistrationEmailService pendingRegistrationEmailService;
    private final HospitalRegistrationService hospitalRegistrationService;

    public PendingRegistrationServiceImpl(
            final PendingRegistrationRepository pendingRegistrationRepository,
            final UserRepository userRepository,
            final TenantRepository tenantRepository,
            final TokenHashingService tokenHashingService,
            final PasswordEncoder passwordEncoder,
            final EmailVerificationProperties emailVerificationProperties,
            final PendingRegistrationEmailService pendingRegistrationEmailService,
            final HospitalRegistrationService hospitalRegistrationService
    ) {
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tokenHashingService = tokenHashingService;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationProperties = emailVerificationProperties;
        this.pendingRegistrationEmailService = pendingRegistrationEmailService;
        this.hospitalRegistrationService = hospitalRegistrationService;
    }

    @Override
    @Transactional
    public PendingRegistrationResponse submit(
            final RegistrationRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final String adminEmail = normalizeEmail(request.email());
        final String hospitalEmail = normalizeEmail(request.hospitalEmail());

        if (pendingRegistrationRepository.existsByEmailIgnoreCaseAndVerifiedAtIsNullAndDeletedFalse(adminEmail)) {
            throw new ConflictException(
                    "EMAIL_ALREADY_EXISTS",
                    "A registration for this email is already awaiting verification. Please check your inbox or resend the verification email."
            );
        }
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            throw new ConflictException(
                    "EMAIL_ALREADY_EXISTS",
                    "An account with this email address already exists"
            );
        }
        if (tenantRepository.existsByEmailIgnoreCase(hospitalEmail)) {
            throw new ConflictException(
                    "EMAIL_ALREADY_EXISTS",
                    "A hospital with this email address already exists"
            );
        }

        final PendingRegistration pending = new PendingRegistration();
        pending.setFirstName(trimToNull(request.firstName()));
        pending.setLastName(trimToNull(request.lastName()));
        pending.setEmail(adminEmail);
        pending.setPasswordHash(passwordEncoder.encode(request.password()));
        pending.setPhone(trimToNull(request.phone()));
        pending.setHospitalName(trimToNull(request.hospitalName()));
        pending.setHospitalEmail(hospitalEmail);
        pending.setHospitalPhone(trimToNull(request.hospitalPhone()));
        pending.setHospitalAddress(trimToNull(request.hospitalAddress()));
        pending.setSubscriptionPlan(request.subscriptionPlan());

        final Instant now = Instant.now();
        final String rawToken = tokenHashingService.generateRawToken();
        pending.setTokenHash(tokenHashingService.hash(rawToken));
        pending.setTokenExpiresAt(now.plus(emailVerificationProperties.getTokenExpiration()));
        pending.setSubmittedAt(now);

        pendingRegistrationRepository.save(pending);

        sendVerificationEmail(pending, rawToken);

        log.info(
                "Pending registration submitted email={} plan={} hospital={}",
                adminEmail, pending.getSubscriptionPlan(), pending.getHospitalName()
        );

        return new PendingRegistrationResponse(
                adminEmail,
                (int) emailVerificationProperties.getTokenExpiration().toMinutes()
        );
    }

    @Override
    @Transactional
    public PendingRegistrationResponse resendVerification(
            final String email,
            final String ipAddress,
            final String userAgent
    ) {
        final String normalized = normalizeEmail(email);
        final PendingRegistration pending =
                pendingRegistrationRepository
                        .findFirstByEmailIgnoreCaseAndVerifiedAtIsNullAndDeletedFalseOrderByCreatedAtDesc(normalized)
                        .orElseThrow(() -> new InvalidTokenException(
                                "No pending registration found for this email address"
                        ));

        final Instant now = Instant.now();
        final String rawToken = tokenHashingService.generateRawToken();
        pending.setTokenHash(tokenHashingService.hash(rawToken));
        pending.setTokenExpiresAt(now.plus(emailVerificationProperties.getTokenExpiration()));
        pendingRegistrationRepository.save(pending);

        sendVerificationEmail(pending, rawToken);

        log.info("Pending registration verification email resent email={}", normalized);
        return new PendingRegistrationResponse(
                normalized,
                (int) emailVerificationProperties.getTokenExpiration().toMinutes()
        );
    }

    @Override
    @Transactional
    public HospitalRegistrationResponse verify(
            final String rawToken,
            final String ipAddress,
            final String userAgent
    ) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Registration verification token is required");
        }

        final String tokenHash = tokenHashingService.hash(rawToken.trim());
        final PendingRegistration pending = pendingRegistrationRepository
                .findByTokenHashAndVerifiedAtIsNullAndDeletedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(
                        "Registration verification token is invalid or has already been used"
                ));

        if (Instant.now().isAfter(pending.getTokenExpiresAt())) {
            throw new ExpiredTokenException(
                    "Registration verification link has expired. Please start a new registration."
            );
        }

        final HospitalRegistrationRequest registrationRequest = new HospitalRegistrationRequest(
                pending.getHospitalName(),
                pending.getHospitalEmail(),
                pending.getHospitalPhone(),
                pending.getHospitalAddress(),
                pending.getSubscriptionPlan(),
                pending.getFirstName(),
                pending.getLastName(),
                pending.getEmail(),
                // Raw password is never stored; the already-hashed value is applied directly.
                pending.getPasswordHash(),
                pending.getPhone()
        );

        final HospitalRegistrationResponse response = hospitalRegistrationService.registerVerified(
                registrationRequest,
                pending.getPasswordHash(),
                ipAddress,
                userAgent
        );

        pendingRegistrationRepository.delete(pending);

        log.info(
                "Pending registration verified and records created email={} tenant={}",
                pending.getEmail(), response.tenantSlug()
        );

        return response;
    }

    @Override
    @Transactional
    public int deleteExpired(final Instant now) {
        final int deleted = pendingRegistrationRepository.deleteExpiredUnverified(now);
        if (deleted > 0) {
            log.info("Deleted {} expired unverified pending registrations", deleted);
        }
        return deleted;
    }

    private void sendVerificationEmail(final PendingRegistration pending, final String rawToken) {
        try {
            pendingRegistrationEmailService.sendVerificationLink(
                    pending.getEmail(),
                    pending.getFirstName(),
                    rawToken
            );
        } catch (final RuntimeException exception) {
            // Keep the pending record (user can resend), but surface the failure.
            log.error(
                    "Registration verification email delivery failed for email={}",
                    pending.getEmail(),
                    exception
            );
        }
    }

    private static String normalizeEmail(final String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
