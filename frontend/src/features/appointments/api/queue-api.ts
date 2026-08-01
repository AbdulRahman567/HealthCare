import { apiGet, apiPatch, apiPost } from '@/services/http/api';
import type {
  CheckInQueuePayload,
  DoctorDayQueueResponse,
  QueueAction,
  QueueEntryResponse,
  QueueStatusUpdatePayload,
} from '@/features/appointments/types/queue';

export const queueApi = {
  async checkIn(payload: CheckInQueuePayload): Promise<QueueEntryResponse> {
    return apiPost<QueueEntryResponse>('/queues/check-in', payload);
  },

  async getDoctorDayQueue(doctorId: string, date?: string): Promise<DoctorDayQueueResponse> {
    return apiGet<DoctorDayQueueResponse>('/queues', {
      params: { doctorId, date },
    });
  },

  async getById(queueId: string): Promise<DoctorDayQueueResponse> {
    return apiGet<DoctorDayQueueResponse>(`/queues/${queueId}`);
  },

  async getEntry(entryId: string): Promise<QueueEntryResponse> {
    return apiGet<QueueEntryResponse>(`/queue-entries/${entryId}`);
  },

  async updateStatus(
    entryId: string,
    action: QueueAction,
    payload: QueueStatusUpdatePayload = {},
  ): Promise<QueueEntryResponse> {
    return apiPatch<QueueEntryResponse>(`/queue-entries/${entryId}/${action}`, payload);
  },
};
