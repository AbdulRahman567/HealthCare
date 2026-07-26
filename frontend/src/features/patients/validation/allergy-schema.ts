import { z } from 'zod';

import {
  ALLERGY_REACTIONS,
  ALLERGY_SEVERITIES,
  ALLERGY_STATUSES,
  ALLERGY_TYPES,
} from '@/features/patients/types/enums';

export const allergyFormSchema = z.object({
  allergenName: z.string().trim().min(1, 'Allergen name is required').max(200),
  allergenCode: z.string().trim().max(64),
  allergyType: z.enum(ALLERGY_TYPES),
  severity: z.enum(ALLERGY_SEVERITIES),
  reaction: z.enum(ALLERGY_REACTIONS),
  status: z.enum(ALLERGY_STATUSES),
  onsetDate: z.string(),
  clinicalNotes: z.string().trim().max(1000),
  verified: z.boolean(),
  patientReported: z.boolean(),
  criticalAlert: z.boolean(),
  showOnBanner: z.boolean(),
});

export type AllergyFormValues = z.infer<typeof allergyFormSchema>;

export const emptyAllergyForm = (): AllergyFormValues => ({
  allergenName: '',
  allergenCode: '',
  allergyType: 'DRUG',
  severity: 'MODERATE',
  reaction: 'OTHER',
  status: 'ACTIVE',
  onsetDate: '',
  clinicalNotes: '',
  verified: false,
  patientReported: true,
  criticalAlert: false,
  showOnBanner: false,
});
