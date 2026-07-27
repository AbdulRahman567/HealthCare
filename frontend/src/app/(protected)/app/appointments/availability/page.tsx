import type { Metadata } from 'next';

import { AvailabilityPage } from '@/features/appointments';

export const metadata: Metadata = {
  title: 'Doctor availability | Healthcare HMS',
  description: 'Configure recurring doctor working hours for appointment booking',
};

export default function AppointmentAvailabilityRoutePage() {
  return <AvailabilityPage />;
}
