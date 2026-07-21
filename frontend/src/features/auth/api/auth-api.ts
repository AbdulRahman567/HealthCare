import { apiClient, refreshAccessToken } from '@/services/http/api-client';
import type {
  ApiSuccessResponse,
  AuthResponse,
  ForgotPasswordPayload,
  HospitalRegistrationResponse,
  LoginPayload,
  RegisterHospitalPayload,
  ResendVerificationPayload,
  ResetPasswordPayload,
  UserProfile,
  VerifyEmailPayload,
} from '@/features/auth/types/auth.types';
import { authTokenStore } from '@/lib/auth-token';

export const authApi = {
  async login(payload: LoginPayload): Promise<AuthResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<AuthResponse>>('/auth/login', payload);
    authTokenStore.setTokens(data.data.accessToken, data.data.refreshToken);
    return data.data;
  },

  async registerHospital(payload: RegisterHospitalPayload): Promise<HospitalRegistrationResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<HospitalRegistrationResponse>>(
      '/auth/register/hospital',
      payload,
    );
    return data.data;
  },

  async forgotPassword(payload: ForgotPasswordPayload): Promise<string> {
    const { data } = await apiClient.post<ApiSuccessResponse<null>>('/auth/forgot-password', payload);
    return data.message;
  },

  async resetPassword(payload: ResetPasswordPayload): Promise<string> {
    const { data } = await apiClient.post<ApiSuccessResponse<null>>('/auth/reset-password', payload);
    return data.message;
  },

  async verifyEmail(payload: VerifyEmailPayload): Promise<string> {
    const { data } = await apiClient.post<ApiSuccessResponse<null>>('/auth/verify-email', payload);
    return data.message;
  },

  async resendVerification(payload: ResendVerificationPayload): Promise<string> {
    const { data } = await apiClient.post<ApiSuccessResponse<null>>('/auth/resend-verification', payload);
    return data.message;
  },

  async refresh(): Promise<AuthResponse> {
    return refreshAccessToken();
  },

  async getProfile(): Promise<UserProfile> {
    const { data } = await apiClient.get<ApiSuccessResponse<UserProfile>>('/auth/profile');
    return data.data;
  },

  async logout(): Promise<void> {
    const refreshToken = authTokenStore.getRefreshToken();
    try {
      await apiClient.post<ApiSuccessResponse<null>>('/auth/logout', { refreshToken });
    } finally {
      authTokenStore.clear();
    }
  },
};
