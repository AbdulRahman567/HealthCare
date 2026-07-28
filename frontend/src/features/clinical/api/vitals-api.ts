import type {
  PatientVitalSignsQuery,
  RecordVitalSignsPayload,
  UpdateVitalSignsPayload,
  VitalSignsResponse,
} from '@/features/clinical/types/vitals';
import { toPageParams } from '@/lib/page-query';
import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const vitalsApi = {
  async listForConsultation(consultationId: string): Promise<VitalSignsResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<VitalSignsResponse[]>>(
      `/consultations/${consultationId}/vital-signs`,
    );
    return data.data;
  },

  async getById(consultationId: string, id: string): Promise<VitalSignsResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<VitalSignsResponse>>(
      `/consultations/${consultationId}/vital-signs/${id}`,
    );
    return data.data;
  },

  async record(
    consultationId: string,
    payload: RecordVitalSignsPayload,
  ): Promise<VitalSignsResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<VitalSignsResponse>>(
      `/consultations/${consultationId}/vital-signs`,
      payload,
    );
    return data.data;
  },

  async update(
    consultationId: string,
    id: string,
    payload: UpdateVitalSignsPayload,
  ): Promise<VitalSignsResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<VitalSignsResponse>>(
      `/consultations/${consultationId}/vital-signs/${id}`,
      payload,
    );
    return data.data;
  },

  async remove(consultationId: string, id: string): Promise<void> {
    await apiClient.delete(`/consultations/${consultationId}/vital-signs/${id}`);
  },

  async listForPatient(
    patientId: string,
    query: PatientVitalSignsQuery = {},
  ): Promise<PageResponse<VitalSignsResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<VitalSignsResponse>>>(
      `/patients/${patientId}/vital-signs`,
      { params: toPageParams(query) },
    );
    return data.data;
  },
};
