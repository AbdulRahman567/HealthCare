package com.healthcare.hms.auth.service;

import com.healthcare.hms.auth.dto.request.ChangePasswordRequest;
import com.healthcare.hms.auth.dto.request.ForgotPasswordRequest;
import com.healthcare.hms.auth.dto.request.LoginRequest;
import com.healthcare.hms.auth.dto.request.RefreshTokenRequest;
import com.healthcare.hms.auth.dto.request.RegisterAdminRequest;
import com.healthcare.hms.auth.dto.request.ResendVerificationRequest;
import com.healthcare.hms.auth.dto.request.ResetPasswordRequest;
import com.healthcare.hms.auth.dto.request.UpdateProfileRequest;
import com.healthcare.hms.auth.dto.request.VerifyEmailRequest;
import com.healthcare.hms.auth.dto.response.AuthResponse;
import com.healthcare.hms.auth.dto.response.UserProfileResponse;
import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;

/**
 * Authentication and current-user profile operations.
 */
public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Delegates to {@link com.healthcare.hms.hospitals.service.HospitalRegistrationService}
     * for atomic Phase 2.5 onboarding. Prefer {@code POST /api/v1/hospitals/register}.
     */
    HospitalRegistrationResponse registerHospital(
            HospitalRegistrationRequest request,
            String ipAddress,
            String userAgent
    );

    /**
     * Legacy two-step admin registration — disabled (Phase 2.7).
     * Prefer atomic {@code POST /api/v1/hospitals/register}.
     */
    UserProfileResponse registerInitialAdmin(
            RegisterAdminRequest request,
            String ipAddress,
            String userAgent
    );

    AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent);

    void logout(String refreshToken, String ipAddress, String userAgent);

    UserProfileResponse getCurrentUser();

    UserProfileResponse updateProfile(UpdateProfileRequest request, String ipAddress, String userAgent);

    void changePassword(ChangePasswordRequest request, String ipAddress, String userAgent);

    void forgotPassword(ForgotPasswordRequest request, String ipAddress, String userAgent);

    void resetPassword(ResetPasswordRequest request, String ipAddress, String userAgent);

    void verifyEmail(VerifyEmailRequest request, String ipAddress, String userAgent);

    void resendVerification(ResendVerificationRequest request, String ipAddress, String userAgent);
}
