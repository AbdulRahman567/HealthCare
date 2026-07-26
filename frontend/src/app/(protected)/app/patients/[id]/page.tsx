import type { Metadata } from 'next';

import { PatientDetailPage } from '@/features/patients';

export const metadata: Metadata = {
  title: 'Patient Chart | Healthcare HMS',
  description: 'Patient clinical chart',
};

type PatientDetailRouteProps = {
  params: Promise<{ id: string }>;
};

export default async function PatientDetailRoutePage({ params }: PatientDetailRouteProps) {
  const { id } = await params;
  return <PatientDetailPage patientId={id} />;
}
