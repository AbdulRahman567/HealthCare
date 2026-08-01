import { apiDelete, apiGet, apiPost, apiPut } from '@/services/http/api';
import type {
  ClinicalNoteAttachmentResponse,
  ClinicalNoteResponse,
  CreateClinicalNotePayload,
  PatientClinicalNotesQuery,
  UpdateClinicalNotePayload,
} from '@/features/clinical/types/clinical-note';
import type { ClinicalNoteType } from '@/features/clinical/types/enums';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const clinicalNotesApi = {
  async listForConsultation(
    consultationId: string,
    noteType?: ClinicalNoteType,
  ): Promise<ClinicalNoteResponse[]> {
    return apiGet<ClinicalNoteResponse[]>(`/consultations/${consultationId}/clinical-notes`, {
      params: noteType ? { noteType } : undefined,
    });
  },

  async getById(consultationId: string, noteId: string): Promise<ClinicalNoteResponse> {
    return apiGet<ClinicalNoteResponse>(
      `/consultations/${consultationId}/clinical-notes/${noteId}`,
    );
  },

  async create(
    consultationId: string,
    payload: CreateClinicalNotePayload,
  ): Promise<ClinicalNoteResponse> {
    return apiPost<ClinicalNoteResponse>(
      `/consultations/${consultationId}/clinical-notes`,
      payload,
    );
  },

  async update(
    consultationId: string,
    noteId: string,
    payload: UpdateClinicalNotePayload,
  ): Promise<ClinicalNoteResponse> {
    return apiPut<ClinicalNoteResponse>(
      `/consultations/${consultationId}/clinical-notes/${noteId}`,
      payload,
    );
  },

  async remove(consultationId: string, noteId: string): Promise<void> {
    await apiDelete(`/consultations/${consultationId}/clinical-notes/${noteId}`);
  },

  async uploadAttachment(
    consultationId: string,
    noteId: string,
    file: File,
    description?: string,
  ): Promise<ClinicalNoteAttachmentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (description?.trim()) {
      formData.append('description', description.trim());
    }
    return apiPost<ClinicalNoteAttachmentResponse>(
      `/consultations/${consultationId}/clinical-notes/${noteId}/attachments`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } },
    );
  },

  async listAttachments(
    consultationId: string,
    noteId: string,
  ): Promise<ClinicalNoteAttachmentResponse[]> {
    return apiGet<ClinicalNoteAttachmentResponse[]>(
      `/consultations/${consultationId}/clinical-notes/${noteId}/attachments`,
    );
  },

  async removeAttachment(
    consultationId: string,
    noteId: string,
    attachmentId: string,
  ): Promise<void> {
    await apiDelete(
      `/consultations/${consultationId}/clinical-notes/${noteId}/attachments/${attachmentId}`,
    );
  },

  async listForPatient(
    patientId: string,
    query: PatientClinicalNotesQuery = {},
  ): Promise<PageResponse<ClinicalNoteResponse>> {
    return apiGet<PageResponse<ClinicalNoteResponse>>(`/patients/${patientId}/clinical-notes`, {
      params: toPageParams(query),
    });
  },
};
