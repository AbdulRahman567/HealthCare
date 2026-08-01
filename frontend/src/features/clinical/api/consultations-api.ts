import { apiGet, apiPatch, apiPost, apiPut } from '@/services/http/api';
import type {
  CompleteConsultationPayload,
  ConsultationListQuery,
  ConsultationResponse,
  CreateConsultationPayload,
  UpdateConsultationDocumentationPayload,
} from '@/features/clinical/types/consultation';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const consultationsApi = {
  async search(query: ConsultationListQuery = {}): Promise<PageResponse<ConsultationResponse>> {
    return apiGet<PageResponse<ConsultationResponse>>('/consultations', {
      params: toPageParams(query),
    });
  },

  async getById(id: string): Promise<ConsultationResponse> {
    return apiGet<ConsultationResponse>(`/consultations/${id}`);
  },

  async create(payload: CreateConsultationPayload): Promise<ConsultationResponse> {
    return apiPost<ConsultationResponse>('/consultations', payload);
  },

  async updateDocumentation(
    id: string,
    payload: UpdateConsultationDocumentationPayload,
  ): Promise<ConsultationResponse> {
    return apiPut<ConsultationResponse>(`/consultations/${id}/documentation`, payload);
  },

  async start(id: string): Promise<ConsultationResponse> {
    return apiPatch<ConsultationResponse>(`/consultations/${id}/start`);
  },

  async pause(id: string): Promise<ConsultationResponse> {
    return apiPatch<ConsultationResponse>(`/consultations/${id}/pause`);
  },

  async resume(id: string): Promise<ConsultationResponse> {
    return apiPatch<ConsultationResponse>(`/consultations/${id}/resume`);
  },

  async complete(
    id: string,
    payload: CompleteConsultationPayload = {},
  ): Promise<ConsultationResponse> {
    return apiPatch<ConsultationResponse>(`/consultations/${id}/complete`, payload);
  },

  async cancel(id: string): Promise<ConsultationResponse> {
    return apiPatch<ConsultationResponse>(`/consultations/${id}/cancel`);
  },
};
