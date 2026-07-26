import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { allergiesApi } from '@/features/patients/api/allergies-api';
import type { UpsertAllergyPayload } from '@/features/patients/types/allergy';
import type { AllergyType } from '@/features/patients/types/enums';
import { patientKeys } from '@/features/patients/hooks/use-patients';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const allergyKeys = {
  all: (patientId: string) => [...patientKeys.detail(patientId), 'allergies'] as const,
  list: (patientId: string, type?: AllergyType) =>
    [...allergyKeys.all(patientId), 'list', type ?? 'all'] as const,
  banner: (patientId: string) => [...allergyKeys.all(patientId), 'banner'] as const,
  critical: (patientId: string) => [...allergyKeys.all(patientId), 'critical'] as const,
};

export function useAllergiesQuery(patientId: string, type?: AllergyType, enabled = true) {
  return useQuery({
    queryKey: allergyKeys.list(patientId, type),
    queryFn: () => allergiesApi.list(patientId, type),
    enabled: enabled && Boolean(patientId),
  });
}

export function useAllergyBannerQuery(patientId: string, enabled = true) {
  return useQuery({
    queryKey: allergyKeys.banner(patientId),
    queryFn: () => allergiesApi.banner(patientId),
    enabled: enabled && Boolean(patientId),
  });
}

export function useAllergyCriticalQuery(patientId: string, enabled = true) {
  return useQuery({
    queryKey: allergyKeys.critical(patientId),
    queryFn: () => allergiesApi.critical(patientId),
    enabled: enabled && Boolean(patientId),
  });
}

export function useAllergyMutations(patientId: string) {
  const queryClient = useQueryClient();
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: allergyKeys.all(patientId) });
    void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });
  };

  return {
    create: useMutation({
      mutationFn: (payload: UpsertAllergyPayload) => allergiesApi.create(patientId, payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationFn: ({ allergyId, payload }: { allergyId: string; payload: UpsertAllergyPayload }) =>
        allergiesApi.update(patientId, allergyId, payload),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationFn: (allergyId: string) => allergiesApi.remove(patientId, allergyId),
      onSuccess: invalidate,
    }),
  };
}
