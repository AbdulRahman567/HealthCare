import { z } from 'zod';

import { INVITABLE_ROLE_TYPES } from '@/features/hospital-admin/types/enums';

export const createInvitationSchema = z.object({
  email: z.string().trim().min(1, 'Email is required').email('Enter a valid email').max(255),
  firstName: z.string().trim().max(100).optional().or(z.literal('')),
  lastName: z.string().trim().max(100).optional().or(z.literal('')),
  roleType: z.enum(INVITABLE_ROLE_TYPES),
  message: z.string().trim().max(500).optional().or(z.literal('')),
});

export type CreateInvitationFormValues = z.infer<typeof createInvitationSchema>;
