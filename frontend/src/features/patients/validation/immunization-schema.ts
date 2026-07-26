import { z } from 'zod';

import { IMMUNIZATION_STATUSES, VACCINE_ROUTES } from '@/features/patients/types/enums';

export const immunizationFormSchema = z
  .object({
    vaccineName: z.string().trim().min(1, 'Vaccine name is required').max(200),
    vaccineCode: z.string().trim().max(64),
    doseNumber: z
      .string()
      .trim()
      .min(1, 'Dose number is required')
      .refine((value) => {
        const n = Number(value);
        return Number.isInteger(n) && n >= 1 && n <= 50;
      }, 'Dose must be an integer between 1 and 50'),
    manufacturer: z.string().trim().max(200),
    batchNumber: z.string().trim().max(100),
    administrationDate: z.string().min(1, 'Administration date is required'),
    nextDueDate: z.string(),
    healthcareProvider: z.string().trim().min(1, 'Provider is required').max(200),
    route: z.union([z.enum(VACCINE_ROUTES), z.literal('')]),
    status: z.enum(IMMUNIZATION_STATUSES),
    clinicalNotes: z.string().trim().max(1000),
  })
  .superRefine((values, ctx) => {
    if (
      values.nextDueDate &&
      values.administrationDate &&
      values.nextDueDate < values.administrationDate
    ) {
      ctx.addIssue({
        code: 'custom',
        path: ['nextDueDate'],
        message: 'Next due date must be on or after administration date',
      });
    }
  });

export type ImmunizationFormValues = z.infer<typeof immunizationFormSchema>;

export const emptyImmunizationForm = (): ImmunizationFormValues => ({
  vaccineName: '',
  vaccineCode: '',
  doseNumber: '1',
  manufacturer: '',
  batchNumber: '',
  administrationDate: '',
  nextDueDate: '',
  healthcareProvider: '',
  route: 'INTRAMUSCULAR',
  status: 'ADMINISTERED',
  clinicalNotes: '',
});
