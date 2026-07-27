import type { Metadata } from 'next';

import { EditAppointmentPage } from '@/features/appointments';

export const metadata: Metadata = {
  title: 'Edit appointment | Healthcare HMS',
  description: 'Update appointment details',
};

type EditAppointmentRouteProps = {
  params: Promise<{ id: string }>;
};

export default async function EditAppointmentRoutePage({ params }: EditAppointmentRouteProps) {
  const { id } = await params;
  return <EditAppointmentPage appointmentId={id} />;
}
