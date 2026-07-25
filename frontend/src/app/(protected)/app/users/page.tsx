import type { Metadata } from 'next';

import { UsersPage } from '@/features/hospital-admin';

export const metadata: Metadata = {
  title: 'Users | Healthcare HMS',
  description: 'Manage hospital users and account status',
};

export default function UsersRoutePage() {
  return <UsersPage />;
}
