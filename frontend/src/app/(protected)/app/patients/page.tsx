import type { Metadata } from 'next';

import { PatientsListPage } from '@/features/patients';

export const metadata: Metadata = {
  title: 'Patients | Healthcare HMS',
  description: 'Search and manage patient records',
};

export default function PatientsRoutePage() {
  return <PatientsListPage />;
}
