package com.healthcare.hms.auth.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hms.auth.dto.request.LoginRequest;
import com.healthcare.hms.auth.dto.request.VerifyEmailRequest;
import com.healthcare.hms.auth.service.EmailVerificationService;
import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import com.healthcare.hms.support.AbstractMySqlIntegrationTest;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Verifies that authentication-gated auth endpoints reject anonymous requests
 * and accept requests carrying a valid bearer access token.
 */
class ProtectedRouteIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final String BASE = "/api/v1/auth";
    private static final String STRONG_PASSWORD = "StrongPass1!ab";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /profile without a bearer token returns 401")
    void getProfile_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /profile with an invalid bearer token returns 401")
    void getProfile_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /profile with a valid bearer token returns 200")
    void getProfile_withValidToken_returns200() throws Exception {
        final String accessToken = registerVerifyAndLogin(uniqueEmail("protected-profile"));

        mockMvc.perform(get(BASE + "/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("GET /authorization without a bearer token returns 401")
    void getAuthorization_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/authorization"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /authorization with a valid bearer token returns 200 with authorization context")
    void getAuthorization_withValidToken_returns200() throws Exception {
        final String accessToken = registerVerifyAndLogin(uniqueEmail("protected-authz"));

        mockMvc.perform(get(BASE + "/authorization")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.roles", hasItem("HOSPITAL_ADMIN")))
                .andExpect(jsonPath("$.data.permissions", hasItem("HOSPITAL_READ")));
    }

    @Test
    @DisplayName("POST /change-password without a bearer token returns 401")
    void changePassword_withoutToken_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"StrongPass1!ab","newPassword":"StrongPass2!cd"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /hospitals/settings without a bearer token returns 401")
    void hospitalSettings_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/settings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /authorization/hospital-access with admin token returns 200")
    void hospitalAccess_withAdminToken_returns200() throws Exception {
        final String accessToken = registerVerifyAndLogin(uniqueEmail("hospital-access"));

        mockMvc.perform(get(BASE + "/authorization/hospital-access")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.permission").value("HOSPITAL_READ"));
    }

    @Test
    @DisplayName("GET /hospitals/settings with mismatched X-Tenant-ID returns 403")
    void hospitalSettings_tenantMismatch_returns403() throws Exception {
        final String accessToken = registerVerifyAndLogin(uniqueEmail("tenant-mismatch"));

        mockMvc.perform(get("/api/v1/hospitals/settings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .header("X-Tenant-ID", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // Test fixtures and MockMvc helpers
    // ------------------------------------------------------------------

    private static String uniqueEmail(final String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@hms-test.local";
    }

    private ResultActions postJson(final String uri, final Object body) throws Exception {
        return mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private JsonNode dataOf(final MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }

    /**
     * Registers a hospital and its initial admin, verifies the admin's email using a
     * directly-issued verification token, logs in, and returns a valid access token.
     */
    private String registerVerifyAndLogin(final String emailPrefix) throws Exception {
        final String hospitalEmail = uniqueEmail(emailPrefix + "-hospital");
        final String email = uniqueEmail(emailPrefix);
        final HospitalRegistrationRequest hospitalRequest = new HospitalRegistrationRequest(
                "Test Hospital " + UUID.randomUUID(),
                hospitalEmail,
                "+1-555-0100",
                "123 Main St, Testville",
                SubscriptionPlan.BASIC,
                "Jane",
                "Admin",
                email,
                STRONG_PASSWORD,
                "+1-555-0101"
        );
        postJson(BASE + "/register/hospital", hospitalRequest)
                .andExpect(status().isCreated());

        final User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Test user not found: " + email));
        final String rawVerificationToken =
                emailVerificationService.issueVerificationToken(user, "127.0.0.1", "JUnit-Test");

        postJson(BASE + "/verify-email", new VerifyEmailRequest(rawVerificationToken))
                .andExpect(status().isOk());

        final MvcResult loginResult = postJson(BASE + "/login", new LoginRequest(email, STRONG_PASSWORD))
                .andExpect(status().isOk())
                .andReturn();

        return dataOf(loginResult).get("accessToken").asText();
    }
}
