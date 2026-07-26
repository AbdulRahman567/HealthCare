import { apiClient } from '@/services/http/api-client';
import type { TimelinePageResponse, TimelineQuery } from '@/features/patients/types/timeline';
import type { ApiSuccessResponse } from '@/types/api';

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

    const { data } = await apiClient.get<ApiSuccessResponse<TimelinePageResponse>>(
      `/patients/${patientId}/timeline`,
      { params },
    );
    return data.data;
  },
};
