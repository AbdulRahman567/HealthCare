export const GENDERS = ['MALE', 'FEMALE', 'OTHER', 'UNKNOWN'] as const;
export type Gender = (typeof GENDERS)[number];

export const BLOOD_GROUPS = [
  'A_POSITIVE',
  'A_NEGATIVE',
  'B_POSITIVE',
  'B_NEGATIVE',
  'AB_POSITIVE',
  'AB_NEGATIVE',
  'O_POSITIVE',
  'O_NEGATIVE',
  'UNKNOWN',
] as const;
export type BloodGroup = (typeof BLOOD_GROUPS)[number];

export const MARITAL_STATUSES = [
  'SINGLE',
  'MARRIED',
  'DIVORCED',
  'WIDOWED',
  'SEPARATED',
  'UNKNOWN',
] as const;
export type MaritalStatus = (typeof MARITAL_STATUSES)[number];

export const PATIENT_STATUSES = ['ACTIVE', 'INACTIVE', 'DECEASED', 'ARCHIVED'] as const;
export type PatientStatus = (typeof PATIENT_STATUSES)[number];

export const ALLERGY_TYPES = ['DRUG', 'FOOD', 'ENVIRONMENTAL', 'OTHER'] as const;
export type AllergyType = (typeof ALLERGY_TYPES)[number];

export const ALLERGY_SEVERITIES = ['MILD', 'MODERATE', 'SEVERE', 'LIFE_THREATENING'] as const;
export type AllergySeverity = (typeof ALLERGY_SEVERITIES)[number];

export const ALLERGY_REACTIONS = [
  'ANAPHYLAXIS',
  'ANGIOEDEMA',
  'URTICARIA',
  'RASH',
  'ITCHING',
  'BRONCHOSPASM',
  'WHEEZING',
  'DYSPNEA',
  'NAUSEA',
  'VOMITING',
  'DIARRHEA',
  'HYPOTENSION',
  'SWELLING',
  'OTHER',
] as const;
export type AllergyReaction = (typeof ALLERGY_REACTIONS)[number];

export const ALLERGY_STATUSES = ['ACTIVE', 'INACTIVE', 'ENTERED_IN_ERROR'] as const;
export type AllergyStatus = (typeof ALLERGY_STATUSES)[number];

export const IMMUNIZATION_STATUSES = [
  'ADMINISTERED',
  'SCHEDULED',
  'REFUSED',
  'ENTERED_IN_ERROR',
] as const;
export type ImmunizationStatus = (typeof IMMUNIZATION_STATUSES)[number];

export const VACCINE_ROUTES = [
  'INTRAMUSCULAR',
  'SUBCUTANEOUS',
  'INTRADERMAL',
  'ORAL',
  'NASAL',
  'OTHER',
  'UNKNOWN',
] as const;
export type VaccineRoute = (typeof VACCINE_ROUTES)[number];

export const CLINICAL_SEVERITIES = ['MILD', 'MODERATE', 'SEVERE', 'CRITICAL', 'UNKNOWN'] as const;
export type ClinicalSeverity = (typeof CLINICAL_SEVERITIES)[number];

export const CLINICAL_CONDITION_STATUSES = ['ONGOING', 'CONTROLLED', 'RECOVERED'] as const;
export type ClinicalConditionStatus = (typeof CLINICAL_CONDITION_STATUSES)[number];

export const DISEASE_CATEGORIES = [
  'INFECTIOUS',
  'CARDIOVASCULAR',
  'RESPIRATORY',
  'ENDOCRINE',
  'GASTROINTESTINAL',
  'NEUROLOGICAL',
  'MUSCULOSKELETAL',
  'RENAL',
  'HEMATOLOGIC',
  'ONCOLOGIC',
  'PSYCHIATRIC',
  'DERMATOLOGIC',
  'OTHER',
] as const;
export type DiseaseCategory = (typeof DISEASE_CATEGORIES)[number];

export const PROCEDURE_CATEGORIES = [
  'GENERAL',
  'ORTHOPEDIC',
  'CARDIAC',
  'NEUROSURGERY',
  'OBSTETRIC',
  'GYNECOLOGIC',
  'UROLOGIC',
  'OPHTHALMIC',
  'ENT',
  'PLASTIC',
  'VASCULAR',
  'OTHER',
] as const;
export type ProcedureCategory = (typeof PROCEDURE_CATEGORIES)[number];

export const TIMELINE_EVENT_TYPES = [
  'REGISTRATION',
  'PAST_DISEASE',
  'SURGERY',
  'CHRONIC_CONDITION',
  'ALLERGY',
  'IMMUNIZATION',
  'VISIT',
  'PRESCRIPTION',
  'LAB_RESULT',
  'BILLING',
] as const;
export type TimelineEventType = (typeof TIMELINE_EVENT_TYPES)[number];

export const TIMELINE_SORT_DIRECTIONS = ['DESC', 'ASC'] as const;
export type TimelineSortDirection = (typeof TIMELINE_SORT_DIRECTIONS)[number];

export const TIMELINE_SEVERITY_HINTS = ['NONE', 'STANDARD', 'HIGH', 'CRITICAL'] as const;
export type TimelineSeverityHint = (typeof TIMELINE_SEVERITY_HINTS)[number];

export const BLOOD_GROUP_LABELS: Record<BloodGroup, string> = {
  A_POSITIVE: 'A+',
  A_NEGATIVE: 'A-',
  B_POSITIVE: 'B+',
  B_NEGATIVE: 'B-',
  AB_POSITIVE: 'AB+',
  AB_NEGATIVE: 'AB-',
  O_POSITIVE: 'O+',
  O_NEGATIVE: 'O-',
  UNKNOWN: 'Unknown',
};
