import type { ListQuery } from '@/types/api';

import type {
  AppointmentStatus,
  AppointmentType,
  QueueEntryStatus,
  VisitType,
} from './enums';

export type AppointmentResponse = {
  id: string;
  appointmentNumber: string;
  hospitalId: string;
  patientId: string;
  patientName?: string | null;
  patientMrn?: string | null;
  doctorId: string;
  departmentId: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  status: AppointmentStatus;
  appointmentType: AppointmentType;
  visitType: VisitType;
  notes: string | null;
  confirmedAt: string | null;
  cancelledAt: string | null;
  cancellationReason: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type CreateAppointmentPayload = {
  patientId: string;
  doctorId: string;
  departmentId: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  appointmentType: AppointmentType;
  visitType: VisitType;
  notes?: string | null;
};

export type UpdateAppointmentPayload = {
  doctorId: string;
  departmentId: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  appointmentType: AppointmentType;
  visitType: VisitType;
  notes?: string | null;
};

export type RescheduleAppointmentPayload = {
  doctorId: string;
  departmentId: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  durationMinutes: number;
};

export type CancelAppointmentPayload = {
  reason?: string | null;
};

export type AppointmentListQuery = ListQuery & {
  appointmentNumber?: string;
  patientId?: string;
  patientName?: string;
  doctorId?: string;
  doctorName?: string;
  departmentId?: string;
  departmentName?: string;
  status?: AppointmentStatus;
  visitType?: VisitType;
  fromDate?: string;
  toDate?: string;
  queueStatus?: QueueEntryStatus;
};
