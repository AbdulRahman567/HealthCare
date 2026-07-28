import type { Metadata } from 'next';

import { AppointmentDetailPage } from '@/features/appointments';

export const metadata: Metadata = {
  title: 'Appointment | Healthcare HMS',
  description: 'Appointment details',
};

type AppointmentDetailRouteProps = {
  params: Promise<{ id: string }>;
};

export default async function AppointmentDetailRoutePage({ params }: AppointmentDetailRouteProps) {
  const { id } = await params;
  return <AppointmentDetailPage appointmentId={id} />;
}
