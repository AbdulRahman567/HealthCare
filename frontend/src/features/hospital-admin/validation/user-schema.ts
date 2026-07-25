import { z } from 'zod';

export const adminUpdateUserSchema = z.object({
  firstName: z.string().trim().min(1, 'First name is required').max(100),
  lastName: z.string().trim().min(1, 'Last name is required').max(100),
  phone: z.string().trim().max(30).optional().or(z.literal('')),
});

export type AdminUpdateUserFormValues = z.infer<typeof adminUpdateUserSchema>;
