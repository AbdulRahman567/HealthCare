import { apiDelete, apiGet, apiPost, apiPut } from '@/services/http/api';
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

const base = (patientId: string) => `/patients/${patientId}/medical-history`;

export const medicalHistoryApi = {
  async get(patientId: string): Promise<MedicalHistoryResponse> {
    return apiGet<MedicalHistoryResponse>(base(patientId));
  },

  async createPastDisease(
    patientId: string,
    payload: UpsertPastDiseasePayload,
  ): Promise<PastDiseaseResponse> {
    return apiPost<PastDiseaseResponse>(`${base(patientId)}/past-diseases`, payload);
  },

  async updatePastDisease(
    patientId: string,
    entryId: string,
    payload: UpsertPastDiseasePayload,
  ): Promise<PastDiseaseResponse> {
    return apiPut<PastDiseaseResponse>(`${base(patientId)}/past-diseases/${entryId}`, payload);
  },

  async deletePastDisease(patientId: string, entryId: string): Promise<void> {
    await apiDelete(`${base(patientId)}/past-diseases/${entryId}`);
  },

  async createSurgery(
    patientId: string,
    payload: UpsertSurgeryHistoryPayload,
  ): Promise<SurgeryHistoryResponse> {
    return apiPost<SurgeryHistoryResponse>(`${base(patientId)}/surgeries`, payload);
  },

  async updateSurgery(
    patientId: string,
    entryId: string,
    payload: UpsertSurgeryHistoryPayload,
  ): Promise<SurgeryHistoryResponse> {
    return apiPut<SurgeryHistoryResponse>(`${base(patientId)}/surgeries/${entryId}`, payload);
  },

  async deleteSurgery(patientId: string, entryId: string): Promise<void> {
    await apiDelete(`${base(patientId)}/surgeries/${entryId}`);
  },

  async createChronicCondition(
    patientId: string,
    payload: UpsertChronicConditionPayload,
  ): Promise<ChronicConditionResponse> {
    return apiPost<ChronicConditionResponse>(`${base(patientId)}/chronic-conditions`, payload);
  },

  async updateChronicCondition(
    patientId: string,
    entryId: string,
    payload: UpsertChronicConditionPayload,
  ): Promise<ChronicConditionResponse> {
    return apiPut<ChronicConditionResponse>(
      `${base(patientId)}/chronic-conditions/${entryId}`,
      payload,
    );
  },

  async deleteChronicCondition(patientId: string, entryId: string): Promise<void> {
    await apiDelete(`${base(patientId)}/chronic-conditions/${entryId}`);
  },

  async createFamilyHistory(
    patientId: string,
    payload: UpsertFamilyHistoryPayload,
  ): Promise<FamilyHistoryResponse> {
    return apiPost<FamilyHistoryResponse>(`${base(patientId)}/family-histories`, payload);
  },

  async updateFamilyHistory(
    patientId: string,
    entryId: string,
    payload: UpsertFamilyHistoryPayload,
  ): Promise<FamilyHistoryResponse> {
    return apiPut<FamilyHistoryResponse>(`${base(patientId)}/family-histories/${entryId}`, payload);
  },

  async deleteFamilyHistory(patientId: string, entryId: string): Promise<void> {
    await apiDelete(`${base(patientId)}/family-histories/${entryId}`);
  },
};
