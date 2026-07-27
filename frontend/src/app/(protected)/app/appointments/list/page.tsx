import type { Metadata } from 'next';

import { AppointmentsListPage } from '@/features/appointments';

export const metadata: Metadata = {
  title: 'Appointment list | Healthcare HMS',
  description: 'Search and filter appointments',
};

export default function AppointmentsListRoutePage() {
  return <AppointmentsListPage />;
}
