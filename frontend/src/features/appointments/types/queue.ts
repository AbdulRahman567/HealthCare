import type { QueueEntryStatus } from './enums';

export type QueueEntryResponse = {
  id: string;
  queueId: string;
  appointmentId: string;
  patientId: string;
  patientName?: string | null;
  doctorId: string;
  hospitalId: string;
  queueNumber: number;
  status: QueueEntryStatus;
  checkedInAt: string;
  statusChangedAt: string;
  notes: string | null;
  consultationId?: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type DoctorDayQueueResponse = {
  id: string;
  doctorId: string;
  hospitalId: string;
  queueDate: string;
  lastQueueNumber: number;
  waitingCount: number;
  inConsultationCount: number;
  entries: QueueEntryResponse[];
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type CheckInQueuePayload = {
  appointmentId: string;
  notes?: string | null;
};

export type QueueStatusUpdatePayload = {
  notes?: string | null;
};

export type QueueAction = 'waiting' | 'start-consultation' | 'complete' | 'missed' | 'cancel';
