package com.healthcare.hms.auth.service;

import com.healthcare.hms.auth.dto.request.ChangePasswordRequest;
import com.healthcare.hms.auth.dto.request.ForgotPasswordRequest;
import com.healthcare.hms.auth.dto.request.LoginRequest;
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

/**
 * Authentication and current-user profile operations.
 */
public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    HospitalRegistrationResponse registerHospital(
            RegisterHospitalRequest request,
            String ipAddress,
            String userAgent
    );

    /**
     * Registers the initial hospital admin and sends a verification email.
     * Tokens are not issued until the email is verified and the user signs in.
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
