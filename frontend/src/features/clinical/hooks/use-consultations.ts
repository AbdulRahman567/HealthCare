import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { consultationsApi } from '@/features/clinical/api/consultations-api';
import type {
  CompleteConsultationPayload,
  ConsultationListQuery,
  CreateConsultationPayload,
  UpdateConsultationDocumentationPayload,
} from '@/features/clinical/types/consultation';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const consultationKeys = {
  all: ['consultations'] as const,
  lists: () => [...consultationKeys.all, 'list'] as const,
  list: (query: ConsultationListQuery) => [...consultationKeys.lists(), query] as const,
  details: () => [...consultationKeys.all, 'detail'] as const,
  detail: (id: string) => [...consultationKeys.details(), id] as const,
};

export function useConsultationsQuery(query: ConsultationListQuery, enabled = true) {
  return useQuery({
    queryKey: consultationKeys.list(query),
    queryFn: () => consultationsApi.search(query),
    enabled,
    placeholderData: (previous) => previous,
  });
}

export function useConsultationQuery(id: string, enabled = true) {
  return useQuery({
    queryKey: consultationKeys.detail(id),
    queryFn: () => consultationsApi.getById(id),
    enabled: enabled && Boolean(id),
  });
}

export function useConsultationMutations() {
  const queryClient = useQueryClient();

  const invalidateAll = () => queryClient.invalidateQueries({ queryKey: consultationKeys.all });
  const invalidateDetail = (id: string) =>
    queryClient.invalidateQueries({ queryKey: consultationKeys.detail(id) });
  const invalidateTimeline = (patientId: string) =>
    void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });

  return {
    create: useMutation({
      mutationKey: [...consultationKeys.all, 'create'],
      mutationFn: (payload: CreateConsultationPayload) => consultationsApi.create(payload),
      onSuccess: (data) => {
        invalidateAll();
        invalidateTimeline(data.patientId);
      },
    }),
    updateDocumentation: useMutation({
      mutationKey: [...consultationKeys.all, 'documentation'],
      mutationFn: ({
        id,
        payload,
      }: {
        id: string;
        payload: UpdateConsultationDocumentationPayload;
      }) => consultationsApi.updateDocumentation(id, payload),
      onSuccess: (data, variables) => {
        invalidateAll();
        invalidateDetail(variables.id);
        invalidateTimeline(data.patientId);
      },
    }),
    start: useMutation({
      mutationKey: [...consultationKeys.all, 'start'],
      mutationFn: (id: string) => consultationsApi.start(id),
      onSuccess: (data, id) => {
        invalidateAll();
        invalidateDetail(id);
        invalidateTimeline(data.patientId);
      },
    }),
    pause: useMutation({
      mutationKey: [...consultationKeys.all, 'pause'],
      mutationFn: (id: string) => consultationsApi.pause(id),
      onSuccess: (data, id) => {
        invalidateAll();
        invalidateDetail(id);
        invalidateTimeline(data.patientId);
      },
    }),
    resume: useMutation({
      mutationKey: [...consultationKeys.all, 'resume'],
      mutationFn: (id: string) => consultationsApi.resume(id),
      onSuccess: (data, id) => {
        invalidateAll();
        invalidateDetail(id);
        invalidateTimeline(data.patientId);
      },
    }),
    complete: useMutation({
      mutationKey: [...consultationKeys.all, 'complete'],
      mutationFn: ({ id, payload }: { id: string; payload?: CompleteConsultationPayload }) =>
        consultationsApi.complete(id, payload ?? {}),
      onSuccess: (data, variables) => {
        invalidateAll();
        invalidateDetail(variables.id);
        invalidateTimeline(data.patientId);
      },
    }),
    cancel: useMutation({
      mutationKey: [...consultationKeys.all, 'cancel'],
      mutationFn: (id: string) => consultationsApi.cancel(id),
      onSuccess: (data, id) => {
        invalidateAll();
        invalidateDetail(id);
        invalidateTimeline(data.patientId);
      },
    }),
  };
}
