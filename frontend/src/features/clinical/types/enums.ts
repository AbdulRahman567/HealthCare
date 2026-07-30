export const CONSULTATION_STATUSES = [
  'DRAFT',
  'IN_PROGRESS',
  'PAUSED',
  'COMPLETED',
  'CANCELLED',
] as const;
export type ConsultationStatus = (typeof CONSULTATION_STATUSES)[number];

export const EDITABLE_CONSULTATION_STATUSES: readonly ConsultationStatus[] = [
  'DRAFT',
  'IN_PROGRESS',
  'PAUSED',
];

export const DIAGNOSIS_TYPES = ['PRIMARY', 'SECONDARY', 'DIFFERENTIAL'] as const;
export type DiagnosisType = (typeof DIAGNOSIS_TYPES)[number];

export const DIAGNOSIS_STATUSES = ['PROVISIONAL', 'CONFIRMED', 'RULED_OUT', 'RESOLVED'] as const;
export type DiagnosisStatus = (typeof DIAGNOSIS_STATUSES)[number];

export const DIAGNOSIS_SEVERITIES = ['MILD', 'MODERATE', 'SEVERE', 'CRITICAL', 'UNKNOWN'] as const;
export type DiagnosisSeverity = (typeof DIAGNOSIS_SEVERITIES)[number];

export const CLINICAL_NOTE_TYPES = [
  'SUBJECTIVE',
  'OBJECTIVE',
  'ASSESSMENT',
  'PLAN',
  'PROGRESS',
  'PROCEDURE',
  'DISCHARGE',
  'ADVICE',
  'GENERAL',
] as const;
export type ClinicalNoteType = (typeof CLINICAL_NOTE_TYPES)[number];

export const FOLLOW_UP_STATUSES = [
  'PENDING',
  'SCHEDULED',
  'COMPLETED',
  'CANCELLED',
  'MISSED',
] as const;
export type FollowUpStatus = (typeof FOLLOW_UP_STATUSES)[number];

export const FOLLOW_UP_PRIORITIES = ['ROUTINE', 'URGENT'] as const;
export type FollowUpPriority = (typeof FOLLOW_UP_PRIORITIES)[number];

export const PRESCRIPTION_STATUSES = [
  'DRAFT',
  'ISSUED',
  'PARTIALLY_DISPENSED',
  'DISPENSED',
  'CANCELLED',
] as const;
export type PrescriptionStatus = (typeof PRESCRIPTION_STATUSES)[number];

export const MEDICATION_ROUTES = [
  'ORAL',
  'SUBLINGUAL',
  'TOPICAL',
  'INHALATION',
  'INTRAVENOUS',
  'INTRAMUSCULAR',
  'SUBCUTANEOUS',
  'RECTAL',
  'OPHTHALMIC',
  'OTIC',
  'NASAL',
  'TRANSDERMAL',
  'OTHER',
] as const;
export type MedicationRoute = (typeof MEDICATION_ROUTES)[number];

export const CONSULTATION_WORKSPACE_TABS = [
  'chart',
  'vitals',
  'diagnosis',
  'prescriptions',
  'notes',
  'follow-ups',
  'timeline',
] as const;
export type ConsultationWorkspaceTab = (typeof CONSULTATION_WORKSPACE_TABS)[number];
