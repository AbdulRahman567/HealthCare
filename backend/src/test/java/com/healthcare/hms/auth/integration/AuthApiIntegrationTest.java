package com.healthcare.hms.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hms.auth.dto.request.ChangePasswordRequest;
import com.healthcare.hms.auth.dto.request.ForgotPasswordRequest;
import com.healthcare.hms.auth.dto.request.LoginRequest;
import com.healthcare.hms.auth.dto.request.RefreshTokenRequest;
import com.healthcare.hms.auth.dto.request.RegisterAdminRequest;
import com.healthcare.hms.auth.dto.request.RegisterHospitalRequest;
import com.healthcare.hms.auth.dto.request.ResetPasswordRequest;
import com.healthcare.hms.auth.dto.request.UpdateProfileRequest;
import com.healthcare.hms.auth.dto.request.VerifyEmailRequest;
import com.healthcare.hms.auth.service.EmailVerificationService;
import com.healthcare.hms.auth.service.PasswordResetService;
import com.healthcare.hms.hospitals.enums.SubscriptionPlan;
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
 * End-to-end coverage of the authentication API surface backed by a real MySQL
 * Testcontainers instance: hospital/admin onboarding, email verification, login,
 * refresh-token rotation, profile management, password lifecycle, and authorization.
 */
class AuthApiIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final String BASE = "/api/v1/auth";
    private static final String STRONG_PASSWORD = "StrongPass1!ab";
    private static final String STRONG_PASSWORD_ALT = "StrongPass2!cd";
    private static final String STRONG_PASSWORD_RESET = "StrongPass3!ef";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /register/hospital returns 201 and persists the tenant")
    void registerHospital_success_returns201WithTenant() throws Exception {
        final String email = uniqueEmail("hospital");
        final RegisterHospitalRequest request = new RegisterHospitalRequest(
                "Test Hospital " + UUID.randomUUID(),
                email,
                "+1-555-0100",
                "123 Main St, Testville",
                SubscriptionPlan.BASIC
        );

        postJson(BASE + "/register/hospital", request)
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Hospital registered successfully"))
                .andExpect(jsonPath("$.data.tenantId").exists())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.slug").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.subscriptionPlan").value("BASIC"));
    }

    @Test
    @DisplayName("POST /register/admin returns 201 with a profile payload, not tokens")
    void registerAdmin_success_returns201WithProfileOnly() throws Exception {
        final UUID tenantId = registerHospital(uniqueEmail("hospital-for-admin"));
        final String adminEmail = uniqueEmail("admin");
        final RegisterAdminRequest request = new RegisterAdminRequest(
                tenantId, "Jane", "Admin", adminEmail, STRONG_PASSWORD, "+1-555-0101"
        );

        postJson(BASE + "/register/admin", request)
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(adminEmail))
                .andExpect(jsonPath("$.data.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.data.emailVerified").value(false))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.roles", hasItem("HOSPITAL_ADMIN")))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("POST /login before email verification returns 401 EMAIL_NOT_VERIFIED")
    void login_beforeEmailVerified_returns401EmailNotVerified() throws Exception {
        final AdminAccount admin = registerUnverifiedAdmin(uniqueEmail("pending"));

        postJson(BASE + "/login", new LoginRequest(admin.email(), admin.password()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_NOT_VERIFIED"));
    }

    @Test
    @DisplayName("Verify email with a valid token, then login succeeds with access and refresh tokens")
    void verifyEmail_thenLogin_succeedsWithTokens() throws Exception {
        final AdminAccount admin = registerUnverifiedAdmin(uniqueEmail("verify"));
        final String rawToken = issueVerificationToken(admin.email());

        postJson(BASE + "/verify-email", new VerifyEmailRequest(rawToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully"));

        postJson(BASE + "/login", new LoginRequest(admin.email(), admin.password()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresInSeconds").isNumber())
                .andExpect(jsonPath("$.data.user.email").value(admin.email()))
                .andExpect(jsonPath("$.data.user.emailVerified").value(true))
                .andExpect(jsonPath("$.data.user.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /login with wrong password returns 401 INVALID_CREDENTIALS")
    void login_wrongPassword_returns401InvalidCredentials() throws Exception {
        final AdminAccount admin = registerAndVerifyAdmin(uniqueEmail("wrongpass"));

        postJson(BASE + "/login", new LoginRequest(admin.email(), "IncorrectPass1!zz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("POST /refresh-token rotates tokens and rejects reuse of the old refresh token")
    void refreshToken_rotatesTokens_andRejectsReuseOfOldToken() throws Exception {
        final AdminAccount admin = registerAndVerifyAdmin(uniqueEmail("refresh"));
        final AuthTokens initialTokens = login(admin.email(), admin.password());

        final MvcResult refreshResult = postJson(
                BASE + "/refresh-token",
                new RefreshTokenRequest(initialTokens.refreshToken())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        final JsonNode rotatedData = dataOf(refreshResult);
        final String newRefreshToken = rotatedData.get("refreshToken").asText();
        assertThat(newRefreshToken).isNotEqualTo(initialTokens.refreshToken());
        assertThat(rotatedData.get("accessToken").asText()).isNotEqualTo(initialTokens.accessToken());

        postJson(BASE + "/refresh-token", new RefreshTokenRequest(initialTokens.refreshToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_TOKEN"));
    }

    @Test
    @DisplayName("GET /profile with a valid bearer token returns 200 with the current user")
    void getProfile_withBearerToken_returns200() throws Exception {
        final AdminAccount admin = registerAndVerifyAdmin(uniqueEmail("profile"));
        final AuthTokens tokens = login(admin.email(), admin.password());

        authorizedGet(BASE + "/profile", tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(admin.email()))
                .andExpect(jsonPath("$.data.firstName").value(admin.firstName()))
                .andExpect(jsonPath("$.data.roles", hasItem("HOSPITAL_ADMIN")));
    }

    @Test
    @DisplayName("PUT /profile updates and persists the current user's profile")
    void updateProfile_returns200AndPersistsChanges() throws Exception {
        final AdminAccount admin = registerAndVerifyAdmin(uniqueEmail("updateprofile"));
        final AuthTokens tokens = login(admin.email(), admin.password());

        final UpdateProfileRequest updateRequest = new UpdateProfileRequest("Janet", "Updated", "+1-555-0199");

        mockMvc.perform(put(BASE + "/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokens.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Janet"))
                .andExpect(jsonPath("$.data.lastName").value("Updated"))
                .andExpect(jsonPath("$.data.phone").value("+1-555-0199"));

        authorizedGet(BASE + "/profile", tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Janet"))
                .andExpect(jsonPath("$.data.lastName").value("Updated"));
    }

    @Test
    @DisplayName("POST /change-password succeeds, revokes prior sessions, and allows login with the new password")
    void changePassword_succeeds_revokesOldSessions_allowsLoginWithNewPassword() throws Exception {
        final AdminAccount admin = registerAndVerifyAdmin(uniqueEmail("changepass"));
        final AuthTokens tokens = login(admin.email(), admin.password());

        mockMvc.perform(post(BASE + "/change-password")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokens.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(admin.password(), STRONG_PASSWORD_ALT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        // Old refresh token was revoked as part of the password change.
        postJson(BASE + "/refresh-token", new RefreshTokenRequest(tokens.refreshToken()))
                .andExpect(status().isUnauthorized());

        // Old password no longer works.
        postJson(BASE + "/login", new LoginRequest(admin.email(), admin.password()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));

        // New password works.
        postJson(BASE + "/login", new LoginRequest(admin.email(), STRONG_PASSWORD_ALT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /forgot-password always returns 200 for both known and unknown emails")
    void forgotPassword_alwaysReturns200_forKnownAndUnknownEmail() throws Exception {
        final AdminAccount admin = registerAndVerifyAdmin(uniqueEmail("forgot"));

        postJson(BASE + "/forgot-password", new ForgotPasswordRequest(admin.email()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        postJson(BASE + "/forgot-password", new ForgotPasswordRequest(uniqueEmail("does-not-exist")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /reset-password with an issued token succeeds and allows login with the new password")
    void resetPassword_withIssuedToken_returns200AndLoginWithNewPasswordSucceeds() throws Exception {
        final AdminAccount admin = registerAndVerifyAdmin(uniqueEmail("resetpass"));
        final String rawResetToken = issueResetToken(admin.email());

        postJson(BASE + "/reset-password", new ResetPasswordRequest(rawResetToken, STRONG_PASSWORD_RESET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        postJson(BASE + "/login", new LoginRequest(admin.email(), admin.password()))
                .andExpect(status().isUnauthorized());

        postJson(BASE + "/login", new LoginRequest(admin.email(), STRONG_PASSWORD_RESET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Protected route without a bearer token returns 401")
    void protectedRoute_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /authorization with a valid token returns 200 with roles and permissions")
    void authorizationEndpoint_withValidToken_returns200WithRolesAndPermissions() throws Exception {
        final AdminAccount admin = registerAndVerifyAdmin(uniqueEmail("authz"));
        final AuthTokens tokens = login(admin.email(), admin.password());

        authorizedGet(BASE + "/authorization", tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(admin.email()))
                .andExpect(jsonPath("$.data.tenantId").value(admin.tenantId().toString()))
                .andExpect(jsonPath("$.data.roles", hasItem("HOSPITAL_ADMIN")))
                .andExpect(jsonPath("$.data.permissions", hasItem("HOSPITAL_READ")))
                .andExpect(jsonPath("$.data.samplePermission").value("USER_READ"));
    }

    // ------------------------------------------------------------------
    // Test fixtures and MockMvc helpers
    // ------------------------------------------------------------------

    private record AdminAccount(UUID tenantId, String email, String password, String firstName, String lastName) {
    }

    private record AuthTokens(String accessToken, String refreshToken) {
    }

    private static String uniqueEmail(final String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@hms-test.local";
    }

    private static String bearer(final String accessToken) {
        return "Bearer " + accessToken;
    }

    private ResultActions postJson(final String uri, final Object body) throws Exception {
        return mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions authorizedGet(final String uri, final String accessToken) throws Exception {
        return mockMvc.perform(get(uri).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)));
    }

    private JsonNode dataOf(final MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }

    private UUID registerHospital(final String emailPrefix) throws Exception {
        final RegisterHospitalRequest request = new RegisterHospitalRequest(
                "Test Hospital " + UUID.randomUUID(),
                uniqueEmail(emailPrefix),
                "+1-555-0100",
                "123 Main St, Testville",
                SubscriptionPlan.BASIC
        );

        final MvcResult result = postJson(BASE + "/register/hospital", request)
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(dataOf(result).get("tenantId").asText());
    }

    private AdminAccount registerUnverifiedAdmin(final String emailPrefix) throws Exception {
        final UUID tenantId = registerHospital(emailPrefix + "-hospital");
        final String email = uniqueEmail(emailPrefix);
        final RegisterAdminRequest request = new RegisterAdminRequest(
                tenantId, "Jane", "Admin", email, STRONG_PASSWORD, "+1-555-0101"
        );

        postJson(BASE + "/register/admin", request)
                .andExpect(status().isCreated());

        return new AdminAccount(tenantId, email, STRONG_PASSWORD, "Jane", "Admin");
    }

    private AdminAccount registerAndVerifyAdmin(final String emailPrefix) throws Exception {
        final AdminAccount admin = registerUnverifiedAdmin(emailPrefix);
        final String rawToken = issueVerificationToken(admin.email());

        postJson(BASE + "/verify-email", new VerifyEmailRequest(rawToken))
                .andExpect(status().isOk());

        return admin;
    }

    private String issueVerificationToken(final String email) {
        final User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Test user not found: " + email));
        return emailVerificationService.issueVerificationToken(user, "127.0.0.1", "JUnit-Test");
    }

    private String issueResetToken(final String email) {
        final User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Test user not found: " + email));
        return passwordResetService.issueResetToken(user, "127.0.0.1", "JUnit-Test");
    }

    private AuthTokens login(final String email, final String password) throws Exception {
        final MvcResult result = postJson(BASE + "/login", new LoginRequest(email, password))
                .andExpect(status().isOk())
                .andReturn();

        final JsonNode data = dataOf(result);
        return new AuthTokens(data.get("accessToken").asText(), data.get("refreshToken").asText());
    }
}
