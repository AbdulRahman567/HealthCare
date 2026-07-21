import { z } from 'zod';

export const registerHospitalSchema = z.object({
  hospitalName: z
    .string()
    .trim()
    .min(2, 'Hospital name must be at least 2 characters')
    .max(200, 'Hospital name must not exceed 200 characters'),
  email: z
    .string()
    .trim()
    .min(1, 'Hospital email is required')
    .email('Enter a valid email address')
    .max(255, 'Email must not exceed 255 characters'),
  phone: z.string().trim().max(30, 'Phone must not exceed 30 characters').optional(),
  address: z.string().trim().max(500, 'Address must not exceed 500 characters').optional(),
  subscriptionPlan: z.enum(['BASIC', 'STANDARD', 'PREMIUM', 'ENTERPRISE']),
});

export type RegisterHospitalFormValues = z.infer<typeof registerHospitalSchema>;
