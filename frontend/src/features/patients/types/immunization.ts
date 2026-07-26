import type { ImmunizationStatus, VaccineRoute } from './enums';

export type ImmunizationResponse = {
  id: string;
  patientId: string;
  vaccineName: string;
  vaccineCode: string | null;
  doseNumber: number;
  manufacturer: string | null;
  batchNumber: string | null;
  administrationDate: string;
  nextDueDate: string | null;
  healthcareProvider: string;
  route: VaccineRoute | null;
  status: ImmunizationStatus;
  clinicalNotes: string | null;
  due: boolean;
  recordedByUserId: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type ImmunizationDueResponse = {
  patientId: string;
  dueCount: number;
  dueImmunizations: ImmunizationResponse[];
};

export type UpsertImmunizationPayload = {
  vaccineName: string;
  vaccineCode?: string | null;
  doseNumber: number;
  manufacturer?: string | null;
  batchNumber?: string | null;
  administrationDate: string;
  nextDueDate?: string | null;
  healthcareProvider: string;
  route?: VaccineRoute | null;
  status?: ImmunizationStatus;
  clinicalNotes?: string | null;
};
