import type { Metadata } from 'next';

import { RegisterPatientPage } from '@/features/patients';

export const metadata: Metadata = {
  title: 'Register Patient | Healthcare HMS',
  description: 'Register a new patient chart',
};

export default function RegisterPatientRoutePage() {
  return <RegisterPatientPage />;
}
