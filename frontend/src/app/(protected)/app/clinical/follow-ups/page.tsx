import type { Metadata } from 'next';

import { FollowUpsWorklistPage } from '@/features/clinical';

export const metadata: Metadata = {
  title: 'Follow-ups | Healthcare HMS',
  description: 'Clinical follow-up worklist and due visits',
};

export default function ClinicalFollowUpsRoutePage() {
  return <FollowUpsWorklistPage />;
}
