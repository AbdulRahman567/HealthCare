import type { Metadata } from 'next';

import { CalendarViewPage } from '@/features/appointments';

export const metadata: Metadata = {
  title: 'Appointment calendar | Healthcare HMS',
  description: 'Daily, weekly, and monthly appointment calendars',
};

export default function AppointmentCalendarRoutePage() {
  return <CalendarViewPage />;
}
