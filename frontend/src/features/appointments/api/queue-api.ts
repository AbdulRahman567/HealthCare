import { apiClient } from '@/services/http/api-client';
import type {
  CheckInQueuePayload,
  DoctorDayQueueResponse,
  QueueAction,
  QueueEntryResponse,
  QueueStatusUpdatePayload,
} from '@/features/appointments/types/queue';
import type { ApiSuccessResponse } from '@/types/api';

export const queueApi = {
  async checkIn(payload: CheckInQueuePayload): Promise<QueueEntryResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<QueueEntryResponse>>(
      '/queues/check-in',
      payload,
    );
    return data.data;
  },

  async getDoctorDayQueue(doctorId: string, date?: string): Promise<DoctorDayQueueResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<DoctorDayQueueResponse>>('/queues', {
      params: { doctorId, date },
    });
    return data.data;
  },

  async getById(queueId: string): Promise<DoctorDayQueueResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<DoctorDayQueueResponse>>(
      `/queues/${queueId}`,
    );
    return data.data;
  },

  async getEntry(entryId: string): Promise<QueueEntryResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<QueueEntryResponse>>(
      `/queue-entries/${entryId}`,
    );
    return data.data;
  },

  async updateStatus(
    entryId: string,
    action: QueueAction,
    payload: QueueStatusUpdatePayload = {},
  ): Promise<QueueEntryResponse> {
    const { data } = await apiClient.patch<ApiSuccessResponse<QueueEntryResponse>>(
      `/queue-entries/${entryId}/${action}`,
      payload,
    );
    return data.data;
  },
};
