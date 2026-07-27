import { useQuery } from '@tanstack/react-query';

import { calendarApi } from '@/features/appointments/api/calendar-api';
import type { CalendarMonthQuery, CalendarQuery } from '@/features/appointments/types/calendar';
import type { CalendarScope } from '@/features/appointments/types/enums';

export type CalendarScopeKind = 'doctors' | 'departments' | 'hospitals';

export function toCalendarScopeKind(scope: CalendarScope): CalendarScopeKind {
  switch (scope) {
    case 'DOCTOR':
      return 'doctors';
    case 'DEPARTMENT':
      return 'departments';
    case 'HOSPITAL':
      return 'hospitals';
  }
}

export const calendarKeys = {
  all: ['calendars'] as const,
  daily: (scope: CalendarScopeKind, scopeId: string, query: CalendarQuery) =>
    [...calendarKeys.all, 'daily', scope, scopeId, query] as const,
  weekly: (scope: CalendarScopeKind, scopeId: string, query: CalendarQuery) =>
    [...calendarKeys.all, 'weekly', scope, scopeId, query] as const,
  monthly: (scope: CalendarScopeKind, scopeId: string, query: CalendarMonthQuery) =>
    [...calendarKeys.all, 'monthly', scope, scopeId, query] as const,
};

export function useCalendarDailyQuery(
  scope: CalendarScopeKind,
  scopeId: string,
  query: CalendarQuery,
  enabled = true,
) {
  return useQuery({
    queryKey: calendarKeys.daily(scope, scopeId, query),
    queryFn: () => calendarApi.daily(scope, scopeId, query),
    enabled: enabled && Boolean(scopeId),
    placeholderData: (previous) => previous,
  });
}

export function useCalendarWeeklyQuery(
  scope: CalendarScopeKind,
  scopeId: string,
  query: CalendarQuery,
  enabled = true,
) {
  return useQuery({
    queryKey: calendarKeys.weekly(scope, scopeId, query),
    queryFn: () => calendarApi.weekly(scope, scopeId, query),
    enabled: enabled && Boolean(scopeId),
    placeholderData: (previous) => previous,
  });
}

export function useCalendarMonthlyQuery(
  scope: CalendarScopeKind,
  scopeId: string,
  query: CalendarMonthQuery,
  enabled = true,
) {
  return useQuery({
    queryKey: calendarKeys.monthly(scope, scopeId, query),
    queryFn: () => calendarApi.monthly(scope, scopeId, query),
    enabled: enabled && Boolean(scopeId) && query.year > 0 && query.month >= 1 && query.month <= 12,
    placeholderData: (previous) => previous,
  });
}
