import type {
  CancelPrescriptionPayload,
  CreatePrescriptionPayload,
  PrescriptionListQuery,
  PrescriptionResponse,
  UpdatePrescriptionPayload,
} from '@/features/clinical/types/prescription';
import { toPageParams } from '@/lib/page-query';
import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const prescriptionsApi = {
  async listForConsultation(consultationId: string): Promise<PrescriptionResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<PrescriptionResponse[]>>(
      `/consultations/${consultationId}/prescriptions`,
    );
    return data.data;
  },

  async search(query: PrescriptionListQuery = {}): Promise<PageResponse<PrescriptionResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<PrescriptionResponse>>>(
      '/prescriptions',
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async getById(id: string): Promise<PrescriptionResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<PrescriptionResponse>>(
      `/prescriptions/${id}`,
    );
    return data.data;
  },

  async create(payload: CreatePrescriptionPayload): Promise<PrescriptionResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<PrescriptionResponse>>(
      '/prescriptions',
      payload,
    );
    return data.data;
  },

  async update(id: string, payload: UpdatePrescriptionPayload): Promise<PrescriptionResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<PrescriptionResponse>>(
      `/prescriptions/${id}`,
      payload,
    );
    return data.data;
  },

  async issue(id: string): Promise<PrescriptionResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<PrescriptionResponse>>(
      `/prescriptions/${id}/issue`,
    );
    return data.data;
  },

  async cancel(
    id: string,
    payload: CancelPrescriptionPayload = {},
  ): Promise<PrescriptionResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<PrescriptionResponse>>(
      `/prescriptions/${id}/cancel`,
      payload,
    );
    return data.data;
  },

  async remove(id: string): Promise<void> {
    await apiClient.delete(`/prescriptions/${id}`);
  },

  async listForPatient(
    patientId: string,
    query: PageQueryLike = {},
  ): Promise<PageResponse<PrescriptionResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<PrescriptionResponse>>>(
      `/patients/${patientId}/prescriptions`,
      { params: toPageParams(query) },
    );
    return data.data;
  },
};

type PageQueryLike = {
  page?: number;
  size?: number;
  sort?: string | string[];
};
