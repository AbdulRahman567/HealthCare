import type { AccessMode } from '@/features/authorization/types';

/**
 * Permission-driven workspace navigation types.
 * Visibility is always evaluated from backend-issued permission codes — never role names.
 */

export type NavIconName =
  | 'layout-dashboard'
  | 'user-round'
  | 'building-2'
  | 'users'
  | 'heart-pulse'
  | 'calendar-days'
  | 'receipt'
  | 'user-plus'
  | 'calendar-plus'
  | 'settings-2'
  | 'file-plus';

export type NavSection = 'main' | 'clinical' | 'admin' | 'account';

export type WorkspaceNavItem = {
  id: string;
  label: string;
  href: string;
  description?: string;
  /** Required permissions. Empty / omitted = any authenticated user. */
  permissions?: string[];
  mode?: AccessMode;
  comingSoon?: boolean;
  icon: NavIconName;
  section: NavSection;
  /** Include in the primary sidebar. Defaults to true. */
  showInSidebar?: boolean;
  /** Include in compact top / mobile nav strip. Defaults to true. */
  showInTopNav?: boolean;
};

export type DashboardCardItem = {
  id: string;
  title: string;
  description: string;
  href: string;
  permissions?: string[];
  mode?: AccessMode;
  comingSoon?: boolean;
  icon: NavIconName;
};

export type QuickActionItem = {
  id: string;
  label: string;
  description?: string;
  href: string;
  permissions?: string[];
  mode?: AccessMode;
  comingSoon?: boolean;
  icon: NavIconName;
};

export type BreadcrumbItem = {
  label: string;
  href?: string;
};
