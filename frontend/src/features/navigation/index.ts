export { AppShell } from '@/features/navigation/components/app-shell';
export { AppSidebar } from '@/features/navigation/components/app-sidebar';
export { AppBreadcrumbs } from '@/features/navigation/components/app-breadcrumbs';
export { TopNavigation } from '@/features/navigation/components/top-navigation';
export { DashboardCards } from '@/features/navigation/components/dashboard-cards';
export { QuickActions } from '@/features/navigation/components/quick-actions';
export { DASHBOARD_CARDS } from '@/features/navigation/config/dashboard-cards';
export { WORKSPACE_NAVIGATION, NAV_SECTION_LABELS } from '@/features/navigation/config/nav-items';
export { QUICK_ACTIONS } from '@/features/navigation/config/quick-actions';
export { useBreadcrumbs } from '@/features/navigation/hooks/use-breadcrumbs';
export {
  useDashboardCards,
  useQuickActions,
  useWorkspaceNavigation,
} from '@/features/navigation/hooks/use-workspace-navigation';
export {
  buildBreadcrumbs,
  filterNavigation,
  filterSidebarNavigation,
  filterTopNavigation,
  isNavItemActive,
} from '@/features/navigation/lib/navigation';
export { filterByPermission } from '@/features/navigation/lib/filter-by-permission';
export type {
  BreadcrumbItem,
  DashboardCardItem,
  QuickActionItem,
  WorkspaceNavItem,
} from '@/features/navigation/types';
