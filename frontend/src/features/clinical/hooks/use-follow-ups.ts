import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { followUpsApi } from '@/features/clinical/api/follow-ups-api';
import { consultationKeys } from '@/features/clinical/hooks/use-consultations';
import type {
  CreateFollowUpPayload,
  FollowUpSearchQuery,
  UpdateFollowUpPayload,
  UpdateFollowUpStatusPayload,
} from '@/features/clinical/types/follow-up';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const followUpKeys = {
  all: ['follow-ups'] as const,
  consultation: (consultationId: string) =>
    [...consultationKeys.detail(consultationId), 'follow-ups'] as const,
  list: (consultationId: string) =>
    [...followUpKeys.consultation(consultationId), 'list'] as const,
  search: (query: FollowUpSearchQuery) => [...followUpKeys.all, 'search', query] as const,
  due: (withinDays: number) => [...followUpKeys.all, 'due', withinDays] as const,
};

export function useConsultationFollowUpsQuery(consultationId: string, enabled = true) {
  return useQuery({
    queryKey: followUpKeys.list(consultationId),
    queryFn: () => followUpsApi.listForConsultation(consultationId),
    enabled: enabled && Boolean(consultationId),
  });
}

export function useFollowUpsSearchQuery(query: FollowUpSearchQuery, enabled = true) {
  return useQuery({
    queryKey: followUpKeys.search(query),
    queryFn: () => followUpsApi.search(query),
    enabled,
    placeholderData: (previous) => previous,
  });
}

export function useFollowUpsDueQuery(withinDays = 14, enabled = true) {
  return useQuery({
    queryKey: followUpKeys.due(withinDays),
    queryFn: () => followUpsApi.due(withinDays),
    enabled,
  });
}

export function useFollowUpMutations(consultationId: string, patientId?: string) {
  const queryClient = useQueryClient();
  const invalidate = () => {
    void queryClient.invalidateQueries({
      queryKey: followUpKeys.consultation(consultationId),
    });
    void queryClient.invalidateQueries({ queryKey: followUpKeys.all });
    if (patientId) {
      void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });
    }
  };

  return {
    create: useMutation({
      mutationFn: (payload: CreateFollowUpPayload) =>
        followUpsApi.create(consultationId, payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationFn: ({ id, payload }: { id: string; payload: UpdateFollowUpPayload }) =>
        followUpsApi.update(consultationId, id, payload),
      onSuccess: invalidate,
    }),
    updateStatus: useMutation({
      mutationFn: ({ id, payload }: { id: string; payload: UpdateFollowUpStatusPayload }) =>
        followUpsApi.updateStatus(consultationId, id, payload),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationFn: (id: string) => followUpsApi.remove(consultationId, id),
      onSuccess: invalidate,
    }),
  };
}
