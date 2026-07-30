package com.healthcare.hms.auth.service.impl;

import com.healthcare.hms.auth.service.RegistrationSetupService;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.users.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * In-memory registration setup store. Tokens expire after 1 hour.
 * Safe for single-instance deployments; replace with Redis for horizontal scaling.
 */
@Service
public class RegistrationSetupServiceImpl implements RegistrationSetupService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationSetupServiceImpl.class);
    private static final Duration TOKEN_TTL = Duration.ofHours(1);

    private final Map<String, SetupEntry> store = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public RegistrationSetupServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String createSetup(final SetupData data) {
        if (userRepository.existsByEmailIgnoreCase(data.email())) {
            throw new ConflictException(
                    "EMAIL_ALREADY_EXISTS",
                    "An account with this email address already exists"
            );
        }

        final String token = UUID.randomUUID().toString();
        store.put(token, new SetupEntry(data, Instant.now().plus(TOKEN_TTL)));
        log.info("Registration setup created token={} email={}", tokenPrefix(token), data.email());
        return token;
    }

    @Override
    public SetupData consumeSetup(final String token) {
        final SetupEntry entry = store.remove(token);
        if (entry == null) {
            return null;
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            log.warn("Registration setup expired token={}", tokenPrefix(token));
            return null;
        }
        log.info("Registration setup consumed token={}", tokenPrefix(token));
        return entry.data();
    }

    private static String tokenPrefix(final String token) {
        return token.length() > 8 ? token.substring(0, 8) + "…" : token;
    }

    private record SetupEntry(SetupData data, Instant expiresAt) {}
}
