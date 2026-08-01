import { apiDelete, apiGet, apiPatch, apiPost, apiPut } from '@/services/http/api';
import type {
  CancelPrescriptionPayload,
  CreatePrescriptionPayload,
  PrescriptionListQuery,
  PrescriptionResponse,
  UpdatePrescriptionPayload,
} from '@/features/clinical/types/prescription';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const prescriptionsApi = {
  async listForConsultation(consultationId: string): Promise<PrescriptionResponse[]> {
    return apiGet<PrescriptionResponse[]>(`/consultations/${consultationId}/prescriptions`);
  },

  async search(query: PrescriptionListQuery = {}): Promise<PageResponse<PrescriptionResponse>> {
    return apiGet<PageResponse<PrescriptionResponse>>('/prescriptions', {
      params: toPageParams(query),
    });
  },

  async getById(id: string): Promise<PrescriptionResponse> {
    return apiGet<PrescriptionResponse>(`/prescriptions/${id}`);
  },

  async create(payload: CreatePrescriptionPayload): Promise<PrescriptionResponse> {
    return apiPost<PrescriptionResponse>('/prescriptions', payload);
  },

  async update(id: string, payload: UpdatePrescriptionPayload): Promise<PrescriptionResponse> {
    return apiPut<PrescriptionResponse>(`/prescriptions/${id}`, payload);
  },

  async issue(id: string): Promise<PrescriptionResponse> {
    return apiPatch<PrescriptionResponse>(`/prescriptions/${id}/issue`);
  },

  async cancel(id: string, payload: CancelPrescriptionPayload = {}): Promise<PrescriptionResponse> {
    return apiPatch<PrescriptionResponse>(`/prescriptions/${id}/cancel`, payload);
  },

  async remove(id: string): Promise<void> {
    await apiDelete(`/prescriptions/${id}`);
  },

  async listForPatient(
    patientId: string,
    query: PageQueryLike = {},
  ): Promise<PageResponse<PrescriptionResponse>> {
    return apiGet<PageResponse<PrescriptionResponse>>(`/patients/${patientId}/prescriptions`, {
      params: toPageParams(query),
    });
  },
};

type PageQueryLike = {
  page?: number;
  size?: number;
  sort?: string | string[];
};
