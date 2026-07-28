import type { PageQuery } from '@/types/api';

import type { DiagnosisSeverity, DiagnosisStatus, DiagnosisType } from './enums';

export type DiagnosisResponse = {
  id: string;
  consultationId: string;
  consultationNumber: string;
  patientId: string;
  diagnosingDoctorId: string;
  diagnosingDoctorName: string;
  diagnosisName: string;
  icdCode: string | null;
  diagnosisType: DiagnosisType;
  diagnosisStatus: DiagnosisStatus;
  severity: DiagnosisSeverity;
  diagnosedAt: string;
  sequenceNumber: number;
  clinicalNotes: string | null;
  createdAt: string;
  version: number;
};

export type CreateDiagnosisPayload = {
  diagnosisName: string;
  icdCode?: string | null;
  diagnosisType: DiagnosisType;
  diagnosisStatus?: DiagnosisStatus | null;
  severity?: DiagnosisSeverity | null;
  sequenceNumber?: number | null;
  clinicalNotes?: string | null;
  diagnosingDoctorId?: string | null;
  diagnosedAt?: string | null;
};

export type UpdateDiagnosisPayload = {
  diagnosisName?: string | null;
  icdCode?: string | null;
  diagnosisType?: DiagnosisType | null;
  diagnosisStatus?: DiagnosisStatus | null;
  severity?: DiagnosisSeverity | null;
  sequenceNumber?: number | null;
  clinicalNotes?: string | null;
  diagnosingDoctorId?: string | null;
  diagnosedAt?: string | null;
};

export type PatientDiagnosisQuery = PageQuery & {
  diagnosisType?: DiagnosisType;
  diagnosisStatus?: DiagnosisStatus;
  fromDate?: string;
  toDate?: string;
};
