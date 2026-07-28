import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { vitalsApi } from '@/features/clinical/api/vitals-api';
import { consultationKeys } from '@/features/clinical/hooks/use-consultations';
import type {
  PatientVitalSignsQuery,
  RecordVitalSignsPayload,
  UpdateVitalSignsPayload,
} from '@/features/clinical/types/vitals';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const vitalsKeys = {
  all: (consultationId: string) =>
    [...consultationKeys.detail(consultationId), 'vital-signs'] as const,
  list: (consultationId: string) => [...vitalsKeys.all(consultationId), 'list'] as const,
  patient: (patientId: string, query: PatientVitalSignsQuery) =>
    ['patients', patientId, 'vital-signs', query] as const,
};

export function useConsultationVitalsQuery(consultationId: string, enabled = true) {
  return useQuery({
    queryKey: vitalsKeys.list(consultationId),
    queryFn: () => vitalsApi.listForConsultation(consultationId),
    enabled: enabled && Boolean(consultationId),
  });
}

export function usePatientVitalsQuery(
  patientId: string,
  query: PatientVitalSignsQuery = {},
  enabled = true,
) {
  return useQuery({
    queryKey: vitalsKeys.patient(patientId, query),
    queryFn: () => vitalsApi.listForPatient(patientId, query),
    enabled: enabled && Boolean(patientId),
    placeholderData: (previous) => previous,
  });
}

export function useVitalsMutations(consultationId: string, patientId?: string) {
  const queryClient = useQueryClient();
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: vitalsKeys.all(consultationId) });
    if (patientId) {
      void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });
    }
  };

  return {
    record: useMutation({
      mutationFn: (payload: RecordVitalSignsPayload) =>
        vitalsApi.record(consultationId, payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationFn: ({ id, payload }: { id: string; payload: UpdateVitalSignsPayload }) =>
        vitalsApi.update(consultationId, id, payload),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationFn: (id: string) => vitalsApi.remove(consultationId, id),
      onSuccess: invalidate,
    }),
  };
}
