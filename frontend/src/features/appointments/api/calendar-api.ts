import { apiGet } from '@/services/http/api';
import type {
  CalendarMonthQuery,
  CalendarMonthResponse,
  CalendarQuery,
  CalendarRangeResponse,
} from '@/features/appointments/types/calendar';
import { toPageParams } from '@/lib/page-query';

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
    return apiGet<CalendarRangeResponse>(rangePath(scope, scopeId, 'daily'), {
      params: toPageParams({ size: 50, ...query }),
    });
  },

  async weekly(
    scope: ScopeKind,
    scopeId: string,
    query: CalendarQuery = {},
  ): Promise<CalendarRangeResponse> {
    return apiGet<CalendarRangeResponse>(rangePath(scope, scopeId, 'weekly'), {
      params: toPageParams({ size: 100, ...query }),
    });
  },

  async monthly(
    scope: ScopeKind,
    scopeId: string,
    query: CalendarMonthQuery,
  ): Promise<CalendarMonthResponse> {
    return apiGet<CalendarMonthResponse>(`/calendars/${scope}/${scopeId}/monthly`, {
      params: query,
    });
  },
};
