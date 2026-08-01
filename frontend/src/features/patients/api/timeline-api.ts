import { apiGet } from '@/services/http/api';
import type { TimelinePageResponse, TimelineQuery } from '@/features/patients/types/timeline';

export const timelineApi = {
  async list(patientId: string, query: TimelineQuery = {}): Promise<TimelinePageResponse> {
    const params: Record<string, unknown> = {};
    if (query.cursor) {
      params.cursor = query.cursor;
    }
    if (query.size !== undefined) {
      params.size = query.size;
    }
    if (query.direction) {
      params.direction = query.direction;
    }
    if (query.types?.length) {
      params.types = query.types;
    }

    return apiGet<TimelinePageResponse>(`/patients/${patientId}/timeline`, {
      params,
    });
  },
};
