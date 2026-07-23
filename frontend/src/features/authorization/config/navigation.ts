/**
 * @deprecated Import from `@/features/navigation` instead.
 * Kept for Phase 3.6 compatibility — navigation is now permission-driven (Phase 3.7).
 */
import { Permissions } from '@/features/authorization/constants/permissions';
import { WORKSPACE_NAVIGATION } from '@/features/navigation/config/nav-items';
import {
  filterNavigation as filterWorkspaceNavigation,
} from '@/features/navigation/lib/navigation';
import type { WorkspaceNavItem } from '@/features/navigation/types';

export type AppRole = string;
export type AppPermission = string;

export type NavItem = {
  id: string;
  label: string;
  href: string;
  description?: string;
  roles?: AppRole[];
  permissions?: AppPermission[];
  mode?: 'any' | 'all';
  comingSoon?: boolean;
};

export const APP_NAVIGATION: NavItem[] = WORKSPACE_NAVIGATION.map((item) => ({
  id: item.id,
  label: item.label,
  href: item.href,
  description: item.description,
  permissions: item.permissions,
  mode: item.mode,
  comingSoon: item.comingSoon,
}));

export function canAccessNavItem(
  item: NavItem | WorkspaceNavItem,
  _roles: string[],
  permissions: string[],
): boolean {
  return filterWorkspaceNavigation(
    [
      {
        id: item.id,
        label: item.label,
        href: item.href,
        description: item.description,
        permissions: item.permissions,
        mode: item.mode,
        comingSoon: item.comingSoon,
        icon: 'layout-dashboard',
        section: 'main',
      },
    ],
    permissions,
  ).length > 0;
}

export function filterNavigation(
  items: NavItem[],
  _roles: string[],
  permissions: string[],
): NavItem[] {
  const allowed = new Set(
    filterWorkspaceNavigation(
      items.map((item) => ({
        id: item.id,
        label: item.label,
        href: item.href,
        description: item.description,
        permissions: item.permissions,
        mode: item.mode,
        comingSoon: item.comingSoon,
        icon: 'layout-dashboard' as const,
        section: 'main' as const,
      })),
      permissions,
    ).map((item) => item.id),
  );
  return items.filter((item) => allowed.has(item.id));
}

/** @deprecated Prefer Permissions constants directly. */
export const HospitalReadPermission = Permissions.HOSPITAL_READ;
