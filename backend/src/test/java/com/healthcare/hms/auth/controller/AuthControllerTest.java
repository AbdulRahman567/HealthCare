package com.healthcare.hms.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthcare.hms.auth.dto.request.ChangePasswordRequest;
import com.healthcare.hms.auth.dto.request.ForgotPasswordRequest;
import com.healthcare.hms.auth.dto.request.LoginRequest;
import com.healthcare.hms.auth.dto.request.LogoutRequest;
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
import com.healthcare.hms.auth.service.AuthService;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.hospitals.enums.SubscriptionPlan;
import com.healthcare.hms.hospitals.enums.TenantStatus;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.security.resolver.CurrentUserArgumentResolver;
import com.healthcare.hms.users.enums.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Standalone MockMvc tests for {@link AuthController} (no full Spring Security context).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .setMessageConverters(converter)
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /login returns tokens")
    void login() throws Exception {
        when(authService.login(any(LoginRequest.class), nullable(String.class), nullable(String.class)))
                .thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("admin@hospital.test", AuthTestData.STRONG_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /register/admin returns 201")
    void registerInitialAdmin() throws Exception {
        when(authService.registerInitialAdmin(
                        any(RegisterAdminRequest.class), nullable(String.class), nullable(String.class)))
                .thenReturn(sampleProfile());

        mockMvc.perform(post("/api/v1/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterAdminRequest(
                                AuthTestData.tenantId(),
                                "Jane",
                                "Admin",
                                "admin@hospital.test",
                                AuthTestData.STRONG_PASSWORD,
                                null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@hospital.test"));
    }

    @Test
    @DisplayName("POST /register/hospital returns 201")
    void registerHospital() throws Exception {
        when(authService.registerHospital(
                        any(RegisterHospitalRequest.class), nullable(String.class), nullable(String.class)))
                .thenReturn(new HospitalRegistrationResponse(
                        UUID.randomUUID(),
                        "Hospital",
                        "hospital",
                        "h@t.com",
                        null,
                        null,
                        SubscriptionPlan.BASIC,
                        TenantStatus.PENDING,
                        Instant.now()
                ));

        mockMvc.perform(post("/api/v1/auth/register/hospital")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterHospitalRequest(
                                        "Hospital", "h@t.com", null, null, SubscriptionPlan.BASIC))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /forgot-password returns generic success")
    void forgotPassword() throws Exception {
        doNothing().when(authService)
                .forgotPassword(any(ForgotPasswordRequest.class), nullable(String.class), nullable(String.class));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("a@b.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /reset-password succeeds")
    void resetPassword() throws Exception {
        doNothing().when(authService)
                .resetPassword(any(ResetPasswordRequest.class), nullable(String.class), nullable(String.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("t".repeat(32), AuthTestData.STRONG_PASSWORD))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /verify-email succeeds")
    void verifyEmail() throws Exception {
        doNothing().when(authService)
                .verifyEmail(any(VerifyEmailRequest.class), nullable(String.class), nullable(String.class));

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyEmailRequest("t".repeat(32)))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /resend-verification succeeds")
    void resendVerification() throws Exception {
        doNothing().when(authService)
                .resendVerification(
                        any(ResendVerificationRequest.class), nullable(String.class), nullable(String.class));

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResendVerificationRequest("a@b.com"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /refresh-token returns new tokens")
    void refreshToken() throws Exception {
        when(authService.refreshToken(
                        any(RefreshTokenRequest.class), nullable(String.class), nullable(String.class)))
                .thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("refresh-raw"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("POST /logout with refresh token succeeds")
    void logoutWithToken() throws Exception {
        authenticate();
        doNothing().when(authService).logout(nullable(String.class), nullable(String.class), nullable(String.class));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LogoutRequest("refresh-raw"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(authService).logout(
                nullable(String.class), nullable(String.class), nullable(String.class));
    }

    @Test
    @DisplayName("POST /logout without body succeeds")
    void logoutWithoutBody() throws Exception {
        authenticate();
        doNothing().when(authService).logout(nullable(String.class), nullable(String.class), nullable(String.class));

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /authorization returns principal context")
    void authorizationContext() throws Exception {
        authenticate();

        mockMvc.perform(get("/api/v1/auth/authorization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@hospital.test"))
                .andExpect(jsonPath("$.data.roles[0]").value("HOSPITAL_ADMIN"));
    }

    @Test
    @DisplayName("GET /authorization/hospital-access confirms permission")
    void hospitalAccess() throws Exception {
        authenticate();

        mockMvc.perform(get("/api/v1/auth/authorization/hospital-access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));
    }

    @Test
    @DisplayName("GET /profile returns user profile")
    void profile() throws Exception {
        authenticate();
        when(authService.getCurrentUser()).thenReturn(sampleProfile());

        mockMvc.perform(get("/api/v1/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@hospital.test"));
    }

    @Test
    @DisplayName("PUT /profile updates profile")
    void updateProfile() throws Exception {
        authenticate();
        when(authService.updateProfile(
                        any(UpdateProfileRequest.class), nullable(String.class), nullable(String.class)))
                .thenReturn(sampleProfile());

        mockMvc.perform(put("/api/v1/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateProfileRequest("Jane", "Admin", null))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /change-password succeeds")
    void changePassword() throws Exception {
        authenticate();
        doNothing().when(authService)
                .changePassword(any(ChangePasswordRequest.class), nullable(String.class), nullable(String.class));

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(
                                        AuthTestData.STRONG_PASSWORD, AuthTestData.STRONG_PASSWORD_ALT))))
                .andExpect(status().isOk());
        verify(authService)
                .changePassword(any(ChangePasswordRequest.class), nullable(String.class), nullable(String.class));
    }

    private static void authenticate() {
        final AuthenticatedUser principal = new AuthenticatedUser(
                AuthTestData.userId(),
                AuthTestData.tenantId(),
                "admin@hospital.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("HOSPITAL_READ"),
                0L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private static AuthResponse sampleAuthResponse() {
        return new AuthResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900,
                604800,
                sampleProfile()
        );
    }

    private static UserProfileResponse sampleProfile() {
        return new UserProfileResponse(
                AuthTestData.userId(),
                AuthTestData.tenantId(),
                "Jane",
                "Admin",
                "admin@hospital.test",
                null,
                true,
                Instant.now(),
                UserStatus.ACTIVE,
                Set.of("HOSPITAL_ADMIN"),
                Set.of("HOSPITAL_READ"),
                null,
                Instant.now()
        );
    }
}
