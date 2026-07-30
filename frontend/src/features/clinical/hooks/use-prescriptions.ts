import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { prescriptionsApi } from '@/features/clinical/api/prescriptions-api';
import { consultationKeys } from '@/features/clinical/hooks/use-consultations';
import type {
  CancelPrescriptionPayload,
  CreatePrescriptionPayload,
  UpdatePrescriptionPayload,
} from '@/features/clinical/types/prescription';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const prescriptionKeys = {
  all: ['prescriptions'] as const,
  consultation: (consultationId: string) =>
    [...consultationKeys.detail(consultationId), 'prescriptions'] as const,
  list: (consultationId: string) =>
    [...prescriptionKeys.consultation(consultationId), 'list'] as const,
  patient: (patientId: string) => [...prescriptionKeys.all, 'patient', patientId] as const,
  patientList: (patientId: string, page: number, size: number) =>
    [...prescriptionKeys.patient(patientId), 'list', page, size] as const,
  detail: (id: string) => [...prescriptionKeys.all, 'detail', id] as const,
};

export function useConsultationPrescriptionsQuery(consultationId: string, enabled = true) {
  return useQuery({
    queryKey: prescriptionKeys.list(consultationId),
    queryFn: () => prescriptionsApi.listForConsultation(consultationId),
    enabled: enabled && Boolean(consultationId),
  });
}

export function usePatientPrescriptionsQuery(
  patientId: string,
  page = 0,
  size = 20,
  enabled = true,
) {
  return useQuery({
    queryKey: prescriptionKeys.patientList(patientId, page, size),
    queryFn: () =>
      prescriptionsApi.listForPatient(patientId, { page, size, sort: 'prescriptionDate,desc' }),
    enabled: enabled && Boolean(patientId),
  });
}

export function usePrescriptionMutations(consultationId: string, patientId?: string) {
  const queryClient = useQueryClient();
  const invalidate = () => {
    void queryClient.invalidateQueries({
      queryKey: prescriptionKeys.consultation(consultationId),
    });
    void queryClient.invalidateQueries({ queryKey: prescriptionKeys.all });
    if (patientId) {
      void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });
      void queryClient.invalidateQueries({ queryKey: prescriptionKeys.patient(patientId) });
    }
  };

  return {
    create: useMutation({
      mutationFn: (payload: CreatePrescriptionPayload) => prescriptionsApi.create(payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationFn: ({ id, payload }: { id: string; payload: UpdatePrescriptionPayload }) =>
        prescriptionsApi.update(id, payload),
      onSuccess: invalidate,
    }),
    issue: useMutation({
      mutationFn: (id: string) => prescriptionsApi.issue(id),
      onSuccess: invalidate,
    }),
    cancel: useMutation({
      mutationFn: ({ id, payload }: { id: string; payload?: CancelPrescriptionPayload }) =>
        prescriptionsApi.cancel(id, payload ?? {}),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationFn: (id: string) => prescriptionsApi.remove(id),
      onSuccess: invalidate,
    }),
  };
}
