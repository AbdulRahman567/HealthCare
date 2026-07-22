import { authApi } from '@/features/auth/api/auth-api';
import { apiClient, refreshAccessToken } from '@/services/http/api-client';
import { authTokenStore } from '@/lib/auth-token';

jest.mock('@/services/http/api-client', () => ({
  apiClient: {
    post: jest.fn(),
    get: jest.fn(),
  },
  refreshAccessToken: jest.fn(),
}));

jest.mock('@/lib/auth-token', () => ({
  authTokenStore: {
    setTokens: jest.fn(),
    getRefreshToken: jest.fn(),
    getAccessToken: jest.fn(),
    clear: jest.fn(),
  },
}));

describe('authApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('login stores tokens', async () => {
    (apiClient.post as jest.Mock).mockResolvedValue({
      data: {
        success: true,
        message: 'ok',
        data: {
          accessToken: 'a',
          refreshToken: 'r',
          tokenType: 'Bearer',
          expiresInSeconds: 900,
          refreshExpiresInSeconds: 604800,
          user: { id: '1', email: 'a@b.com' },
        },
        timestamp: new Date().toISOString(),
      },
    });

    await authApi.login({ email: 'a@b.com', password: 'x' });
    expect(authTokenStore.setTokens).toHaveBeenCalledWith('a', 'r');
  });

  it('registerHospital returns hospital payload', async () => {
    const payload = {
      tenantId: 't1',
      tenantSlug: 'hospital',
      tenantStatus: 'PENDING',
      hospitalId: 'h1',
      hospitalName: 'Hospital',
      hospitalCode: 'HOSP',
      hospitalStatus: 'PENDING',
      defaultHospital: true,
      hospitalEmail: 'h@t.com',
      hospitalPhone: null,
      hospitalAddress: null,
      subscriptionPlan: 'BASIC',
      adminUserId: 'u1',
      adminEmail: 'admin@t.com',
      adminEmailVerified: false,
      provisionedRoles: ['HOSPITAL_ADMIN'],
      createdAt: '2026-01-01T00:00:00Z',
    };
    (apiClient.post as jest.Mock).mockResolvedValue({
      data: {
        success: true,
        message: 'created',
        data: payload,
        timestamp: '',
      },
    });
    await expect(
      authApi.registerHospital({
        hospitalName: 'Hospital',
        hospitalEmail: 'h@t.com',
        subscriptionPlan: 'BASIC',
        adminFirstName: 'Jane',
        adminLastName: 'Admin',
        adminEmail: 'admin@t.com',
        adminPassword: 'StrongPass1!ab',
      }),
    ).resolves.toEqual(payload);
    expect(apiClient.post).toHaveBeenCalledWith('/hospitals/register', expect.any(Object));
  });

  it('forgotPassword returns message', async () => {
    (apiClient.post as jest.Mock).mockResolvedValue({
      data: { success: true, message: 'sent', data: null, timestamp: '' },
    });
    await expect(authApi.forgotPassword({ email: 'a@b.com' })).resolves.toBe('sent');
  });

  it('resetPassword returns message', async () => {
    (apiClient.post as jest.Mock).mockResolvedValue({
      data: { success: true, message: 'reset', data: null, timestamp: '' },
    });
    await expect(
      authApi.resetPassword({ token: 'x'.repeat(32), newPassword: 'StrongPass1!ab' }),
    ).resolves.toBe('reset');
  });

  it('verifyEmail returns message', async () => {
    (apiClient.post as jest.Mock).mockResolvedValue({
      data: { success: true, message: 'verified', data: null, timestamp: '' },
    });
    await expect(authApi.verifyEmail({ token: 'x'.repeat(32) })).resolves.toBe('verified');
  });

  it('resendVerification returns message', async () => {
    (apiClient.post as jest.Mock).mockResolvedValue({
      data: { success: true, message: 'resent', data: null, timestamp: '' },
    });
    await expect(authApi.resendVerification({ email: 'a@b.com' })).resolves.toBe('resent');
  });

  it('refresh delegates to refreshAccessToken', async () => {
    (refreshAccessToken as jest.Mock).mockResolvedValue({ accessToken: 'a2' });
    await expect(authApi.refresh()).resolves.toEqual({ accessToken: 'a2' });
    expect(refreshAccessToken).toHaveBeenCalled();
  });

  it('getProfile returns user profile', async () => {
    (apiClient.get as jest.Mock).mockResolvedValue({
      data: {
        success: true,
        message: 'ok',
        data: { id: '1', email: 'a@b.com' },
        timestamp: '',
      },
    });
    await expect(authApi.getProfile()).resolves.toEqual({ id: '1', email: 'a@b.com' });
  });

  it('logout posts refresh token then clears store', async () => {
    (authTokenStore.getRefreshToken as jest.Mock).mockReturnValue('refresh-raw');
    (apiClient.post as jest.Mock).mockResolvedValue({
      data: { success: true, message: 'bye', data: null, timestamp: '' },
    });

    await authApi.logout();

    expect(apiClient.post).toHaveBeenCalledWith('/auth/logout', { refreshToken: 'refresh-raw' });
    expect(authTokenStore.clear).toHaveBeenCalled();
  });

  it('logout clears store even when request fails', async () => {
    (authTokenStore.getRefreshToken as jest.Mock).mockReturnValue('refresh-raw');
    (apiClient.post as jest.Mock).mockRejectedValue(new Error('network'));

    await expect(authApi.logout()).rejects.toThrow('network');
    expect(authTokenStore.clear).toHaveBeenCalled();
  });
});
