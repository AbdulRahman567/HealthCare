import type {
  CreateFollowUpPayload,
  FollowUpResponse,
  FollowUpSearchQuery,
  PatientFollowUpQuery,
  UpdateFollowUpPayload,
  UpdateFollowUpStatusPayload,
} from '@/features/clinical/types/follow-up';
import { toPageParams } from '@/lib/page-query';
import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const followUpsApi = {
  async listForConsultation(consultationId: string): Promise<FollowUpResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<FollowUpResponse[]>>(
      `/consultations/${consultationId}/follow-ups`,
    );
    return data.data;
  },

  async getById(consultationId: string, id: string): Promise<FollowUpResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<FollowUpResponse>>(
      `/consultations/${consultationId}/follow-ups/${id}`,
    );
    return data.data;
  },

  async create(consultationId: string, payload: CreateFollowUpPayload): Promise<FollowUpResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<FollowUpResponse>>(
      `/consultations/${consultationId}/follow-ups`,
      payload,
    );
    return data.data;
  },

  async update(
    consultationId: string,
    id: string,
    payload: UpdateFollowUpPayload,
  ): Promise<FollowUpResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<FollowUpResponse>>(
      `/consultations/${consultationId}/follow-ups/${id}`,
      payload,
    );
    return data.data;
  },

  async updateStatus(
    consultationId: string,
    id: string,
    payload: UpdateFollowUpStatusPayload,
  ): Promise<FollowUpResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<FollowUpResponse>>(
      `/consultations/${consultationId}/follow-ups/${id}/status`,
      payload,
    );
    return data.data;
  },

  async remove(consultationId: string, id: string): Promise<void> {
    await apiClient.delete(`/consultations/${consultationId}/follow-ups/${id}`);
  },

  async search(query: FollowUpSearchQuery = {}): Promise<PageResponse<FollowUpResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<FollowUpResponse>>>(
      '/follow-ups',
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async due(withinDays = 14): Promise<FollowUpResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<FollowUpResponse[]>>(
      '/follow-ups/due',
      { params: { withinDays } },
    );
    return data.data;
  },

  async getGlobalById(id: string): Promise<FollowUpResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<FollowUpResponse>>(
      `/follow-ups/${id}`,
    );
    return data.data;
  },

  async listForPatient(
    patientId: string,
    query: PatientFollowUpQuery = {},
  ): Promise<PageResponse<FollowUpResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<FollowUpResponse>>>(
      `/patients/${patientId}/follow-ups`,
      { params: toPageParams(query) },
    );
    return data.data;
  },
};
