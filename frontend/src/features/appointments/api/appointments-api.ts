import { apiClient } from '@/services/http/api-client';
import type {
  AppointmentListQuery,
  AppointmentResponse,
  CancelAppointmentPayload,
  CreateAppointmentPayload,
  RescheduleAppointmentPayload,
  UpdateAppointmentPayload,
} from '@/features/appointments/types/appointment';
import { toPageParams } from '@/lib/page-query';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const appointmentsApi = {
  async search(query: AppointmentListQuery = {}): Promise<PageResponse<AppointmentResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<AppointmentResponse>>>(
      '/appointments',
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async getById(id: string): Promise<AppointmentResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<AppointmentResponse>>(
      `/appointments/${id}`,
    );
    return data.data;
  },

  async create(payload: CreateAppointmentPayload): Promise<AppointmentResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<AppointmentResponse>>(
      '/appointments',
      payload,
    );
    return data.data;
  },

  async update(id: string, payload: UpdateAppointmentPayload): Promise<AppointmentResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<AppointmentResponse>>(
      `/appointments/${id}`,
      payload,
    );
    return data.data;
  },

  async reschedule(
    id: string,
    payload: RescheduleAppointmentPayload,
  ): Promise<AppointmentResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<AppointmentResponse>>(
      `/appointments/${id}/reschedule`,
      payload,
    );
    return data.data;
  },

  async cancel(id: string, payload: CancelAppointmentPayload = {}): Promise<AppointmentResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<AppointmentResponse>>(
      `/appointments/${id}/cancel`,
      payload,
    );
    return data.data;
  },

  async confirm(id: string): Promise<AppointmentResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<AppointmentResponse>>(
      `/appointments/${id}/confirm`,
    );
    return data.data;
  },
};
