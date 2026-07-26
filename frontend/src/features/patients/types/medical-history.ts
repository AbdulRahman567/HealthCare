import type {
  ClinicalConditionStatus,
  ClinicalSeverity,
  DiseaseCategory,
  ProcedureCategory,
} from './enums';

export type ClinicalEntryBase = {
  id: string;
  patientId: string;
  medicalHistoryId: string;
  diagnosisDate: string;
  recoveryDate: string | null;
  severity: ClinicalSeverity;
  conditionStatus: ClinicalConditionStatus;
  clinicalNotes: string | null;
  recordedByUserId: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type PastDiseaseResponse = ClinicalEntryBase & {
  diseaseName: string;
  diseaseCategory: DiseaseCategory;
  diseaseCode: string | null;
};

export type SurgeryHistoryResponse = ClinicalEntryBase & {
  procedureName: string;
  procedureCategory: ProcedureCategory;
  procedureCode: string | null;
  performingFacility: string | null;
};

export type ChronicConditionResponse = ClinicalEntryBase & {
  conditionName: string;
  diseaseCategory: DiseaseCategory;
  conditionCode: string | null;
};

export type MedicalHistoryResponse = {
  id: string;
  patientId: string;
  lastReviewedAt: string | null;
  lastReviewedBy: string | null;
  pastDiseases: PastDiseaseResponse[];
  surgeries: SurgeryHistoryResponse[];
  chronicConditions: ChronicConditionResponse[];
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type UpsertPastDiseasePayload = {
  diseaseName: string;
  diseaseCategory: DiseaseCategory;
  diseaseCode?: string | null;
  diagnosisDate: string;
  recoveryDate?: string | null;
  severity: ClinicalSeverity;
  conditionStatus: ClinicalConditionStatus;
  clinicalNotes?: string | null;
};

export type UpsertSurgeryHistoryPayload = {
  procedureName: string;
  procedureCategory: ProcedureCategory;
  procedureCode?: string | null;
  performingFacility?: string | null;
  diagnosisDate: string;
  recoveryDate?: string | null;
  severity: ClinicalSeverity;
  conditionStatus: ClinicalConditionStatus;
  clinicalNotes?: string | null;
};

export type UpsertChronicConditionPayload = {
  conditionName: string;
  diseaseCategory: DiseaseCategory;
  conditionCode?: string | null;
  diagnosisDate: string;
  recoveryDate?: string | null;
  severity: ClinicalSeverity;
  conditionStatus: ClinicalConditionStatus;
  clinicalNotes?: string | null;
};
