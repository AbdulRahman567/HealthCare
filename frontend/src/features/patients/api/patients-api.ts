import { apiClient } from '@/services/http/api-client';
import type { PatientListQuery, PatientResponse, PatientWritePayload } from '@/features/patients/types/patient';
import { toPageParams } from '@/lib/page-query';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const patientsApi = {
  async search(query: PatientListQuery = {}): Promise<PageResponse<PatientResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<PatientResponse>>>(
      '/patients',
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async getById(id: string): Promise<PatientResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<PatientResponse>>(`/patients/${id}`);
    return data.data;
  },

  async register(payload: PatientWritePayload): Promise<PatientResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<PatientResponse>>(
      '/patients',
      payload,
    );
    return data.data;
  },

  async update(id: string, payload: PatientWritePayload): Promise<PatientResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<PatientResponse>>(
      `/patients/${id}`,
      payload,
    );
    return data.data;
  },

  async deactivate(id: string): Promise<PatientResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<PatientResponse>>(
      `/patients/${id}/deactivate`,
    );
    return data.data;
  },

  async reactivate(id: string): Promise<PatientResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<PatientResponse>>(
      `/patients/${id}/reactivate`,
    );
    return data.data;
  },
};
