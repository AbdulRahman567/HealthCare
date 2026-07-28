import { apiClient } from '@/services/http/api-client';
import type {
  MedicalHistoryResponse,
  PastDiseaseResponse,
  SurgeryHistoryResponse,
  ChronicConditionResponse,
  FamilyHistoryResponse,
  UpsertPastDiseasePayload,
  UpsertSurgeryHistoryPayload,
  UpsertChronicConditionPayload,
  UpsertFamilyHistoryPayload,
} from '@/features/patients/types/medical-history';
import type { ApiSuccessResponse } from '@/types/api';

const base = (patientId: string) => `/patients/${patientId}/medical-history`;

export const medicalHistoryApi = {
  async get(patientId: string): Promise<MedicalHistoryResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<MedicalHistoryResponse>>(
      base(patientId),
    );
    return data.data;
  },

  async createPastDisease(
    patientId: string,
    payload: UpsertPastDiseasePayload,
  ): Promise<PastDiseaseResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<PastDiseaseResponse>>(
      `${base(patientId)}/past-diseases`,
      payload,
    );
    return data.data;
  },

  async updatePastDisease(
    patientId: string,
    entryId: string,
    payload: UpsertPastDiseasePayload,
  ): Promise<PastDiseaseResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<PastDiseaseResponse>>(
      `${base(patientId)}/past-diseases/${entryId}`,
      payload,
    );
    return data.data;
  },

  async deletePastDisease(patientId: string, entryId: string): Promise<void> {
    await apiClient.delete(`${base(patientId)}/past-diseases/${entryId}`);
  },

  async createSurgery(
    patientId: string,
    payload: UpsertSurgeryHistoryPayload,
  ): Promise<SurgeryHistoryResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<SurgeryHistoryResponse>>(
      `${base(patientId)}/surgeries`,
      payload,
    );
    return data.data;
  },

  async updateSurgery(
    patientId: string,
    entryId: string,
    payload: UpsertSurgeryHistoryPayload,
  ): Promise<SurgeryHistoryResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<SurgeryHistoryResponse>>(
      `${base(patientId)}/surgeries/${entryId}`,
      payload,
    );
    return data.data;
  },

  async deleteSurgery(patientId: string, entryId: string): Promise<void> {
    await apiClient.delete(`${base(patientId)}/surgeries/${entryId}`);
  },

  async createChronicCondition(
    patientId: string,
    payload: UpsertChronicConditionPayload,
  ): Promise<ChronicConditionResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<ChronicConditionResponse>>(
      `${base(patientId)}/chronic-conditions`,
      payload,
    );
    return data.data;
  },

  async updateChronicCondition(
    patientId: string,
    entryId: string,
    payload: UpsertChronicConditionPayload,
  ): Promise<ChronicConditionResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<ChronicConditionResponse>>(
      `${base(patientId)}/chronic-conditions/${entryId}`,
      payload,
    );
    return data.data;
  },

  async deleteChronicCondition(patientId: string, entryId: string): Promise<void> {
    await apiClient.delete(`${base(patientId)}/chronic-conditions/${entryId}`);
  },

  async createFamilyHistory(
    patientId: string,
    payload: UpsertFamilyHistoryPayload,
  ): Promise<FamilyHistoryResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<FamilyHistoryResponse>>(
      `${base(patientId)}/family-histories`,
      payload,
    );
    return data.data;
  },

  async updateFamilyHistory(
    patientId: string,
    entryId: string,
    payload: UpsertFamilyHistoryPayload,
  ): Promise<FamilyHistoryResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<FamilyHistoryResponse>>(
      `${base(patientId)}/family-histories/${entryId}`,
      payload,
    );
    return data.data;
  },

  async deleteFamilyHistory(patientId: string, entryId: string): Promise<void> {
    await apiClient.delete(`${base(patientId)}/family-histories/${entryId}`);
  },
};
