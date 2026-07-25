import type { Metadata } from 'next';

import { DepartmentsPage } from '@/features/hospital-admin';

export const metadata: Metadata = {
  title: 'Departments | Healthcare HMS',
  description: 'Manage hospital departments',
};

export default function DepartmentsRoutePage() {
  return <DepartmentsPage />;
}
