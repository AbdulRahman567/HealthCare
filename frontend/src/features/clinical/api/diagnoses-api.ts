import type {
  CreateDiagnosisPayload,
  DiagnosisResponse,
  PatientDiagnosisQuery,
  UpdateDiagnosisPayload,
} from '@/features/clinical/types/diagnosis';
import { toPageParams } from '@/lib/page-query';
import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const diagnosesApi = {
  async listForConsultation(consultationId: string): Promise<DiagnosisResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<DiagnosisResponse[]>>(
      `/consultations/${consultationId}/diagnoses`,
    );
    return data.data;
  },

  async getById(consultationId: string, id: string): Promise<DiagnosisResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<DiagnosisResponse>>(
      `/consultations/${consultationId}/diagnoses/${id}`,
    );
    return data.data;
  },

  async create(
    consultationId: string,
    payload: CreateDiagnosisPayload,
  ): Promise<DiagnosisResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<DiagnosisResponse>>(
      `/consultations/${consultationId}/diagnoses`,
      payload,
    );
    return data.data;
  },

  async update(
    consultationId: string,
    id: string,
    payload: UpdateDiagnosisPayload,
  ): Promise<DiagnosisResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<DiagnosisResponse>>(
      `/consultations/${consultationId}/diagnoses/${id}`,
      payload,
    );
    return data.data;
  },

  async remove(consultationId: string, id: string): Promise<void> {
    await apiClient.delete(`/consultations/${consultationId}/diagnoses/${id}`);
  },

  async listForPatient(
    patientId: string,
    query: PatientDiagnosisQuery = {},
  ): Promise<PageResponse<DiagnosisResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<DiagnosisResponse>>>(
      `/patients/${patientId}/diagnoses`,
      { params: toPageParams(query) },
    );
    return data.data;
  },
};
