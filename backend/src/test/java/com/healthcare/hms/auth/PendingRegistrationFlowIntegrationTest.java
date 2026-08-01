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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import com.healthcare.hms.support.AbstractMySqlIntegrationTest;

/**
 * Phase 7 end-to-end proof against a real (testcontainers) MySQL:
 * submit -> NO real records; pending row correct; verify link -> real records with
 * trialEndsAt from verification moment; scheduled cleanup removes expired pendings.
 */
class PendingRegistrationFlowIntegrationTest extends AbstractMySqlIntegrationTest {

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

    private static final String ADMIN_EMAIL = "phase7.admin@test.local";
    private static final String HOSPITAL_EMAIL = "phase7.hospital@test.local";
    private static final String HOSPITAL_NAME = "Phase 7 Integration Hospital";

    @Test
    void fullRegistrationIsDeferredUntilVerification() throws Exception {
        // ---- 1. Submit the single-page registration payload ----
        String payload = objectMapper.writeValueAsString(Map.of(
                "firstName", "Phase",
                "lastName", "Seven",
                "email", ADMIN_EMAIL,
                "password", "StrongPass1!ab",
                "phone", "+1234567890",
                "hospitalName", HOSPITAL_NAME,
                "hospitalEmail", HOSPITAL_EMAIL,
                "hospitalPhone", "+0987654321",
                "hospitalAddress", "120 Medical Center Drive",
                "subscriptionPlan", "STANDARD"
        ));

        mockMvc.perform(post("/api/v1/register")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isAccepted());

        // ---- 2. PROOF: no real tenant / hospital / user exists yet ----
        Integer tenantsBefore = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tenants WHERE email = ?", Integer.class, HOSPITAL_EMAIL);
        Integer usersBefore = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, ADMIN_EMAIL);
        System.out.println("[DB] tenants rows for hospital email (after submit): " + tenantsBefore);
        System.out.println("[DB] users rows for admin email (after submit): " + usersBefore);
        assertThat(tenantsBefore).isZero();
        assertThat(usersBefore).isZero();

        // ---- 3. PROOF: the pending registration row is present with correct data ----
        Map<String, Object> pending = jdbc.queryForMap(
                "SELECT email, hospital_name, hospital_email, subscription_plan, submitted_at, token_expires_at, verified_at "
                        + "FROM pending_registrations WHERE email = ?", ADMIN_EMAIL);
        System.out.println("[DB] pending_registrations row: " + pending);
        assertThat(pending.get("hospital_name")).isEqualTo(HOSPITAL_NAME);
        assertThat(pending.get("subscription_plan")).isEqualTo("STANDARD");
        assertThat(pending.get("verified_at")).isNull();

        // ---- 4. Capture the raw verification token from the recorded email ----
        EmailMessage email = recordingEmailSender.lastMessage();
        assertThat(email).isNotNull();
        assertThat(email.to()).isEqualTo(ADMIN_EMAIL);
        Matcher matcher = TOKEN_PATTERN.matcher(email.textBody());
        assertThat(matcher.find()).as("verification link with token in email body").isTrue();
        String rawToken = matcher.group(1);

        // ---- 5. Click the verification link ----
        mockMvc.perform(get("/api/v1/verify-registration").param("token", rawToken))
                .andExpect(status().isOk());

        // ---- 6. PROOF: real records now exist with correct data + trialEndsAt from now ----
        Map<String, Object> tenant = jdbc.queryForMap(
                "SELECT name, email, subscription_plan, trial_ends_at FROM tenants WHERE email = ?",
                HOSPITAL_EMAIL);
        System.out.println("[DB] tenant created: " + tenant);
        assertThat(tenant.get("name")).isEqualTo(HOSPITAL_NAME);
        assertThat(tenant.get("subscription_plan")).isEqualTo("STANDARD");

        Instant trialEndsAt = ((java.sql.Timestamp) tenant.get("trial_ends_at")).toInstant();
        Instant now = Instant.now();
        Duration until = Duration.between(now, trialEndsAt);
        System.out.println("[DB] trial_ends_at = " + trialEndsAt + " (now = " + now + ", ~" + until.toHours() + "h ahead)");
        // trial must be ~14 days from the verification moment, not from the original submit.
        assertThat(until.toDays()).isBetween(13L, 15L);

        Map<String, Object> admin = jdbc.queryForMap(
                "SELECT email, first_name, last_name, email_verified FROM users WHERE email = ?",
                ADMIN_EMAIL);
        System.out.println("[DB] admin user created: " + admin);
        assertThat(admin.get("email_verified")).isEqualTo(true);

        // ---- 7. PROOF: pending record consumed/deleted ----
        Integer pendingAfter = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pending_registrations WHERE email = ?", Integer.class, ADMIN_EMAIL);
        System.out.println("[DB] pending_registrations rows after verify: " + pendingAfter);
        assertThat(pendingAfter).isZero();
    }

    @Test
    void expiredUnverifiedPendingIsCleanedUp() {
        // Create a real pending row via the application (Hibernate stores UTC consistently),
        // then fast-forward its expiry to the past using MySQL's UTC clock.
        String email = "phase7." + java.util.UUID.randomUUID().toString().substring(0, 8) + ".cleanup@test.local";
        pendingRegistrationService.submit(new com.healthcare.hms.auth.dto.request.RegistrationRequest(
                "Cleanup", "Pending", email, "StrongPass1!ab", null,
                "Cleanup Hospital", "cleanup.hospital@test.local", null, null,
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

    /** Records the last outbound email so the raw verification token can be read. */
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
