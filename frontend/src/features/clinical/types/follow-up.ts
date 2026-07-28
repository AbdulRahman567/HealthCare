import type { PageQuery } from '@/types/api';

import type { FollowUpPriority, FollowUpStatus } from './enums';

export type FollowUpReminderStatus = 'PENDING' | 'SENT' | 'SKIPPED' | 'FAILED';

export type FollowUpResponse = {
  id: string;
  consultationId: string;
  consultationNumber: string;
  patientId: string;
  doctorId: string;
  doctorName: string;
  scheduledDate: string;
  scheduledTime: string | null;
  status: FollowUpStatus;
  priority: FollowUpPriority;
  reason: string | null;
  instructions: string | null;
  clinicalRecommendations: string | null;
  followUpAppointmentId: string | null;
  reminderEnabled: boolean;
  reminderLeadDays: number | null;
  nextReminderAt: string | null;
  lastReminderAt: string | null;
  reminderStatus: FollowUpReminderStatus | null;
  createdAt: string;
  version: number;
};

export type CreateFollowUpPayload = {
  scheduledDate: string;
  scheduledTime?: string | null;
  status?: FollowUpStatus | null;
  priority?: FollowUpPriority | null;
  reason?: string | null;
  instructions?: string | null;
  clinicalRecommendations?: string | null;
  doctorId?: string | null;
  followUpAppointmentId?: string | null;
  reminderEnabled?: boolean | null;
  reminderLeadDays?: number | null;
};

export type UpdateFollowUpPayload = {
  scheduledDate?: string | null;
  scheduledTime?: string | null;
  status?: FollowUpStatus | null;
  priority?: FollowUpPriority | null;
  reason?: string | null;
  instructions?: string | null;
  clinicalRecommendations?: string | null;
  doctorId?: string | null;
  followUpAppointmentId?: string | null;
  reminderEnabled?: boolean | null;
  reminderLeadDays?: number | null;
};

export type UpdateFollowUpStatusPayload = {
  status: FollowUpStatus;
};

export type FollowUpSearchQuery = PageQuery & {
  patientId?: string;
  doctorId?: string;
  consultationId?: string;
  status?: FollowUpStatus;
  priority?: FollowUpPriority;
  fromDate?: string;
  toDate?: string;
  overdueOnly?: boolean;
  dueSoonOnly?: boolean;
  dueWithinDays?: number;
};

export type PatientFollowUpQuery = PageQuery & {
  status?: FollowUpStatus;
  fromDate?: string;
  toDate?: string;
};
