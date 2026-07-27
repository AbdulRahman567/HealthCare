import { apiClient } from '@/services/http/api-client';
import type {
  CalendarMonthQuery,
  CalendarMonthResponse,
  CalendarQuery,
  CalendarRangeResponse,
} from '@/features/appointments/types/calendar';
import { toPageParams } from '@/lib/page-query';
import type { ApiSuccessResponse } from '@/types/api';

type ScopeKind = 'doctors' | 'departments' | 'hospitals';

function rangePath(scope: ScopeKind, scopeId: string, view: 'daily' | 'weekly'): string {
  return `/calendars/${scope}/${scopeId}/${view}`;
}

export const calendarApi = {
  async daily(
    scope: ScopeKind,
    scopeId: string,
    query: CalendarQuery = {},
  ): Promise<CalendarRangeResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<CalendarRangeResponse>>(
      rangePath(scope, scopeId, 'daily'),
      { params: toPageParams({ size: 50, ...query }) },
    );
    return data.data;
  },

  async weekly(
    scope: ScopeKind,
    scopeId: string,
    query: CalendarQuery = {},
  ): Promise<CalendarRangeResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<CalendarRangeResponse>>(
      rangePath(scope, scopeId, 'weekly'),
      { params: toPageParams({ size: 100, ...query }) },
    );
    return data.data;
  },

  async monthly(
    scope: ScopeKind,
    scopeId: string,
    query: CalendarMonthQuery,
  ): Promise<CalendarMonthResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<CalendarMonthResponse>>(
      `/calendars/${scope}/${scopeId}/monthly`,
      { params: query },
    );
    return data.data;
  },
};
