import type { ListQuery } from '@/types/api';

import type { BloodGroup, Gender, MaritalStatus, PatientStatus } from './enums';

export type EmergencyContact = {
  name: string;
  phone: string;
  relation: string;
};

export type EmergencyContactPayload = {
  name?: string | null;
  phone?: string | null;
  relation?: string | null;
};

export type PatientResponse = {
  id: string;
  tenantId: string;
  mrn: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  bloodGroup: BloodGroup | null;
  nationalId: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  emergencyContact: EmergencyContact | null;
  maritalStatus: MaritalStatus | null;
  status: PatientStatus;
  primaryDepartmentId: string | null;
  primaryDoctorId: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
  version: number;
};

export type PatientWritePayload = {
  mrn: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  bloodGroup?: BloodGroup | null;
  nationalId?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  emergencyContact?: EmergencyContactPayload | null;
  maritalStatus?: MaritalStatus | null;
};

export type PatientListQuery = ListQuery & {
  mrn?: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  email?: string;
  nationalId?: string;
  status?: PatientStatus;
  bloodGroup?: BloodGroup;
  gender?: Gender;
  dateOfBirth?: string;
  dateOfBirthFrom?: string;
  dateOfBirthTo?: string;
  ageMin?: number;
  ageMax?: number;
  departmentId?: string;
  doctorId?: string;
};
