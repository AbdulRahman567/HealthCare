import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { immunizationsApi } from '@/features/patients/api/immunizations-api';
import type { UpsertImmunizationPayload } from '@/features/patients/types/immunization';
import type { ImmunizationStatus } from '@/features/patients/types/enums';
import { patientKeys } from '@/features/patients/hooks/use-patients';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const immunizationKeys = {
  all: (patientId: string) => [...patientKeys.detail(patientId), 'immunizations'] as const,
  list: (patientId: string, status?: ImmunizationStatus) =>
    [...immunizationKeys.all(patientId), 'list', status ?? 'all'] as const,
  due: (patientId: string) => [...immunizationKeys.all(patientId), 'due'] as const,
};

export function useImmunizationsQuery(
  patientId: string,
  status?: ImmunizationStatus,
  enabled = true,
) {
  return useQuery({
    queryKey: immunizationKeys.list(patientId, status),
    queryFn: () => immunizationsApi.list(patientId, status),
    enabled: enabled && Boolean(patientId),
  });
}

export function useImmunizationsDueQuery(patientId: string, enabled = true) {
  return useQuery({
    queryKey: immunizationKeys.due(patientId),
    queryFn: () => immunizationsApi.due(patientId),
    enabled: enabled && Boolean(patientId),
  });
}

export function useImmunizationMutations(patientId: string) {
  const queryClient = useQueryClient();
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: immunizationKeys.all(patientId) });
    void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });
  };

  return {
    create: useMutation({
      mutationFn: (payload: UpsertImmunizationPayload) =>
        immunizationsApi.create(patientId, payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationFn: ({
        immunizationId,
        payload,
      }: {
        immunizationId: string;
        payload: UpsertImmunizationPayload;
      }) => immunizationsApi.update(patientId, immunizationId, payload),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationFn: (immunizationId: string) => immunizationsApi.remove(patientId, immunizationId),
      onSuccess: invalidate,
    }),
  };
}
