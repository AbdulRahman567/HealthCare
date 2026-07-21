import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';

import type { ApiSuccessResponse, AuthResponse } from '@/features/auth/types/auth.types';
import { emitAuthSessionInvalidated } from '@/lib/auth-events';
import { toApiClientError } from '@/lib/api-error';
import { authTokenStore } from '@/lib/auth-token';
import { env } from '@/lib/env';

type RetryConfig = InternalAxiosRequestConfig & { _retry?: boolean };

type QueueEntry = {
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
};

let isRefreshing = false;
let failedQueue: QueueEntry[] = [];

const refreshClient = axios.create({
  baseURL: env.NEXT_PUBLIC_API_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

function processQueue(error: unknown, token: string | null): void {
  failedQueue.forEach((entry) => {
    if (error || !token) {
      entry.reject(error ?? new Error('Token refresh failed'));
      return;
    }
    entry.resolve(token);
  });
  failedQueue = [];
}

function shouldSkipRefresh(url?: string): boolean {
  if (!url) {
    return false;
  }
  return (
    url.includes('/auth/login') ||
    url.includes('/auth/logout') ||
    url.includes('/auth/refresh-token') ||
    url.includes('/auth/register/') ||
    url.includes('/auth/forgot-password') ||
    url.includes('/auth/reset-password') ||
    url.includes('/auth/verify-email') ||
    url.includes('/auth/resend-verification')
  );
}

export async function refreshAccessToken(): Promise<AuthResponse> {
  const refreshToken = authTokenStore.getRefreshToken();
  if (!refreshToken) {
    throw toApiClientError(new Error('No refresh token available'));
  }

  const { data } = await refreshClient.post<ApiSuccessResponse<AuthResponse>>('/auth/refresh-token', {
    refreshToken,
  });

  authTokenStore.setTokens(data.data.accessToken, data.data.refreshToken);
  return data.data;
}

export const apiClient = axios.create({
  baseURL: env.NEXT_PUBLIC_API_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = authTokenStore.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryConfig | undefined;
    const status = error.response?.status;

    if (
      !originalRequest ||
      status !== 401 ||
      originalRequest._retry ||
      shouldSkipRefresh(originalRequest.url)
    ) {
      return Promise.reject(toApiClientError(error));
    }

    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return apiClient(originalRequest);
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const refreshed = await refreshAccessToken();
      processQueue(null, refreshed.accessToken);
      originalRequest.headers.Authorization = `Bearer ${refreshed.accessToken}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      authTokenStore.clear();
      emitAuthSessionInvalidated();
      return Promise.reject(toApiClientError(refreshError));
    } finally {
      isRefreshing = false;
    }
  },
);
