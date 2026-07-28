import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { medicalHistoryApi } from '@/features/patients/api/medical-history-api';
import type {
  UpsertChronicConditionPayload,
  UpsertFamilyHistoryPayload,
  UpsertPastDiseasePayload,
  UpsertSurgeryHistoryPayload,
} from '@/features/patients/types/medical-history';
import { patientKeys } from '@/features/patients/hooks/use-patients';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const medicalHistoryKeys = {
  all: (patientId: string) => [...patientKeys.detail(patientId), 'medical-history'] as const,
};

export function useMedicalHistoryQuery(patientId: string, enabled = true) {
  return useQuery({
    queryKey: medicalHistoryKeys.all(patientId),
    queryFn: () => medicalHistoryApi.get(patientId),
    enabled: enabled && Boolean(patientId),
  });
}

export function useMedicalHistoryMutations(patientId: string) {
  const queryClient = useQueryClient();
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: medicalHistoryKeys.all(patientId) });
    void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });
  };

  return {
    createPastDisease: useMutation({
      mutationFn: (payload: UpsertPastDiseasePayload) =>
        medicalHistoryApi.createPastDisease(patientId, payload),
      onSuccess: invalidate,
    }),
    updatePastDisease: useMutation({
      mutationFn: ({ entryId, payload }: { entryId: string; payload: UpsertPastDiseasePayload }) =>
        medicalHistoryApi.updatePastDisease(patientId, entryId, payload),
      onSuccess: invalidate,
    }),
    deletePastDisease: useMutation({
      mutationFn: (entryId: string) => medicalHistoryApi.deletePastDisease(patientId, entryId),
      onSuccess: invalidate,
    }),
    createSurgery: useMutation({
      mutationFn: (payload: UpsertSurgeryHistoryPayload) =>
        medicalHistoryApi.createSurgery(patientId, payload),
      onSuccess: invalidate,
    }),
    updateSurgery: useMutation({
      mutationFn: ({
        entryId,
        payload,
      }: {
        entryId: string;
        payload: UpsertSurgeryHistoryPayload;
      }) => medicalHistoryApi.updateSurgery(patientId, entryId, payload),
      onSuccess: invalidate,
    }),
    deleteSurgery: useMutation({
      mutationFn: (entryId: string) => medicalHistoryApi.deleteSurgery(patientId, entryId),
      onSuccess: invalidate,
    }),
    createChronicCondition: useMutation({
      mutationFn: (payload: UpsertChronicConditionPayload) =>
        medicalHistoryApi.createChronicCondition(patientId, payload),
      onSuccess: invalidate,
    }),
    updateChronicCondition: useMutation({
      mutationFn: ({
        entryId,
        payload,
      }: {
        entryId: string;
        payload: UpsertChronicConditionPayload;
      }) => medicalHistoryApi.updateChronicCondition(patientId, entryId, payload),
      onSuccess: invalidate,
    }),
    deleteChronicCondition: useMutation({
      mutationFn: (entryId: string) => medicalHistoryApi.deleteChronicCondition(patientId, entryId),
      onSuccess: invalidate,
    }),
    createFamilyHistory: useMutation({
      mutationFn: (payload: UpsertFamilyHistoryPayload) =>
        medicalHistoryApi.createFamilyHistory(patientId, payload),
      onSuccess: invalidate,
    }),
    updateFamilyHistory: useMutation({
      mutationFn: ({
        entryId,
        payload,
      }: {
        entryId: string;
        payload: UpsertFamilyHistoryPayload;
      }) => medicalHistoryApi.updateFamilyHistory(patientId, entryId, payload),
      onSuccess: invalidate,
    }),
    deleteFamilyHistory: useMutation({
      mutationFn: (entryId: string) => medicalHistoryApi.deleteFamilyHistory(patientId, entryId),
      onSuccess: invalidate,
    }),
  };
}
