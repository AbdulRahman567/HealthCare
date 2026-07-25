import type { Metadata } from 'next';

import { HospitalAdminDashboard } from '@/features/hospital-admin';
import { DashboardCards } from '@/features/navigation/components/dashboard-cards';
import { QuickActions } from '@/features/navigation/components/quick-actions';
import { WorkspaceHome } from '@/features/navigation/components/workspace-home';

export const metadata: Metadata = {
  title: 'Dashboard | Healthcare HMS',
  description: 'Hospital administration workspace dashboard',
};

export default function AppHomePage() {
  return (
    <div className="mx-auto max-w-6xl space-y-8">
      <div className="space-y-2">
        <h1 className="text-2xl font-semibold tracking-tight">Hospital dashboard</h1>
        <p className="text-muted-foreground text-sm text-pretty">
          Administration overview for departments, staff, users, and invitations. Menus and actions
          respect your permissions.
        </p>
      </div>
      <WorkspaceHome />
      <HospitalAdminDashboard />
      <QuickActions />
      <DashboardCards />
    </div>
  );
}
