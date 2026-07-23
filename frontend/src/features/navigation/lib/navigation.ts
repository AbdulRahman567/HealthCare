import {
  NAV_SECTION_ORDER,
  WORKSPACE_NAVIGATION,
} from '@/features/navigation/config/nav-items';
import { filterByPermission } from '@/features/navigation/lib/filter-by-permission';
import type { BreadcrumbItem, WorkspaceNavItem } from '@/features/navigation/types';

export function filterNavigation(
  items: readonly WorkspaceNavItem[],
  permissions: readonly string[],
): WorkspaceNavItem[] {
  return filterByPermission(items, permissions);
}

export function filterSidebarNavigation(
  permissions: readonly string[],
): WorkspaceNavItem[] {
  return filterNavigation(WORKSPACE_NAVIGATION, permissions).filter(
    (item) => item.showInSidebar !== false,
  );
}

export function filterTopNavigation(permissions: readonly string[]): WorkspaceNavItem[] {
  return filterNavigation(WORKSPACE_NAVIGATION, permissions).filter(
    (item) => item.showInTopNav !== false,
  );
}

export function groupNavigationBySection(
  items: readonly WorkspaceNavItem[],
): Array<{ section: WorkspaceNavItem['section']; items: WorkspaceNavItem[] }> {
  return NAV_SECTION_ORDER.map((section) => ({
    section,
    items: items.filter((item) => item.section === section),
  })).filter((group) => group.items.length > 0);
}

export function isNavItemActive(pathname: string, href: string): boolean {
  if (href === '/app') {
    return pathname === '/app' || pathname === '/app/';
  }
  return pathname === href || pathname.startsWith(`${href}/`);
}

function titleCaseSegment(segment: string): string {
  return segment
    .split('-')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

/**
 * Builds breadcrumb trail from the navigation catalog for the current pathname.
 */
export function buildBreadcrumbs(pathname: string): BreadcrumbItem[] {
  const crumbs: BreadcrumbItem[] = [{ label: 'Dashboard', href: '/app' }];

  if (pathname === '/app' || pathname === '/app/') {
    return crumbs;
  }

  const match = WORKSPACE_NAVIGATION.find(
    (item) => item.href !== '/app' && isNavItemActive(pathname, item.href),
  );

  if (!match) {
    const last = pathname.split('/').filter(Boolean).pop() ?? 'Page';
    crumbs.push({ label: titleCaseSegment(last) });
    return crumbs;
  }

  const nested = pathname
    .slice(match.href.length)
    .split('/')
    .filter(Boolean)
    .map(titleCaseSegment);

  if (nested.length === 0) {
    crumbs.push({ label: match.label });
    return crumbs;
  }

  crumbs.push({ label: match.label, href: match.href });
  nested.forEach((label, index) => {
    const isLast = index === nested.length - 1;
    crumbs.push(isLast ? { label } : { label });
  });
  return crumbs;
}
