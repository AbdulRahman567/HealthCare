import { apiDelete, apiGet, apiPost, apiPut } from '@/services/http/api';
import type {
  ImmunizationDueResponse,
  ImmunizationResponse,
  UpsertImmunizationPayload,
} from '@/features/patients/types/immunization';
import type { ImmunizationStatus } from '@/features/patients/types/enums';

const base = (patientId: string) => `/patients/${patientId}/immunizations`;

export const immunizationsApi = {
  async list(patientId: string, status?: ImmunizationStatus): Promise<ImmunizationResponse[]> {
    return apiGet<ImmunizationResponse[]>(base(patientId), {
      params: status ? { status } : undefined,
    });
  },

  async due(patientId: string): Promise<ImmunizationDueResponse> {
    return apiGet<ImmunizationDueResponse>(`${base(patientId)}/due`);
  },

  async getById(patientId: string, immunizationId: string): Promise<ImmunizationResponse> {
    return apiGet<ImmunizationResponse>(`${base(patientId)}/${immunizationId}`);
  },

  async create(
    patientId: string,
    payload: UpsertImmunizationPayload,
  ): Promise<ImmunizationResponse> {
    return apiPost<ImmunizationResponse>(base(patientId), payload);
  },

  async update(
    patientId: string,
    immunizationId: string,
    payload: UpsertImmunizationPayload,
  ): Promise<ImmunizationResponse> {
    return apiPut<ImmunizationResponse>(`${base(patientId)}/${immunizationId}`, payload);
  },

  async remove(patientId: string, immunizationId: string): Promise<void> {
    await apiDelete(`${base(patientId)}/${immunizationId}`);
  },
};
