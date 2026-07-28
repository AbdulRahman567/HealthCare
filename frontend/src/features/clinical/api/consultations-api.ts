import type {
  CompleteConsultationPayload,
  ConsultationListQuery,
  ConsultationResponse,
  CreateConsultationPayload,
  UpdateConsultationDocumentationPayload,
} from '@/features/clinical/types/consultation';
import { toPageParams } from '@/lib/page-query';
import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const consultationsApi = {
  async search(query: ConsultationListQuery = {}): Promise<PageResponse<ConsultationResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<ConsultationResponse>>>(
      '/consultations',
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async getById(id: string): Promise<ConsultationResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<ConsultationResponse>>(
      `/consultations/${id}`,
    );
    return data.data;
  },

  async create(payload: CreateConsultationPayload): Promise<ConsultationResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<ConsultationResponse>>(
      '/consultations',
      payload,
    );
    return data.data;
  },

  async updateDocumentation(
    id: string,
    payload: UpdateConsultationDocumentationPayload,
  ): Promise<ConsultationResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<ConsultationResponse>>(
      `/consultations/${id}/documentation`,
      payload,
    );
    return data.data;
  },

  async start(id: string): Promise<ConsultationResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<ConsultationResponse>>(
      `/consultations/${id}/start`,
    );
    return data.data;
  },

  async pause(id: string): Promise<ConsultationResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<ConsultationResponse>>(
      `/consultations/${id}/pause`,
    );
    return data.data;
  },

  async resume(id: string): Promise<ConsultationResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<ConsultationResponse>>(
      `/consultations/${id}/resume`,
    );
    return data.data;
  },

  async complete(
    id: string,
    payload: CompleteConsultationPayload = {},
  ): Promise<ConsultationResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<ConsultationResponse>>(
      `/consultations/${id}/complete`,
      payload,
    );
    return data.data;
  },

  async cancel(id: string): Promise<ConsultationResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<ConsultationResponse>>(
      `/consultations/${id}/cancel`,
    );
    return data.data;
  },
};
