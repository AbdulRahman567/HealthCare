import { apiDelete, apiGet, apiPost, apiPut } from '@/services/http/api';
import type {
  CreateDiagnosisPayload,
  DiagnosisResponse,
  PatientDiagnosisQuery,
  UpdateDiagnosisPayload,
} from '@/features/clinical/types/diagnosis';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const diagnosesApi = {
  async listForConsultation(consultationId: string): Promise<DiagnosisResponse[]> {
    return apiGet<DiagnosisResponse[]>(`/consultations/${consultationId}/diagnoses`);
  },

  async getById(consultationId: string, id: string): Promise<DiagnosisResponse> {
    return apiGet<DiagnosisResponse>(`/consultations/${consultationId}/diagnoses/${id}`);
  },

  async create(
    consultationId: string,
    payload: CreateDiagnosisPayload,
  ): Promise<DiagnosisResponse> {
    return apiPost<DiagnosisResponse>(`/consultations/${consultationId}/diagnoses`, payload);
  },

  async update(
    consultationId: string,
    id: string,
    payload: UpdateDiagnosisPayload,
  ): Promise<DiagnosisResponse> {
    return apiPut<DiagnosisResponse>(`/consultations/${consultationId}/diagnoses/${id}`, payload);
  },

  async remove(consultationId: string, id: string): Promise<void> {
    await apiDelete(`/consultations/${consultationId}/diagnoses/${id}`);
  },

  async listForPatient(
    patientId: string,
    query: PatientDiagnosisQuery = {},
  ): Promise<PageResponse<DiagnosisResponse>> {
    return apiGet<PageResponse<DiagnosisResponse>>(`/patients/${patientId}/diagnoses`, {
      params: toPageParams(query),
    });
  },
};
