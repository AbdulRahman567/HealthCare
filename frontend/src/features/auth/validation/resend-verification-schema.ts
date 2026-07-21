import { z } from 'zod';

export const resendVerificationSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Email is required')
    .email('Enter a valid email address')
    .max(255, 'Email must not exceed 255 characters'),
});

export type ResendVerificationFormValues = z.infer<typeof resendVerificationSchema>;
