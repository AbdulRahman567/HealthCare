import { apiGet, apiPatch, apiPost, apiPut } from '@/services/http/api';
import type {
  AppointmentListQuery,
  AppointmentResponse,
  CancelAppointmentPayload,
  CreateAppointmentPayload,
  RescheduleAppointmentPayload,
  UpdateAppointmentPayload,
} from '@/features/appointments/types/appointment';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const appointmentsApi = {
  async search(query: AppointmentListQuery = {}): Promise<PageResponse<AppointmentResponse>> {
    return apiGet<PageResponse<AppointmentResponse>>('/appointments', {
      params: toPageParams(query),
    });
  },

  async getById(id: string): Promise<AppointmentResponse> {
    return apiGet<AppointmentResponse>(`/appointments/${id}`);
  },

  async create(payload: CreateAppointmentPayload): Promise<AppointmentResponse> {
    return apiPost<AppointmentResponse>('/appointments', payload);
  },

  async update(id: string, payload: UpdateAppointmentPayload): Promise<AppointmentResponse> {
    return apiPut<AppointmentResponse>(`/appointments/${id}`, payload);
  },

  async reschedule(
    id: string,
    payload: RescheduleAppointmentPayload,
  ): Promise<AppointmentResponse> {
    return apiPatch<AppointmentResponse>(`/appointments/${id}/reschedule`, payload);
  },

  async cancel(id: string, payload: CancelAppointmentPayload = {}): Promise<AppointmentResponse> {
    return apiPatch<AppointmentResponse>(`/appointments/${id}/cancel`, payload);
  },

  async confirm(id: string): Promise<AppointmentResponse> {
    return apiPatch<AppointmentResponse>(`/appointments/${id}/confirm`);
  },
};
