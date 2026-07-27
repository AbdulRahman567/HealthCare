import type { Metadata } from 'next';

import { QueueViewPage } from '@/features/appointments';

export const metadata: Metadata = {
  title: 'Doctor queue | Healthcare HMS',
  description: 'Daily doctor OPD queue board',
};

export default function AppointmentQueueRoutePage() {
  return <QueueViewPage />;
}
