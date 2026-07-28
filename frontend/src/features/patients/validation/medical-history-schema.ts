import { z } from 'zod';

import {
  CLINICAL_CONDITION_STATUSES,
  CLINICAL_SEVERITIES,
  DISEASE_CATEGORIES,
  FAMILY_RELATIONS,
  PROCEDURE_CATEGORIES,
} from '@/features/patients/types/enums';

const optionalNotes = z.string().trim().max(1000);
const optionalCode = z.string().trim().max(64);

function refineRecoveryDate(
  values: { conditionStatus: string; recoveryDate: string },
  ctx: z.RefinementCtx,
) {
  if (values.conditionStatus === 'RECOVERED' && !values.recoveryDate) {
    ctx.addIssue({
      code: 'custom',
      path: ['recoveryDate'],
      message: 'Recovery date is required when status is Recovered',
    });
  }
}

export const pastDiseaseFormSchema = z
  .object({
    diseaseName: z.string().trim().min(1, 'Disease name is required').max(200),
    diseaseCategory: z.enum(DISEASE_CATEGORIES),
    diseaseCode: optionalCode,
    diagnosisDate: z.string().min(1, 'Date is required'),
    recoveryDate: z.string(),
    severity: z.enum(CLINICAL_SEVERITIES),
    conditionStatus: z.enum(CLINICAL_CONDITION_STATUSES),
    clinicalNotes: optionalNotes,
  })
  .superRefine(refineRecoveryDate);

export const surgeryFormSchema = z
  .object({
    procedureName: z.string().trim().min(1, 'Procedure name is required').max(200),
    procedureCategory: z.enum(PROCEDURE_CATEGORIES),
    procedureCode: optionalCode,
    performingFacility: z.string().trim().max(200),
    diagnosisDate: z.string().min(1, 'Date is required'),
    recoveryDate: z.string(),
    severity: z.enum(CLINICAL_SEVERITIES),
    conditionStatus: z.enum(CLINICAL_CONDITION_STATUSES),
    clinicalNotes: optionalNotes,
  })
  .superRefine(refineRecoveryDate);

export const chronicConditionFormSchema = z
  .object({
    conditionName: z.string().trim().min(1, 'Condition name is required').max(200),
    diseaseCategory: z.enum(DISEASE_CATEGORIES),
    conditionCode: optionalCode,
    diagnosisDate: z.string().min(1, 'Date is required'),
    recoveryDate: z.string(),
    severity: z.enum(CLINICAL_SEVERITIES),
    conditionStatus: z.enum(CLINICAL_CONDITION_STATUSES),
    clinicalNotes: optionalNotes,
  })
  .superRefine(refineRecoveryDate);

export const familyHistoryFormSchema = z
  .object({
    diseaseName: z.string().trim().min(1, 'Disease name is required').max(200),
    diseaseCategory: z.enum(DISEASE_CATEGORIES),
    diseaseCode: optionalCode,
    familyRelation: z.enum(FAMILY_RELATIONS),
    diagnosisDate: z.string().min(1, 'Date is required'),
    recoveryDate: z.string(),
    severity: z.enum(CLINICAL_SEVERITIES),
    conditionStatus: z.enum(CLINICAL_CONDITION_STATUSES),
    clinicalNotes: optionalNotes,
  })
  .superRefine(refineRecoveryDate);

export type PastDiseaseFormValues = z.infer<typeof pastDiseaseFormSchema>;
export type SurgeryFormValues = z.infer<typeof surgeryFormSchema>;
export type ChronicConditionFormValues = z.infer<typeof chronicConditionFormSchema>;
export type FamilyHistoryFormValues = z.infer<typeof familyHistoryFormSchema>;
