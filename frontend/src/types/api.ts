/**
 * Shared API envelopes aligned with backend {@code ApiResponse} / {@code PageResponse}.
 */

export type ApiSuccessResponse<T> = {
  success: true;
  message: string;
  data: T;
  timestamp: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type PageQuery = {
  page?: number;
  size?: number;
  sort?: string | string[];
};

export type ListQuery = PageQuery & {
  q?: string;
};
