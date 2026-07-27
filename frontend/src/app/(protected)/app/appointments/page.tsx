import type { Metadata } from 'next';

import { AppointmentDashboardPage } from '@/features/appointments';

export const metadata: Metadata = {
  title: 'Appointments | Healthcare HMS',
  description: 'Appointment scheduling dashboard',
};

export default function AppointmentsRoutePage() {
  return <AppointmentDashboardPage />;
}
