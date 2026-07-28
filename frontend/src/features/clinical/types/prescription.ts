import type { PageQuery } from '@/types/api';

import type { MedicationRoute, PrescriptionStatus } from './enums';

export type PrescriptionItemResponse = {
  id: string;
  prescriptionId: string;
  medicineName: string;
  medicineId: string | null;
  medicineCode: string | null;
  dosage: string;
  frequency: string;
  route: MedicationRoute;
  duration: string;
  instructions: string | null;
  quantity: number;
  refills: number | null;
  sequenceNumber: number;
  beforeFood: boolean | null;
  afterFood: boolean | null;
  createdAt: string;
  version: number;
};

export type PrescriptionResponse = {
  id: string;
  prescriptionNumber: string;
  consultationId: string;
  consultationNumber: string;
  hospitalId: string;
  patientId: string;
  patientName: string;
  patientMrn: string;
  doctorId: string;
  doctorName: string;
  departmentId: string;
  departmentName: string;
  prescriptionDate: string;
  status: PrescriptionStatus;
  notes: string | null;
  issuedAt: string | null;
  cancelledAt: string | null;
  cancelReason: string | null;
  dispensedAt: string | null;
  pharmacyReference: string | null;
  items: PrescriptionItemResponse[];
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type PrescriptionItemPayload = {
  medicineName: string;
  medicineId?: string | null;
  medicineCode?: string | null;
  dosage: string;
  frequency: string;
  route: MedicationRoute;
  duration: string;
  instructions?: string | null;
  quantity: number;
  refills?: number | null;
  sequenceNumber?: number | null;
  beforeFood?: boolean | null;
  afterFood?: boolean | null;
};

export type CreatePrescriptionPayload = {
  consultationId: string;
  prescriptionDate?: string | null;
  notes?: string | null;
  issueImmediately?: boolean | null;
  items: PrescriptionItemPayload[];
};

export type UpdatePrescriptionPayload = {
  prescriptionDate?: string | null;
  notes?: string | null;
  items?: PrescriptionItemPayload[] | null;
};

export type CancelPrescriptionPayload = {
  reason?: string | null;
};

export type PrescriptionListQuery = PageQuery & {
  prescriptionNumber?: string;
  patientId?: string;
  doctorId?: string;
  consultationId?: string;
  status?: PrescriptionStatus;
  fromDate?: string;
  toDate?: string;
};
