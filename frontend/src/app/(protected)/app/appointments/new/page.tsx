import type { Metadata } from 'next';

import { BookAppointmentPage } from '@/features/appointments';

export const metadata: Metadata = {
  title: 'Book appointment | Healthcare HMS',
  description: 'Schedule a patient appointment',
};

export default function BookAppointmentRoutePage() {
  return <BookAppointmentPage />;
}
