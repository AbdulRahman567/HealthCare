import type { Metadata } from 'next';

import { DashboardCards } from '@/features/navigation/components/dashboard-cards';
import { QuickActions } from '@/features/navigation/components/quick-actions';
import { WorkspaceHome } from '@/features/navigation/components/workspace-home';

export const metadata: Metadata = {
  title: 'Dashboard | Healthcare HMS',
  description: 'Permission-aware workspace dashboard',
};

export default function AppHomePage() {
  return (
    <div className="mx-auto max-w-6xl space-y-8">
      <div className="space-y-2">
        <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground text-sm text-pretty">
          Your menus and actions are limited to what you are authorized to access.
        </p>
      </div>
      <WorkspaceHome />
      <QuickActions />
      <DashboardCards />
    </div>
  );
}
