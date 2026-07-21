import { isAxiosError } from 'axios';

import type { ApiErrorBody, ApiErrorDetail } from '@/features/auth/types/auth.types';

export class ApiClientError extends Error {
  readonly status: number;
  readonly errorCode?: string;
  readonly details: ApiErrorDetail[];

  constructor(message: string, status: number, errorCode?: string, details: ApiErrorDetail[] = []) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.errorCode = errorCode;
    this.details = details;
  }
}

export function getErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (error instanceof ApiClientError) {
    return error.message;
  }

  if (isAxiosError(error)) {
    const data = error.response?.data as ApiErrorBody | undefined;
    if (data?.message) {
      return data.message;
    }
    if (error.message) {
      return error.message;
    }
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallback;
}

export function toApiClientError(error: unknown): ApiClientError {
  if (error instanceof ApiClientError) {
    return error;
  }

  if (isAxiosError(error)) {
    const data = error.response?.data as ApiErrorBody | undefined;
    return new ApiClientError(
      data?.message ?? error.message ?? 'Request failed',
      error.response?.status ?? 500,
      data?.errorCode,
      data?.errors ?? [],
    );
  }

  if (error instanceof Error) {
    return new ApiClientError(error.message, 500);
  }

  return new ApiClientError('Unexpected error', 500);
}
