import { apiDelete, apiGet, apiPost, apiPut } from '@/services/http/api';
import type {
  AllergyBannerResponse,
  AllergyCriticalAlertResponse,
  AllergyResponse,
  UpsertAllergyPayload,
} from '@/features/patients/types/allergy';
import type { AllergyType } from '@/features/patients/types/enums';

const base = (patientId: string) => `/patients/${patientId}/allergies`;

export const allergiesApi = {
  async list(patientId: string, type?: AllergyType): Promise<AllergyResponse[]> {
    return apiGet<AllergyResponse[]>(base(patientId), {
      params: type ? { type } : undefined,
    });
  },

  async banner(patientId: string): Promise<AllergyBannerResponse> {
    return apiGet<AllergyBannerResponse>(`${base(patientId)}/banner`);
  },

  async critical(patientId: string): Promise<AllergyCriticalAlertResponse> {
    return apiGet<AllergyCriticalAlertResponse>(`${base(patientId)}/critical`);
  },

  async getById(patientId: string, allergyId: string): Promise<AllergyResponse> {
    return apiGet<AllergyResponse>(`${base(patientId)}/${allergyId}`);
  },

  async create(patientId: string, payload: UpsertAllergyPayload): Promise<AllergyResponse> {
    return apiPost<AllergyResponse>(base(patientId), payload);
  },

  async update(
    patientId: string,
    allergyId: string,
    payload: UpsertAllergyPayload,
  ): Promise<AllergyResponse> {
    return apiPut<AllergyResponse>(`${base(patientId)}/${allergyId}`, payload);
  },

  async remove(patientId: string, allergyId: string): Promise<void> {
    await apiDelete(`${base(patientId)}/${allergyId}`);
  },
};
