package com.healthcare.hms.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hms.auth.service.PendingRegistrationService;
import com.healthcare.hms.common.email.EmailMessage;
import com.healthcare.hms.common.email.EmailSender;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * One-off Phase 7 verification against a real, local MySQL — no Docker/testcontainers.
 *
 * <p>Runs only when {@code -Dhms.it.db.url=...} is provided (see {@link AbstractMySqlIntegrationTest}).
 * Emits real {@code SELECT} output against the live DB as proof.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "hms.it.db.url", matches = ".+")
class PendingRegistrationLiveVerificationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("token=([0-9a-f]{96})");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RecordingEmailSender recordingEmailSender;
    @Autowired
    private PendingRegistrationService pendingRegistrationService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        EmailSender recordingEmailSender() {
            return new RecordingEmailSender();
        }
    }

    @DynamicPropertySource
    static void datasource(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty("hms.it.db.url"));
        registry.add("spring.datasource.username", () -> System.getProperty("hms.it.db.username", "hms_user"));
        registry.add("spring.datasource.password", () -> System.getProperty("hms.it.db.password", "hms_password"));
    }

    private static final String RUN_ID = java.util.UUID.randomUUID().toString().substring(0, 8);
    private static final String ADMIN_EMAIL = "phase7." + RUN_ID + "@test.local";
    private static final String HOSPITAL_EMAIL = "phase7." + RUN_ID + ".hospital@test.local";
    private static final String HOSPITAL_NAME = "Phase 7 Live Hospital " + RUN_ID;

    @Test
    void fullRegistrationIsDeferredUntilVerification() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "firstName", "Live",
                "lastName", "Verify",
                "email", ADMIN_EMAIL,
                "password", "StrongPass1!ab",
                "phone", "+1234567890",
                "hospitalName", HOSPITAL_NAME,
                "hospitalEmail", HOSPITAL_EMAIL,
                "hospitalPhone", "+0987654321",
                "hospitalAddress", "120 Medical Center Drive",
                "subscriptionPlan", "PREMIUM"
        ));

        mockMvc.perform(post("/api/v1/register")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isAccepted());

        Integer tenantsBefore = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tenants WHERE email = ?", Integer.class, HOSPITAL_EMAIL);
        Integer usersBefore = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, ADMIN_EMAIL);
        System.out.println("[DB] SELECT COUNT(*) tenants WHERE email=hospital: " + tenantsBefore);
        System.out.println("[DB] SELECT COUNT(*) users WHERE email=admin: " + usersBefore);
        assertThat(tenantsBefore).isZero();
        assertThat(usersBefore).isZero();

        Map<String, Object> pending = jdbc.queryForMap(
                "SELECT email, hospital_name, hospital_email, subscription_plan, token_expires_at, verified_at "
                        + "FROM pending_registrations WHERE email = ?", ADMIN_EMAIL);
        System.out.println("[DB] pending_registrations row: " + pending);
        assertThat(pending.get("hospital_name")).isEqualTo(HOSPITAL_NAME);
        assertThat(pending.get("subscription_plan")).isEqualTo("PREMIUM");
        assertThat(pending.get("verified_at")).isNull();

        EmailMessage email = recordingEmailSender.lastMessage();
        assertThat(email).isNotNull();
        assertThat(email.to()).isEqualTo(ADMIN_EMAIL);
        Matcher matcher = TOKEN_PATTERN.matcher(email.textBody());
        assertThat(matcher.find()).as("verification link with token").isTrue();
        String rawToken = matcher.group(1);

        mockMvc.perform(get("/api/v1/verify-registration").param("token", rawToken))
                .andExpect(status().isOk());

        Map<String, Object> tenant = jdbc.queryForMap(
                "SELECT name, email, subscription_plan, trial_ends_at FROM tenants WHERE email = ?",
                HOSPITAL_EMAIL);
        System.out.println("[DB] tenant created: " + tenant);
        assertThat(tenant.get("name")).isEqualTo(HOSPITAL_NAME);
        assertThat(tenant.get("subscription_plan")).isEqualTo("PREMIUM");

        Instant trialEndsAt = ((java.sql.Timestamp) tenant.get("trial_ends_at")).toInstant();
        Instant now = Instant.now();
        Duration until = Duration.between(now, trialEndsAt);
        System.out.println("[DB] trial_ends_at=" + trialEndsAt + " now=" + now + " (~" + until.toHours() + "h ahead)");
        assertThat(until.toDays()).isBetween(13L, 15L);

        Map<String, Object> admin = jdbc.queryForMap(
                "SELECT email, first_name, last_name, email_verified FROM users WHERE email = ?",
                ADMIN_EMAIL);
        System.out.println("[DB] admin user created: " + admin);
        assertThat(admin.get("email_verified")).isEqualTo(true);

        Integer pendingAfter = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pending_registrations WHERE email = ?", Integer.class, ADMIN_EMAIL);
        System.out.println("[DB] pending_registrations after verify: " + pendingAfter);
        assertThat(pendingAfter).isZero();
    }

    @Test
    void expiredUnverifiedPendingIsCleanedUp() {
        String email = "phase7." + java.util.UUID.randomUUID().toString().substring(0, 8) + ".cleanup@test.local";

        // Create a REAL pending row through the application (Hibernate stores UTC consistently),
        // then fast-forward its expiry to the past using MySQL's UTC clock.
        pendingRegistrationService.submit(new com.healthcare.hms.auth.dto.request.RegistrationRequest(
                "Cleanup", "Pending", email, "StrongPass1!ab", null,
                "Cleanup Hospital", "cleanup.live.hospital@test.local", null, null,
                com.healthcare.hms.tenant.enums.SubscriptionPlan.BASIC
        ), "127.0.0.1", "test");

        int backdated = jdbc.update(
                "UPDATE pending_registrations SET token_expires_at = UTC_TIMESTAMP() - INTERVAL 2 HOUR WHERE email = ?",
                email);
        System.out.println("[DB] rows backdated to expired: " + backdated);

        Integer before = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pending_registrations WHERE email = ?", Integer.class, email);
        System.out.println("[DB] pending_registrations before cleanup: " + before);

        int deleted = pendingRegistrationService.deleteExpired(Instant.now());
        System.out.println("[DB] deleteExpired(now) returned = " + deleted);

        Integer after = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pending_registrations WHERE email = ?", Integer.class, email);
        System.out.println("[DB] pending_registrations after cleanup: " + after);
        assertThat(after).isZero();
    }

    static class RecordingEmailSender implements EmailSender {
        private EmailMessage last;

        @Override
        public void send(final EmailMessage message) {
            this.last = message;
        }

        EmailMessage lastMessage() {
            return last;
        }
    }
}
