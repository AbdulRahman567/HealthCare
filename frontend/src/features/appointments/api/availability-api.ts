import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse } from '@/types/api';

export type ScheduleWindow = {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
};

export type DoctorScheduleResponse = {
  id: string;
  doctorId: string;
  hospitalId: string;
  name: string | null;
  effectiveFrom: string;
  effectiveTo: string | null;
  maxAppointmentsPerDay: number;
  recurrenceType: string;
  status: string;
  notes: string | null;
  windows: ScheduleWindow[];
};

export type UpsertDoctorSchedulePayload = {
  name?: string | null;
  effectiveFrom: string;
  effectiveTo?: string | null;
  maxAppointmentsPerDay: number;
  recurrenceType?: string;
  status?: string;
  notes?: string | null;
  windows: ScheduleWindow[];
  breaks?: unknown[];
};

export const availabilityApi = {
  async listSchedules(doctorId: string): Promise<DoctorScheduleResponse[]> {
    const { data } = await apiClient.get<ApiSuccessResponse<DoctorScheduleResponse[]>>(
      `/doctors/${doctorId}/schedules`,
    );
    return data.data;
  },

  async createSchedule(
    doctorId: string,
    payload: UpsertDoctorSchedulePayload,
  ): Promise<DoctorScheduleResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<DoctorScheduleResponse>>(
      `/doctors/${doctorId}/schedules`,
      payload,
    );
    return data.data;
  },
};
