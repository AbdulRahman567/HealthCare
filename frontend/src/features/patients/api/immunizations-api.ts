import { apiClient } from '@/services/http/api-client';
import type {
  ImmunizationDueResponse,
  ImmunizationResponse,
  UpsertImmunizationPayload,
} from '@/features/patients/types/immunization';
import type { ImmunizationStatus } from '@/features/patients/types/enums';
import type { ApiSuccessResponse } from '@/types/api';

const base = (patientId: string) => `/patients/${patientId}/immunizations`;

export const immunizationsApi = {
  async list(patientId: string, status?: ImmunizationStatus): Promise<ImmunizationResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<ImmunizationResponse[]>>(
      base(patientId),
      { params: status ? { status } : undefined },
    );
    return data.data;
  },

  async due(patientId: string): Promise<ImmunizationDueResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<ImmunizationDueResponse>>(
      `${base(patientId)}/due`,
    );
    return data.data;
  },

  async getById(patientId: string, immunizationId: string): Promise<ImmunizationResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<ImmunizationResponse>>(
      `${base(patientId)}/${immunizationId}`,
    );
    return data.data;
  },

  async create(
    patientId: string,
    payload: UpsertImmunizationPayload,
  ): Promise<ImmunizationResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<ImmunizationResponse>>(
      base(patientId),
      payload,
    );
    return data.data;
  },

  async update(
    patientId: string,
    immunizationId: string,
    payload: UpsertImmunizationPayload,
  ): Promise<ImmunizationResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<ImmunizationResponse>>(
      `${base(patientId)}/${immunizationId}`,
      payload,
    );
    return data.data;
  },

  async remove(patientId: string, immunizationId: string): Promise<void> {
    await apiClient.delete(`${base(patientId)}/${immunizationId}`);
  },
};
