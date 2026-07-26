import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { patientsApi } from '@/features/patients/api/patients-api';
import type { PatientListQuery, PatientWritePayload } from '@/features/patients/types/patient';

export const patientKeys = {
  all: ['patients'] as const,
  lists: () => [...patientKeys.all, 'list'] as const,
  list: (query: PatientListQuery) => [...patientKeys.lists(), query] as const,
  details: () => [...patientKeys.all, 'detail'] as const,
  detail: (id: string) => [...patientKeys.details(), id] as const,
};

export function usePatientsQuery(query: PatientListQuery, enabled = true) {
  return useQuery({
    queryKey: patientKeys.list(query),
    queryFn: () => patientsApi.search(query),
    enabled,
    placeholderData: (previous) => previous,
  });
}

export function usePatientQuery(id: string, enabled = true) {
  return useQuery({
    queryKey: patientKeys.detail(id),
    queryFn: () => patientsApi.getById(id),
    enabled: enabled && Boolean(id),
  });
}

export function usePatientMutations() {
  const queryClient = useQueryClient();

  const invalidateAll = () => queryClient.invalidateQueries({ queryKey: patientKeys.all });
  const invalidateDetail = (id: string) =>
    queryClient.invalidateQueries({ queryKey: patientKeys.detail(id) });

  return {
    register: useMutation({
      mutationKey: [...patientKeys.all, 'register'],
      mutationFn: (payload: PatientWritePayload) => patientsApi.register(payload),
      onSuccess: invalidateAll,
    }),
    update: useMutation({
      mutationKey: [...patientKeys.all, 'update'],
      mutationFn: ({ id, payload }: { id: string; payload: PatientWritePayload }) =>
        patientsApi.update(id, payload),
      onSuccess: (_data, variables) => {
        invalidateAll();
        invalidateDetail(variables.id);
      },
    }),
    deactivate: useMutation({
      mutationKey: [...patientKeys.all, 'deactivate'],
      mutationFn: (id: string) => patientsApi.deactivate(id),
      onSuccess: (_data, id) => {
        invalidateAll();
        invalidateDetail(id);
      },
    }),
    reactivate: useMutation({
      mutationKey: [...patientKeys.all, 'reactivate'],
      mutationFn: (id: string) => patientsApi.reactivate(id),
      onSuccess: (_data, id) => {
        invalidateAll();
        invalidateDetail(id);
      },
    }),
  };
}
