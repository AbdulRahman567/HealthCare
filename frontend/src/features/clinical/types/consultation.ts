import type { PageQuery } from '@/types/api';

import type { ConsultationStatus } from './enums';

export type ClinicalSummaryResponse = {
  chiefComplaint: string | null;
  historyOfPresentIllness: string | null;
  physicalExamination: string | null;
  doctorNotes: string | null;
  summary: string | null;
  advice: string | null;
};

export type ConsultationResponse = {
  id: string;
  consultationNumber: string;
  hospitalId: string;
  patientId: string;
  patientName: string;
  patientMrn: string;
  doctorId: string;
  doctorName: string;
  departmentId: string;
  departmentName: string;
  appointmentId: string | null;
  consultationDate: string;
  status: ConsultationStatus;
  startedAt: string | null;
  pausedAt: string | null;
  completedAt: string | null;
  clinicalSummary: ClinicalSummaryResponse;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type CreateConsultationPayload = {
  patientId: string;
  doctorId: string;
  departmentId: string;
  appointmentId?: string | null;
  chiefComplaint?: string | null;
  startImmediately?: boolean;
};

export type UpdateConsultationDocumentationPayload = {
  chiefComplaint?: string | null;
  historyOfPresentIllness?: string | null;
  physicalExamination?: string | null;
  doctorNotes?: string | null;
  summary?: string | null;
  advice?: string | null;
};

export type CompleteConsultationPayload = {
  summary?: string | null;
  advice?: string | null;
};

export type ConsultationListQuery = PageQuery & {
  consultationNumber?: string;
  patientId?: string;
  patientName?: string;
  doctorId?: string;
  doctorName?: string;
  departmentId?: string;
  status?: ConsultationStatus;
  fromDate?: string;
  toDate?: string;
  appointmentId?: string;
};
