import type { Metadata } from 'next';

import { EditPatientPage } from '@/features/patients';

export const metadata: Metadata = {
  title: 'Edit Patient | Healthcare HMS',
  description: 'Update patient demographics',
};

type EditPatientRouteProps = {
  params: Promise<{ id: string }>;
};

export default async function EditPatientRoutePage({ params }: EditPatientRouteProps) {
  const { id } = await params;
  return <EditPatientPage patientId={id} />;
}
