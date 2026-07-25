import { z } from 'zod';

import {
  EMPLOYMENT_STATUSES,
  EMPLOYMENT_TYPES,
  STAFF_SHIFTS,
  STAFF_TYPES,
} from '@/features/hospital-admin/types/enums';

const codePattern = /^[A-Za-z0-9][A-Za-z0-9_-]*$/;
const uuidSchema = z.string().uuid('Select a valid user');

const employmentFields = {
  userId: uuidSchema,
  departmentId: uuidSchema,
  employeeCode: z
    .string()
    .trim()
    .min(2, 'Employee code must be at least 2 characters')
    .max(50)
    .regex(codePattern, 'Use letters, numbers, underscore, or hyphen'),
  jobTitle: z.string().trim().max(150).optional().or(z.literal('')),
  employmentStatus: z.enum(EMPLOYMENT_STATUSES),
  employmentType: z.enum(EMPLOYMENT_TYPES),
  hiredAt: z.string().optional().or(z.literal('')),
  terminatedAt: z.string().optional().or(z.literal('')),
};

export const staffFormSchema = z
  .object({
    staffType: z.enum(STAFF_TYPES),
    ...employmentFields,
    specialization: z.string().trim().max(150).optional().or(z.literal('')),
    licenseNumber: z.string().trim().max(100).optional().or(z.literal('')),
    qualification: z.string().trim().max(255).optional().or(z.literal('')),
    experienceYears: z.string().optional().or(z.literal('')),
    consultationFee: z.string().optional().or(z.literal('')),
    shift: z.enum(STAFF_SHIFTS).optional(),
    deskLocation: z.string().trim().max(150).optional().or(z.literal('')),
    languages: z.string().trim().max(255).optional().or(z.literal('')),
    specialtyArea: z.string().trim().max(150).optional().or(z.literal('')),
    certification: z.string().trim().max(255).optional().or(z.literal('')),
    pharmacyLocation: z.string().trim().max(150).optional().or(z.literal('')),
  })
  .superRefine((values, ctx) => {
    if (values.staffType === 'DOCTOR') {
      if (!values.specialization?.trim()) {
        ctx.addIssue({
          code: 'custom',
          path: ['specialization'],
          message: 'Specialization is required',
        });
      }
      if (!values.licenseNumber?.trim()) {
        ctx.addIssue({
          code: 'custom',
          path: ['licenseNumber'],
          message: 'License number is required',
        });
      }
    }
    if (values.staffType === 'NURSE' && !values.shift) {
      ctx.addIssue({ code: 'custom', path: ['shift'], message: 'Shift is required' });
    }
    if (values.staffType === 'PHARMACIST' && !values.licenseNumber?.trim()) {
      ctx.addIssue({
        code: 'custom',
        path: ['licenseNumber'],
        message: 'License number is required',
      });
    }
  });

export type StaffFormValues = z.infer<typeof staffFormSchema>;
