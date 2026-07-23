'use client';

import { useMemo } from 'react';

import { usePermission } from '@/features/authorization/hooks/use-permission';
import { DASHBOARD_CARDS } from '@/features/navigation/config/dashboard-cards';
import { QUICK_ACTIONS } from '@/features/navigation/config/quick-actions';
import { filterByPermission } from '@/features/navigation/lib/filter-by-permission';
import {
  filterSidebarNavigation,
  filterTopNavigation,
  groupNavigationBySection,
} from '@/features/navigation/lib/navigation';

export function useWorkspaceNavigation() {
  const { permissions } = usePermission();

  return useMemo(() => {
    const sidebar = filterSidebarNavigation(permissions);
    const top = filterTopNavigation(permissions);
    return {
      permissions,
      sidebarItems: sidebar,
      topItems: top,
      sidebarGroups: groupNavigationBySection(sidebar),
    };
  }, [permissions]);
}

export function useDashboardCards() {
  const { permissions } = usePermission();
  return useMemo(() => filterByPermission(DASHBOARD_CARDS, permissions), [permissions]);
}

export function useQuickActions() {
  const { permissions } = usePermission();
  return useMemo(() => filterByPermission(QUICK_ACTIONS, permissions), [permissions]);
}
