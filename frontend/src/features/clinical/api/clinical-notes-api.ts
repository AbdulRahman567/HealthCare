import type {
  ClinicalNoteAttachmentResponse,
  ClinicalNoteResponse,
  CreateClinicalNotePayload,
  PatientClinicalNotesQuery,
  UpdateClinicalNotePayload,
} from '@/features/clinical/types/clinical-note';
import type { ClinicalNoteType } from '@/features/clinical/types/enums';
import { toPageParams } from '@/lib/page-query';
import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const clinicalNotesApi = {
  async listForConsultation(
    consultationId: string,
    noteType?: ClinicalNoteType,
  ): Promise<ClinicalNoteResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<ClinicalNoteResponse[]>>(
      `/consultations/${consultationId}/clinical-notes`,
      { params: noteType ? { noteType } : undefined },
    );
    return data.data;
  },

  async getById(consultationId: string, noteId: string): Promise<ClinicalNoteResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<ClinicalNoteResponse>>(
      `/consultations/${consultationId}/clinical-notes/${noteId}`,
    );
    return data.data;
  },

  async create(
    consultationId: string,
    payload: CreateClinicalNotePayload,
  ): Promise<ClinicalNoteResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<ClinicalNoteResponse>>(
      `/consultations/${consultationId}/clinical-notes`,
      payload,
    );
    return data.data;
  },

  async update(
    consultationId: string,
    noteId: string,
    payload: UpdateClinicalNotePayload,
  ): Promise<ClinicalNoteResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<ClinicalNoteResponse>>(
      `/consultations/${consultationId}/clinical-notes/${noteId}`,
      payload,
    );
    return data.data;
  },

  async remove(consultationId: string, noteId: string): Promise<void> {
    await apiClient.delete(`/consultations/${consultationId}/clinical-notes/${noteId}`);
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
    const { data } = await apiClient.post<ApiSuccessResponse<ClinicalNoteAttachmentResponse>>(
      `/consultations/${consultationId}/clinical-notes/${noteId}/attachments`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } },
    );
    return data.data;
  },

  async listAttachments(
    consultationId: string,
    noteId: string,
  ): Promise<ClinicalNoteAttachmentResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<ClinicalNoteAttachmentResponse[]>>(
      `/consultations/${consultationId}/clinical-notes/${noteId}/attachments`,
    );
    return data.data;
  },

  async removeAttachment(
    consultationId: string,
    noteId: string,
    attachmentId: string,
  ): Promise<void> {
    await apiClient.delete(
      `/consultations/${consultationId}/clinical-notes/${noteId}/attachments/${attachmentId}`,
    );
  },

  async listForPatient(
    patientId: string,
    query: PatientClinicalNotesQuery = {},
  ): Promise<PageResponse<ClinicalNoteResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<ClinicalNoteResponse>>>(
      `/patients/${patientId}/clinical-notes`,
      { params: toPageParams(query) },
    );
    return data.data;
  },
};
