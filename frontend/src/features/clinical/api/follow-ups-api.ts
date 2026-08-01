import { apiDelete, apiGet, apiPatch, apiPost, apiPut } from '@/services/http/api';
import type {
  CreateFollowUpPayload,
  FollowUpResponse,
  FollowUpSearchQuery,
  PatientFollowUpQuery,
  UpdateFollowUpPayload,
  UpdateFollowUpStatusPayload,
} from '@/features/clinical/types/follow-up';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const followUpsApi = {
  async listForConsultation(consultationId: string): Promise<FollowUpResponse[]> {
    return apiGet<FollowUpResponse[]>(`/consultations/${consultationId}/follow-ups`);
  },

  async getById(consultationId: string, id: string): Promise<FollowUpResponse> {
    return apiGet<FollowUpResponse>(`/consultations/${consultationId}/follow-ups/${id}`);
  },

  async create(consultationId: string, payload: CreateFollowUpPayload): Promise<FollowUpResponse> {
    return apiPost<FollowUpResponse>(`/consultations/${consultationId}/follow-ups`, payload);
  },

  async update(
    consultationId: string,
    id: string,
    payload: UpdateFollowUpPayload,
  ): Promise<FollowUpResponse> {
    return apiPut<FollowUpResponse>(`/consultations/${consultationId}/follow-ups/${id}`, payload);
  },

  async updateStatus(
    consultationId: string,
    id: string,
    payload: UpdateFollowUpStatusPayload,
  ): Promise<FollowUpResponse> {
    return apiPatch<FollowUpResponse>(
      `/consultations/${consultationId}/follow-ups/${id}/status`,
      payload,
    );
  },

  async remove(consultationId: string, id: string): Promise<void> {
    await apiDelete(`/consultations/${consultationId}/follow-ups/${id}`);
  },

  async search(query: FollowUpSearchQuery = {}): Promise<PageResponse<FollowUpResponse>> {
    return apiGet<PageResponse<FollowUpResponse>>('/follow-ups', {
      params: toPageParams(query),
    });
  },

  async due(withinDays = 14): Promise<FollowUpResponse[]> {
    return apiGet<FollowUpResponse[]>('/follow-ups/due', {
      params: { withinDays },
    });
  },

  async getGlobalById(id: string): Promise<FollowUpResponse> {
    return apiGet<FollowUpResponse>(`/follow-ups/${id}`);
  },

  async listForPatient(
    patientId: string,
    query: PatientFollowUpQuery = {},
  ): Promise<PageResponse<FollowUpResponse>> {
    return apiGet<PageResponse<FollowUpResponse>>(`/patients/${patientId}/follow-ups`, {
      params: toPageParams(query),
    });
  },
};
