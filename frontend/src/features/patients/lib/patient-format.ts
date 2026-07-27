import type { PatientResponse, PatientWritePayload } from '@/features/patients/types/patient';
import type { PatientFormValues } from '@/features/patients/validation/patient-schema';
import { BLOOD_GROUP_LABELS, type BloodGroup } from '@/features/patients/types/enums';
import { formatEnumLabel, formatPersonName } from '@/lib/page-query';

export function emptyToNull(value?: string | null): string | null {
  const trimmed = value?.trim();
  return trimmed ? trimmed : null;
}

export function patientToFormValues(patient: PatientResponse): PatientFormValues {
  return {
    mrn: patient.mrn,
    firstName: patient.firstName,
    lastName: patient.lastName,
    dateOfBirth: patient.dateOfBirth,
    gender: patient.gender,
    bloodGroup: patient.bloodGroup ?? '',
    nationalId: patient.nationalId ?? '',
    phone: patient.phone ?? '',
    email: patient.email ?? '',
    address: patient.address ?? '',
    maritalStatus: patient.maritalStatus ?? '',
    emergencyContactName: patient.emergencyContact?.name ?? '',
    emergencyContactPhone: patient.emergencyContact?.phone ?? '',
    emergencyContactRelation: patient.emergencyContact?.relation ?? '',
  };
}

export function formValuesToPatientPayload(values: PatientFormValues): PatientWritePayload {
  const emergencyName = emptyToNull(values.emergencyContactName);
  const emergencyPhone = emptyToNull(values.emergencyContactPhone);
  const emergencyRelation = emptyToNull(values.emergencyContactRelation);
  const hasEmergency = Boolean(emergencyName || emergencyPhone || emergencyRelation);

  return {
    mrn: values.mrn.trim(),
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    dateOfBirth: values.dateOfBirth,
    gender: values.gender,
    bloodGroup: values.bloodGroup ? (values.bloodGroup as BloodGroup) : null,
    nationalId: emptyToNull(values.nationalId),
    phone: emptyToNull(values.phone),
    email: emptyToNull(values.email),
    address: emptyToNull(values.address),
    maritalStatus: values.maritalStatus
      ? (values.maritalStatus as PatientWritePayload['maritalStatus'])
      : null,
    emergencyContact: hasEmergency
      ? {
          name: emergencyName,
          phone: emergencyPhone,
          relation: emergencyRelation,
        }
      : null,
  };
}

export function patientDisplayName(
  patient: Pick<PatientResponse, 'firstName' | 'lastName'>,
): string {
  return formatPersonName(patient.firstName, patient.lastName);
}

export function formatBloodGroup(value: BloodGroup | null | undefined): string {
  if (!value) {
    return '—';
  }
  return BLOOD_GROUP_LABELS[value] ?? formatEnumLabel(value);
}

export function calculateAge(dateOfBirth: string, asOf: Date = new Date()): number | null {
  const dob = new Date(dateOfBirth);
  if (Number.isNaN(dob.getTime())) {
    return null;
  }
  let age = asOf.getFullYear() - dob.getFullYear();
  const monthDiff = asOf.getMonth() - dob.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && asOf.getDate() < dob.getDate())) {
    age -= 1;
  }
  return age >= 0 ? age : null;
}

export function formatDate(value: string | null | undefined): string {
  if (!value) {
    return '—';
  }
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date);
}
