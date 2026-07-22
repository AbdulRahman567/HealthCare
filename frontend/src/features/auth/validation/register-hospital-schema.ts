import { z } from 'zod';

const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{12,}$/;

export const registerHospitalSchema = z.object({
  hospitalName: z
    .string()
    .trim()
    .min(2, 'Hospital name must be at least 2 characters')
    .max(200, 'Hospital name must not exceed 200 characters'),
  hospitalEmail: z
    .string()
    .trim()
    .min(1, 'Hospital email is required')
    .email('Enter a valid email address')
    .max(255, 'Email must not exceed 255 characters'),
  hospitalPhone: z.string().trim().max(30, 'Phone must not exceed 30 characters').optional(),
  hospitalAddress: z.string().trim().max(500, 'Address must not exceed 500 characters').optional(),
  subscriptionPlan: z.enum(['BASIC', 'STANDARD', 'PREMIUM', 'ENTERPRISE']),
  adminFirstName: z
    .string()
    .trim()
    .min(1, 'Administrator first name is required')
    .max(100, 'Administrator first name must not exceed 100 characters'),
  adminLastName: z
    .string()
    .trim()
    .min(1, 'Administrator last name is required')
    .max(100, 'Administrator last name must not exceed 100 characters'),
  adminEmail: z
    .string()
    .trim()
    .min(1, 'Administrator email is required')
    .email('Enter a valid email address')
    .max(255, 'Email must not exceed 255 characters'),
  adminPassword: z
    .string()
    .min(12, 'Password must be at least 12 characters')
    .max(128, 'Password must not exceed 128 characters')
    .regex(
      strongPasswordRegex,
      'Password must include uppercase, lowercase, a number, and a special character',
    ),
  adminPhone: z.string().trim().max(30, 'Phone must not exceed 30 characters').optional(),
});

export type RegisterHospitalFormValues = z.infer<typeof registerHospitalSchema>;
