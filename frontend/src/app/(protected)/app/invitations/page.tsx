import type { Metadata } from 'next';

import { InvitationsPage } from '@/features/hospital-admin';

export const metadata: Metadata = {
  title: 'Invitations | Healthcare HMS',
  description: 'Manage hospital user invitations',
};

export default function InvitationsRoutePage() {
  return <InvitationsPage />;
}
