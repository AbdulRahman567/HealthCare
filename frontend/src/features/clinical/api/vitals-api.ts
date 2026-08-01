import { apiDelete, apiGet, apiPost, apiPut } from '@/services/http/api';
import type {
  PatientVitalSignsQuery,
  RecordVitalSignsPayload,
  UpdateVitalSignsPayload,
  VitalSignsResponse,
} from '@/features/clinical/types/vitals';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const vitalsApi = {
  async listForConsultation(consultationId: string): Promise<VitalSignsResponse[]> {
    return apiGet<VitalSignsResponse[]>(`/consultations/${consultationId}/vital-signs`);
  },

  async getById(consultationId: string, id: string): Promise<VitalSignsResponse> {
    return apiGet<VitalSignsResponse>(`/consultations/${consultationId}/vital-signs/${id}`);
  },

  async record(
    consultationId: string,
    payload: RecordVitalSignsPayload,
  ): Promise<VitalSignsResponse> {
    return apiPost<VitalSignsResponse>(`/consultations/${consultationId}/vital-signs`, payload);
  },

  async update(
    consultationId: string,
    id: string,
    payload: UpdateVitalSignsPayload,
  ): Promise<VitalSignsResponse> {
    return apiPut<VitalSignsResponse>(
      `/consultations/${consultationId}/vital-signs/${id}`,
      payload,
    );
  },

  async remove(consultationId: string, id: string): Promise<void> {
    await apiDelete(`/consultations/${consultationId}/vital-signs/${id}`);
  },

  async listForPatient(
    patientId: string,
    query: PatientVitalSignsQuery = {},
  ): Promise<PageResponse<VitalSignsResponse>> {
    return apiGet<PageResponse<VitalSignsResponse>>(`/patients/${patientId}/vital-signs`, {
      params: toPageParams(query),
    });
  },
};
