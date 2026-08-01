package com.healthcare.hms.auth.config;

import com.healthcare.hms.auth.service.PendingRegistrationService;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Enables the periodic purge of expired, unverified pending registrations (Phase 7).
 *
 * <p>Disabled by default — mirror the reminders scheduler flag. Set
 * {@code hms.registration.scheduler-enabled=true} in prod so abandoned signups are
 * self-cleaning. The cleanup is also callable directly from tests.
 */
@Configuration
public class PendingRegistrationCleanupConfiguration {

    /**
     * Nested config so {@code @EnableScheduling} is only active when the flag is on.
     */
    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(prefix = "hms.registration", name = "scheduler-enabled", havingValue = "true")
    static class PendingRegistrationSchedulingConfiguration {

        private static final Logger log =
                LoggerFactory.getLogger(PendingRegistrationSchedulingConfiguration.class);

        private final PendingRegistrationService pendingRegistrationService;

        PendingRegistrationSchedulingConfiguration(final PendingRegistrationService pendingRegistrationService) {
            this.pendingRegistrationService = pendingRegistrationService;
        }

        @Scheduled(cron = "${hms.registration.cleanup-cron:0 */5 * * * *}")
        public void runCleanup() {
            final int deleted = pendingRegistrationService.deleteExpired(Instant.now());
            if (deleted > 0) {
                log.info("Pending registration cleanup run deleted={}", deleted);
            }
        }
    }
}
