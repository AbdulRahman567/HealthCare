import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { clinicalNotesApi } from '@/features/clinical/api/clinical-notes-api';
import { consultationKeys } from '@/features/clinical/hooks/use-consultations';
import type {
  CreateClinicalNotePayload,
  UpdateClinicalNotePayload,
} from '@/features/clinical/types/clinical-note';
import type { ClinicalNoteType } from '@/features/clinical/types/enums';
import { timelineKeys } from '@/features/patients/hooks/use-timeline';

export const clinicalNoteKeys = {
  all: (consultationId: string) =>
    [...consultationKeys.detail(consultationId), 'clinical-notes'] as const,
  list: (consultationId: string, noteType?: ClinicalNoteType) =>
    [...clinicalNoteKeys.all(consultationId), 'list', noteType ?? 'ALL'] as const,
};

export function useConsultationNotesQuery(
  consultationId: string,
  noteType?: ClinicalNoteType,
  enabled = true,
) {
  return useQuery({
    queryKey: clinicalNoteKeys.list(consultationId, noteType),
    queryFn: () => clinicalNotesApi.listForConsultation(consultationId, noteType),
    enabled: enabled && Boolean(consultationId),
  });
}

export function useClinicalNoteMutations(consultationId: string, patientId?: string) {
  const queryClient = useQueryClient();
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: clinicalNoteKeys.all(consultationId) });
    if (patientId) {
      void queryClient.invalidateQueries({ queryKey: timelineKeys.all(patientId) });
    }
  };

  return {
    create: useMutation({
      mutationFn: (payload: CreateClinicalNotePayload) =>
        clinicalNotesApi.create(consultationId, payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationFn: ({
        noteId,
        payload,
      }: {
        noteId: string;
        payload: UpdateClinicalNotePayload;
      }) => clinicalNotesApi.update(consultationId, noteId, payload),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationFn: (noteId: string) => clinicalNotesApi.remove(consultationId, noteId),
      onSuccess: invalidate,
    }),
    uploadAttachment: useMutation({
      mutationFn: ({
        noteId,
        file,
        description,
      }: {
        noteId: string;
        file: File;
        description?: string;
      }) => clinicalNotesApi.uploadAttachment(consultationId, noteId, file, description),
      onSuccess: invalidate,
    }),
    removeAttachment: useMutation({
      mutationFn: ({ noteId, attachmentId }: { noteId: string; attachmentId: string }) =>
        clinicalNotesApi.removeAttachment(consultationId, noteId, attachmentId),
      onSuccess: invalidate,
    }),
  };
}
