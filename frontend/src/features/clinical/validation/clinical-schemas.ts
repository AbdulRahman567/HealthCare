import { z } from 'zod';

import {
  CLINICAL_NOTE_TYPES,
  DIAGNOSIS_SEVERITIES,
  DIAGNOSIS_STATUSES,
  DIAGNOSIS_TYPES,
  FOLLOW_UP_PRIORITIES,
  FOLLOW_UP_STATUSES,
  MEDICATION_ROUTES,
} from '@/features/clinical/types/enums';

const optionalUuid = z.string().uuid().optional().or(z.literal(''));
const optionalText = (max: number) => z.string().trim().max(max).optional().or(z.literal(''));
const optionalNumericString = z.string().optional().or(z.literal(''));

function parseOptionalNumber(raw: string | undefined): number | undefined {
  const trimmed = raw?.trim();
  if (!trimmed) {
    return undefined;
  }
  const parsed = Number(trimmed);
  return Number.isFinite(parsed) ? parsed : Number.NaN;
}

export const startConsultationSchema = z.object({
  patientId: z.string().uuid('Select a patient'),
  doctorId: z.string().uuid('Select a doctor'),
  departmentId: z.string().uuid('Select a department'),
  appointmentId: optionalUuid,
  chiefComplaint: optionalText(2000),
  startImmediately: z.boolean(),
});

export type StartConsultationFormValues = z.infer<typeof startConsultationSchema>;

export function emptyStartConsultationForm(): StartConsultationFormValues {
  return {
    patientId: '',
    doctorId: '',
    departmentId: '',
    appointmentId: '',
    chiefComplaint: '',
    startImmediately: true,
  };
}

export const documentationSchema = z.object({
  chiefComplaint: optionalText(2000),
  historyOfPresentIllness: optionalText(4000),
  physicalExamination: optionalText(4000),
  doctorNotes: optionalText(4000),
  summary: optionalText(2000),
  advice: optionalText(2000),
});

export type DocumentationFormValues = z.infer<typeof documentationSchema>;

export const vitalsFormSchema = z
  .object({
    temperatureCelsius: optionalNumericString,
    heartRateBpm: optionalNumericString,
    systolicBp: optionalNumericString,
    diastolicBp: optionalNumericString,
    respiratoryRate: optionalNumericString,
    oxygenSaturationPercent: optionalNumericString,
    heightCm: optionalNumericString,
    weightKg: optionalNumericString,
    painScale: optionalNumericString,
    notes: optionalText(500),
  })
  .superRefine((values, ctx) => {
    const numericKeys = [
      'temperatureCelsius',
      'heartRateBpm',
      'systolicBp',
      'diastolicBp',
      'respiratoryRate',
      'oxygenSaturationPercent',
      'heightCm',
      'weightKg',
      'painScale',
    ] as const;

    const parsed: Partial<Record<(typeof numericKeys)[number], number>> = {};
    for (const key of numericKeys) {
      const numberValue = parseOptionalNumber(values[key]);
      if (numberValue === undefined) {
        continue;
      }
      if (Number.isNaN(numberValue)) {
        ctx.addIssue({
          code: 'custom',
          path: [key],
          message: 'Must be a number',
        });
        continue;
      }
      parsed[key] = numberValue;
    }

    if (Object.keys(parsed).length === 0) {
      ctx.addIssue({
        code: 'custom',
        message: 'Enter at least one vital sign measurement',
        path: ['temperatureCelsius'],
      });
    }

    const hasSys = parsed.systolicBp !== undefined;
    const hasDia = parsed.diastolicBp !== undefined;
    if (hasSys !== hasDia) {
      ctx.addIssue({
        code: 'custom',
        message: 'Blood pressure requires both systolic and diastolic',
        path: ['systolicBp'],
      });
    }
    if (
      hasSys &&
      hasDia &&
      parsed.systolicBp !== undefined &&
      parsed.diastolicBp !== undefined &&
      parsed.systolicBp <= parsed.diastolicBp
    ) {
      ctx.addIssue({
        code: 'custom',
        message: 'Systolic must be greater than diastolic',
        path: ['systolicBp'],
      });
    }
  });

export type VitalsFormValues = z.infer<typeof vitalsFormSchema>;

export function emptyVitalsForm(): VitalsFormValues {
  return {
    temperatureCelsius: '',
    heartRateBpm: '',
    systolicBp: '',
    diastolicBp: '',
    respiratoryRate: '',
    oxygenSaturationPercent: '',
    heightCm: '',
    weightKg: '',
    painScale: '',
    notes: '',
  };
}

const icd10Pattern = /^[A-TV-Z][0-9]{2}(\.[0-9A-TV-Z]{1,4})?$/;

export const diagnosisFormSchema = z.object({
  diagnosisName: z.string().trim().min(1, 'Diagnosis name is required').max(200),
  icdCode: z
    .string()
    .trim()
    .max(32)
    .optional()
    .or(z.literal(''))
    .refine((value) => !value || icd10Pattern.test(value), 'Invalid ICD-10 code'),
  diagnosisType: z.enum(DIAGNOSIS_TYPES),
  diagnosisStatus: z.enum(DIAGNOSIS_STATUSES),
  severity: z.enum(DIAGNOSIS_SEVERITIES),
  clinicalNotes: optionalText(1000),
});

export type DiagnosisFormValues = z.infer<typeof diagnosisFormSchema>;

export function emptyDiagnosisForm(): DiagnosisFormValues {
  return {
    diagnosisName: '',
    icdCode: '',
    diagnosisType: 'PRIMARY',
    diagnosisStatus: 'PROVISIONAL',
    severity: 'UNKNOWN',
    clinicalNotes: '',
  };
}

export const clinicalNoteFormSchema = z.object({
  noteType: z.enum(CLINICAL_NOTE_TYPES),
  title: optionalText(200),
  content: z.string().trim().min(1, 'Note content is required').max(4000),
});

export type ClinicalNoteFormValues = z.infer<typeof clinicalNoteFormSchema>;

export function emptyClinicalNoteForm(): ClinicalNoteFormValues {
  return {
    noteType: 'GENERAL',
    title: '',
    content: '',
  };
}

export const followUpFormSchema = z.object({
  scheduledDate: z.string().min(1, 'Scheduled date is required'),
  scheduledTime: z
    .string()
    .optional()
    .or(z.literal(''))
    .refine((value) => !value || /^([01]\d|2[0-3]):[0-5]\d$/.test(value), 'Use HH:mm'),
  status: z.enum(FOLLOW_UP_STATUSES),
  priority: z.enum(FOLLOW_UP_PRIORITIES),
  reason: optionalText(500),
  instructions: optionalText(1000),
  clinicalRecommendations: optionalText(2000),
  reminderEnabled: z.boolean(),
  reminderLeadDays: z
    .string()
    .optional()
    .or(z.literal(''))
    .refine((value) => {
      if (!value?.trim()) {
        return true;
      }
      const parsed = Number(value);
      return Number.isInteger(parsed) && parsed >= 0 && parsed <= 90;
    }, 'Lead days must be an integer from 0 to 90'),
});

export type FollowUpFormValues = z.infer<typeof followUpFormSchema>;

export function emptyFollowUpForm(): FollowUpFormValues {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 7);
  return {
    scheduledDate: tomorrow.toISOString().slice(0, 10),
    scheduledTime: '',
    status: 'PENDING',
    priority: 'ROUTINE',
    reason: '',
    instructions: '',
    clinicalRecommendations: '',
    reminderEnabled: true,
    reminderLeadDays: '1',
  };
}

export const prescriptionItemSchema = z
  .object({
    medicineName: z.string().trim().min(1, 'Medicine name is required').max(200),
    dosage: z.string().trim().min(1, 'Dosage is required').max(100),
    frequency: z.string().trim().min(1, 'Frequency is required').max(100),
    route: z.enum(MEDICATION_ROUTES),
    duration: z.string().trim().min(1, 'Duration is required').max(100),
    instructions: optionalText(1000),
    quantity: z
      .string()
      .trim()
      .min(1, 'Quantity is required')
      .refine((value) => {
        const parsed = Number(value);
        return Number.isInteger(parsed) && parsed >= 1;
      }, 'Quantity must be at least 1'),
    refills: z
      .string()
      .optional()
      .or(z.literal(''))
      .refine((value) => {
        if (!value?.trim()) {
          return true;
        }
        const parsed = Number(value);
        return Number.isInteger(parsed) && parsed >= 0;
      }, 'Refills must be 0 or greater'),
    beforeFood: z.boolean(),
    afterFood: z.boolean(),
  })
  .superRefine((values, ctx) => {
    if (values.beforeFood && values.afterFood) {
      ctx.addIssue({
        code: 'custom',
        path: ['beforeFood'],
        message: 'Cannot be both before and after food',
      });
    }
  });

export const prescriptionFormSchema = z.object({
  notes: optionalText(2000),
  issueImmediately: z.boolean(),
  items: z.array(prescriptionItemSchema).min(1, 'Add at least one medicine'),
});

export type PrescriptionFormValues = z.infer<typeof prescriptionFormSchema>;
export type PrescriptionItemFormValues = z.infer<typeof prescriptionItemSchema>;

export function emptyPrescriptionItem(): PrescriptionItemFormValues {
  return {
    medicineName: '',
    dosage: '',
    frequency: '',
    route: 'ORAL',
    duration: '',
    instructions: '',
    quantity: '1',
    refills: '0',
    beforeFood: false,
    afterFood: false,
  };
}

export function emptyPrescriptionForm(): PrescriptionFormValues {
  return {
    notes: '',
    issueImmediately: false,
    items: [emptyPrescriptionItem()],
  };
}

export const completeConsultationSchema = z.object({
  summary: optionalText(2000),
  advice: optionalText(2000),
});
