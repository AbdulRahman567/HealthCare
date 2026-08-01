import { apiGet, apiPost, apiPut } from '@/services/http/api';
import type {
  PatientListQuery,
  PatientResponse,
  PatientWritePayload,
} from '@/features/patients/types/patient';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const patientsApi = {
  async search(query: PatientListQuery = {}): Promise<PageResponse<PatientResponse>> {
    return apiGet<PageResponse<PatientResponse>>('/patients', {
      params: toPageParams(query),
    });
  },

  async getById(id: string): Promise<PatientResponse> {
    return apiGet<PatientResponse>(`/patients/${id}`);
  },

  async register(payload: PatientWritePayload): Promise<PatientResponse> {
    return apiPost<PatientResponse>('/patients', payload);
  },

  async update(id: string, payload: PatientWritePayload): Promise<PatientResponse> {
    return apiPut<PatientResponse>(`/patients/${id}`, payload);
  },

  async deactivate(id: string): Promise<PatientResponse> {
    return apiPost<PatientResponse>(`/patients/${id}/deactivate`);
  },

  async reactivate(id: string): Promise<PatientResponse> {
    return apiPost<PatientResponse>(`/patients/${id}/reactivate`);
  },
};
