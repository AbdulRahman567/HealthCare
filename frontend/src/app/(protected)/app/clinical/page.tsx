import type { Metadata } from 'next';

import { ConsultationsListPage } from '@/features/clinical';

export const metadata: Metadata = {
  title: 'Clinical Consultations | Healthcare HMS',
  description: 'Clinical consultation list and encounter workspace',
};

export default function ClinicalConsultationsRoutePage() {
  return <ConsultationsListPage />;
}
