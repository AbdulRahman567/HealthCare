import { z } from 'zod';

import {
  BLOOD_GROUPS,
  GENDERS,
  MARITAL_STATUSES,
} from '@/features/patients/types/enums';

const mrnPattern = /^[A-Za-z0-9][A-Za-z0-9_-]*$/;
const phonePattern = /^\+?[0-9][0-9\s().-]{6,28}$/;

const optionalPhone = z
  .string()
  .trim()
  .max(30)
  .refine((value) => value === '' || phonePattern.test(value), 'Enter a valid phone');

const optionalEmail = z
  .string()
  .trim()
  .max(255)
  .refine((value) => value === '' || z.email().safeParse(value).success, 'Enter a valid email');

export const patientFormSchema = z
  .object({
    mrn: z
      .string()
      .trim()
      .min(2, 'MRN must be at least 2 characters')
      .max(50, 'MRN must be at most 50 characters')
      .regex(mrnPattern, 'Use letters, numbers, underscore, or hyphen'),
    firstName: z.string().trim().min(1, 'First name is required').max(100),
    lastName: z.string().trim().min(1, 'Last name is required').max(100),
    dateOfBirth: z
      .string()
      .min(1, 'Date of birth is required')
      .refine((value) => !Number.isNaN(Date.parse(value)), 'Enter a valid date')
      .refine((value) => new Date(value) <= new Date(), 'Date of birth cannot be in the future'),
    gender: z.enum(GENDERS),
    bloodGroup: z.union([z.enum(BLOOD_GROUPS), z.literal('')]),
    nationalId: z.string().trim().max(50),
    phone: optionalPhone,
    email: optionalEmail,
    address: z.string().trim().max(500),
    maritalStatus: z.union([z.enum(MARITAL_STATUSES), z.literal('')]),
    emergencyContactName: z.string().trim().max(150),
    emergencyContactPhone: optionalPhone,
    emergencyContactRelation: z.string().trim().max(50),
  })
  .superRefine((values, ctx) => {
    const hasAnyEmergency =
      Boolean(values.emergencyContactName) ||
      Boolean(values.emergencyContactPhone) ||
      Boolean(values.emergencyContactRelation);

    if (!hasAnyEmergency) {
      return;
    }

    if (!values.emergencyContactName) {
      ctx.addIssue({
        code: 'custom',
        path: ['emergencyContactName'],
        message: 'Emergency contact name is required when providing contact details',
      });
    }
    if (!values.emergencyContactPhone) {
      ctx.addIssue({
        code: 'custom',
        path: ['emergencyContactPhone'],
        message: 'Emergency contact phone is required when providing contact details',
      });
    }
  });

export type PatientFormValues = z.infer<typeof patientFormSchema>;

export const emptyPatientForm = (): PatientFormValues => ({
  mrn: '',
  firstName: '',
  lastName: '',
  dateOfBirth: '',
  gender: 'UNKNOWN',
  bloodGroup: '',
  nationalId: '',
  phone: '',
  email: '',
  address: '',
  maritalStatus: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  emergencyContactRelation: '',
});
