import type { PageQuery } from '@/types/api';

export type BloodPressureResponse = {
  systolicMmHg: number | null;
  diastolicMmHg: number | null;
};

export type VitalSignsResponse = {
  id: string;
  consultationId: string;
  consultationNumber: string;
  patientId: string;
  recordedAt: string;
  recordedByUserId: string | null;
  recordedByName: string | null;
  temperatureCelsius: number | null;
  heartRateBpm: number | null;
  bloodPressure: BloodPressureResponse | null;
  respiratoryRate: number | null;
  oxygenSaturationPercent: number | null;
  heightCm: number | null;
  weightKg: number | null;
  bmi: number | null;
  painScale: number | null;
  notes: string | null;
  createdAt: string;
  version: number;
};

export type RecordVitalSignsPayload = {
  temperatureCelsius?: number | null;
  heartRateBpm?: number | null;
  systolicBp?: number | null;
  diastolicBp?: number | null;
  respiratoryRate?: number | null;
  oxygenSaturationPercent?: number | null;
  heightCm?: number | null;
  weightKg?: number | null;
  painScale?: number | null;
  notes?: string | null;
  recordedAt?: string | null;
};

export type UpdateVitalSignsPayload = RecordVitalSignsPayload;

export type PatientVitalSignsQuery = PageQuery & {
  fromDate?: string;
  toDate?: string;
};
