import type { PageQuery } from '@/types/api';

import type { ClinicalNoteType } from './enums';

export type ClinicalNoteAttachmentResponse = {
  id: string;
  clinicalNoteId: string;
  consultationId: string;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  attachmentKind: 'IMAGE' | 'PDF' | 'OTHER';
  description: string | null;
  uploadedByUserId: string | null;
  createdAt: string;
  version: number;
};

export type ClinicalNoteResponse = {
  id: string;
  consultationId: string;
  consultationNumber: string;
  patientId: string;
  authorDoctorId: string;
  authorDoctorName: string;
  noteType: ClinicalNoteType;
  title: string | null;
  content: string;
  recordedAt: string;
  attachments: ClinicalNoteAttachmentResponse[];
  createdAt: string;
  version: number;
};

export type CreateClinicalNotePayload = {
  noteType: ClinicalNoteType;
  title?: string | null;
  content: string;
  authorDoctorId?: string | null;
  recordedAt?: string | null;
};

export type UpdateClinicalNotePayload = {
  noteType?: ClinicalNoteType | null;
  title?: string | null;
  content?: string | null;
  authorDoctorId?: string | null;
  recordedAt?: string | null;
};

export type PatientClinicalNotesQuery = PageQuery & {
  noteType?: ClinicalNoteType;
  fromDate?: string;
  toDate?: string;
};
