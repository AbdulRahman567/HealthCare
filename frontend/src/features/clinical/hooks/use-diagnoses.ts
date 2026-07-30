import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { diagnosesApi } from '@/features/clinical/api/diagnoses-api';
import { consultationKeys } from '@/features/clinical/hooks/use-consultations';
import type {
  CreateDiagnosisPayload,
  PatientDiagnosisQuery,
  UpdateDiagnosisPayload,
} from '@/features/clinical/types/diagnosis';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const diagnosisKeys = {
  all: (consultationId: string) =>
    [...consultationKeys.detail(consultationId), 'diagnoses'] as const,
  list: (consultationId: string) => [...diagnosisKeys.all(consultationId), 'list'] as const,
  patient: (patientId: string, query: PatientDiagnosisQuery) =>
    ['patients', patientId, 'diagnoses', query] as const,
};

export function useConsultationDiagnosesQuery(consultationId: string, enabled = true) {
  return useQuery({
    queryKey: diagnosisKeys.list(consultationId),
    queryFn: () => diagnosesApi.listForConsultation(consultationId),
    enabled: enabled && Boolean(consultationId),
  });
}

export function usePatientDiagnosesQuery(
  patientId: string,
  query: PatientDiagnosisQuery = {},
  enabled = true,
) {
  return useQuery({
    queryKey: diagnosisKeys.patient(patientId, query),
    queryFn: () => diagnosesApi.listForPatient(patientId, query),
    enabled: enabled && Boolean(patientId),
    placeholderData: (previous) => previous,
  });
}

export function useDiagnosisMutations(consultationId: string, patientId?: string) {
  const queryClient = useQueryClient();
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: diagnosisKeys.all(consultationId) });
    if (patientId) {
      void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });
    }
  };

  return {
    create: useMutation({
      mutationFn: (payload: CreateDiagnosisPayload) => diagnosesApi.create(consultationId, payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationFn: ({ id, payload }: { id: string; payload: UpdateDiagnosisPayload }) =>
        diagnosesApi.update(consultationId, id, payload),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationFn: (id: string) => diagnosesApi.remove(consultationId, id),
      onSuccess: invalidate,
    }),
  };
}
