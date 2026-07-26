import { apiClient } from '@/services/http/api-client';
import type {
  AllergyBannerResponse,
  AllergyCriticalAlertResponse,
  AllergyResponse,
  UpsertAllergyPayload,
} from '@/features/patients/types/allergy';
import type { AllergyType } from '@/features/patients/types/enums';
import type { ApiSuccessResponse } from '@/types/api';

const base = (patientId: string) => `/patients/${patientId}/allergies`;

export const allergiesApi = {
  async list(patientId: string, type?: AllergyType): Promise<AllergyResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<AllergyResponse[]>>(base(patientId), {
      params: type ? { type } : undefined,
    });
    return data.data;
  },

  async banner(patientId: string): Promise<AllergyBannerResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<AllergyBannerResponse>>(
      `${base(patientId)}/banner`,
    );
    return data.data;
  },

  async critical(patientId: string): Promise<AllergyCriticalAlertResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<AllergyCriticalAlertResponse>>(
      `${base(patientId)}/critical`,
    );
    return data.data;
  },

  async getById(patientId: string, allergyId: string): Promise<AllergyResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<AllergyResponse>>(
      `${base(patientId)}/${allergyId}`,
    );
    return data.data;
  },

  async create(patientId: string, payload: UpsertAllergyPayload): Promise<AllergyResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<AllergyResponse>>(
      base(patientId),
      payload,
    );
    return data.data;
  },

  async update(
    patientId: string,
    allergyId: string,
    payload: UpsertAllergyPayload,
  ): Promise<AllergyResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<AllergyResponse>>(
      `${base(patientId)}/${allergyId}`,
      payload,
    );
    return data.data;
  },

  async remove(patientId: string, allergyId: string): Promise<void> {
    await apiClient.delete(`${base(patientId)}/${allergyId}`);
  },
};
