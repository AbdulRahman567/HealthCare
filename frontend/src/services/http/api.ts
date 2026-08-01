import type { AxiosRequestConfig } from 'axios';

import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse } from '@/types/api';

/**
 * Thin typed wrappers around {@link apiClient} that unwrap the shared
 * `ApiSuccessResponse` envelope (`{ success, message, data, timestamp }`)
 * and return the inner `data` payload directly.
 *
 * These centralise the repeated `const { data } = await apiClient.get<ApiSuccessResponse<T>>(...)`
 * pattern so feature API modules only describe the endpoint and its types.
 */

export async function apiGet<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const { data } = await apiClient.get<ApiSuccessResponse<T>>(url, config);
  return data.data;
}

export async function apiPost<T>(
  url: string,
  body?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const { data } = await apiClient.post<ApiSuccessResponse<T>>(url, body, config);
  return data.data;
}

export async function apiPut<T>(
  url: string,
  body?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const { data } = await apiClient.put<ApiSuccessResponse<T>>(url, body, config);
  return data.data;
}

export async function apiPatch<T>(
  url: string,
  body?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const { data } = await apiClient.patch<ApiSuccessResponse<T>>(url, body, config);
  return data.data;
}

export async function apiDelete(url: string, config?: AxiosRequestConfig): Promise<void> {
  await apiClient.delete(url, config);
}
