import { z } from 'zod';

import { DEPARTMENT_STATUSES, DEPARTMENT_TYPES } from '@/features/hospital-admin/types/enums';

const codePattern = /^[A-Za-z0-9][A-Za-z0-9_-]*$/;

export const departmentFormSchema = z.object({
  name: z.string().trim().min(2, 'Name must be at least 2 characters').max(200),
  code: z
    .string()
    .trim()
    .min(2, 'Code must be at least 2 characters')
    .max(50)
    .regex(codePattern, 'Use letters, numbers, underscore, or hyphen'),
  description: z.string().trim().max(1000).optional().or(z.literal('')),
  departmentType: z.enum(DEPARTMENT_TYPES),
  status: z.enum(DEPARTMENT_STATUSES),
  location: z.string().trim().max(255).optional().or(z.literal('')),
});

export type DepartmentFormValues = z.infer<typeof departmentFormSchema>;
