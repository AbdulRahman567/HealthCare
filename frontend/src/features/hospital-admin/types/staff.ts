import type { EmploymentStatus, EmploymentType, StaffShift, StaffType } from './enums';
import type { ListQuery } from '@/types/api';

export type StaffBase = {
  id: string;
  tenantId: string;
  hospitalId: string;
  userId: string;
  departmentId: string;
  reportsToStaffId: string | null;
  employeeCode: string;
  jobTitle: string | null;
  employmentStatus: EmploymentStatus;
  employmentType: EmploymentType;
  hiredAt: string | null;
  terminatedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
  version: number;
};

export type DoctorResponse = StaffBase & {
  specialization: string;
  licenseNumber: string;
  qualification: string | null;
  experienceYears: number | null;
  consultationFee: number | null;
};

export type NurseResponse = StaffBase & {
  shift: StaffShift;
  qualification: string | null;
  licenseNumber: string | null;
};

export type ReceptionistResponse = StaffBase & {
  deskLocation: string | null;
  languages: string | null;
};

export type LaboratoryStaffResponse = StaffBase & {
  specialtyArea: string | null;
  licenseNumber: string | null;
  certification: string | null;
};

export type PharmacistResponse = StaffBase & {
  licenseNumber: string;
  pharmacyLocation: string | null;
  qualification: string | null;
};

export type StaffProfile =
  | DoctorResponse
  | NurseResponse
  | ReceptionistResponse
  | LaboratoryStaffResponse
  | PharmacistResponse;

export type StaffEmploymentFields = {
  userId: string;
  departmentId: string;
  employeeCode: string;
  jobTitle?: string | null;
  employmentStatus: EmploymentStatus;
  employmentType: EmploymentType;
  hiredAt?: string | null;
  terminatedAt?: string | null;
  reportsToStaffId?: string | null;
};

export type CreateDoctorPayload = StaffEmploymentFields & {
  specialization: string;
  licenseNumber: string;
  qualification?: string | null;
  experienceYears?: number | null;
  consultationFee?: number | null;
};

export type CreateNursePayload = StaffEmploymentFields & {
  shift: StaffShift;
  qualification?: string | null;
  licenseNumber?: string | null;
};

export type CreateReceptionistPayload = StaffEmploymentFields & {
  deskLocation?: string | null;
  languages?: string | null;
};

export type CreateLaboratoryStaffPayload = StaffEmploymentFields & {
  specialtyArea?: string | null;
  licenseNumber?: string | null;
  certification?: string | null;
};

export type CreatePharmacistPayload = StaffEmploymentFields & {
  licenseNumber: string;
  pharmacyLocation?: string | null;
  qualification?: string | null;
};

export type StaffListQuery = ListQuery & {
  employmentStatus?: EmploymentStatus;
  departmentId?: string;
};

export type StaffResourceKey =
  'doctors' | 'nurses' | 'receptionists' | 'laboratory-staff' | 'pharmacists';

export const STAFF_TYPE_TO_RESOURCE: Record<StaffType, StaffResourceKey> = {
  DOCTOR: 'doctors',
  NURSE: 'nurses',
  RECEPTIONIST: 'receptionists',
  LABORATORY_STAFF: 'laboratory-staff',
  PHARMACIST: 'pharmacists',
};

export const STAFF_RESOURCE_LABEL: Record<StaffType, string> = {
  DOCTOR: 'Doctors',
  NURSE: 'Nurses',
  RECEPTIONIST: 'Receptionists',
  LABORATORY_STAFF: 'Laboratory staff',
  PHARMACIST: 'Pharmacists',
};
