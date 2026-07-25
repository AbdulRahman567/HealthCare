import type { Metadata } from 'next';

import { StaffDirectoryPage } from '@/features/hospital-admin';

export const metadata: Metadata = {
  title: 'Staff directory | Healthcare HMS',
  description: 'Manage hospital staff employment profiles',
};

export default function StaffDirectoryRoutePage() {
  return <StaffDirectoryPage />;
}
