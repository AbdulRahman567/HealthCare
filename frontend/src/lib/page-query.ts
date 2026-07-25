import type { PageQuery } from '@/types/api';

/**
 * Builds Axios/query-string params for Spring Data pageable list endpoints.
 */
export function toPageParams(query: PageQuery & Record<string, unknown>): Record<string, unknown> {
  const params: Record<string, unknown> = {};

  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null || value === '') {
      continue;
    }
    params[key] = value;
  }

  if (params.page === undefined) {
    params.page = 0;
  }
  if (params.size === undefined) {
    params.size = 20;
  }

  return params;
}

export function formatPersonName(firstName?: string | null, lastName?: string | null): string {
  return [firstName, lastName].filter(Boolean).join(' ').trim() || '—';
}

export function formatEnumLabel(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}
