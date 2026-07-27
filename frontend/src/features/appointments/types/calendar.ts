import type { PageResponse } from '@/types/api';

import type {
  AppointmentStatus,
  AppointmentType,
  CalendarScope,
  CalendarViewType,
  VisitType,
} from './enums';

export type CalendarEventResponse = {
  id: string;
  appointmentNumber: string;
  hospitalId: string;
  departmentId: string;
  departmentName: string | null;
  doctorId: string;
  doctorName: string | null;
  doctorEmployeeCode: string | null;
  patientId: string;
  patientName: string | null;
  patientMrn: string | null;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  status: AppointmentStatus;
  appointmentType: AppointmentType;
  visitType: VisitType;
};

export type CalendarDayResponse = {
  date: string;
  totalCount: number;
  countsByStatus: Partial<Record<AppointmentStatus, number>>;
  events: CalendarEventResponse[];
};

export type CalendarDaySummaryResponse = {
  date: string;
  totalCount: number;
  countsByStatus: Partial<Record<AppointmentStatus, number>>;
};

export type CalendarRangeResponse = {
  scope: CalendarScope;
  scopeId: string;
  view: CalendarViewType;
  fromDate: string;
  toDate: string;
  days: CalendarDayResponse[];
  events: PageResponse<CalendarEventResponse>;
};

export type CalendarMonthResponse = {
  scope: CalendarScope;
  scopeId: string;
  year: number;
  month: number;
  totalAppointments: number;
  days: CalendarDaySummaryResponse[];
};

export type CalendarQuery = {
  date?: string;
  status?: AppointmentStatus;
  page?: number;
  size?: number;
  sort?: string | string[];
};

export type CalendarMonthQuery = {
  year: number;
  month: number;
  status?: AppointmentStatus;
};
