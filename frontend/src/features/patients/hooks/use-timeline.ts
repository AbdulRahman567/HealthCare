import { useInfiniteQuery } from '@tanstack/react-query';

import { timelineApi } from '@/features/patients/api/timeline-api';
import type { TimelineQuery } from '@/features/patients/types/timeline';
import { patientKeys } from '@/features/patients/hooks/use-patients';

export const timelineKeys = {
  all: (patientId: string) => [...patientKeys.detail(patientId), 'timeline'] as const,
  feed: (patientId: string, query: Omit<TimelineQuery, 'cursor'>) =>
    [...timelineKeys.all(patientId), query] as const,
};

export function useTimelineInfiniteQuery(
  patientId: string,
  query: Omit<TimelineQuery, 'cursor'> = {},
  enabled = true,
) {
  return useInfiniteQuery({
    queryKey: timelineKeys.feed(patientId, query),
    queryFn: ({ pageParam }) =>
      timelineApi.list(patientId, {
        ...query,
        cursor: pageParam,
      }),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? (lastPage.nextCursor ?? undefined) : undefined,
    enabled: enabled && Boolean(patientId),
  });
}
