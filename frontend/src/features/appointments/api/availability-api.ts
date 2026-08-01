import { apiGet, apiPost } from '@/services/http/api';

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
    return apiGet<DoctorScheduleResponse[]>(`/doctors/${doctorId}/schedules`);
  },

  async createSchedule(
    doctorId: string,
    payload: UpsertDoctorSchedulePayload,
  ): Promise<DoctorScheduleResponse> {
    return apiPost<DoctorScheduleResponse>(`/doctors/${doctorId}/schedules`, payload);
  },
};
