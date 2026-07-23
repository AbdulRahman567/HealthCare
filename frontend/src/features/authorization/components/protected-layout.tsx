'use client';

import type { ReactNode } from 'react';

import { RouteProtection } from '@/features/authorization/components/route-protection';
import type { AccessRequirement } from '@/features/authorization/types';
import { AppShell } from '@/features/navigation/components/app-shell';

type ProtectedLayoutProps = {
  children: ReactNode;
  /**
   * Extra access requirement beyond the route catalog (optional).
   */
  requirement?: AccessRequirement;
  roles?: string[];
  permissions?: string[];
  /**
   * When false, renders children without the app chrome (sidebar / top nav).
   */
  withShell?: boolean;
};

/**
 * Protected layout: route protection + optional permission-aware app shell.
 */
export function ProtectedLayout({
  children,
  requirement,
  roles,
  permissions,
  withShell = true,
}: ProtectedLayoutProps) {
  const content = withShell ? <AppShell>{children}</AppShell> : children;

  return (
    <RouteProtection requirement={requirement} roles={roles} permissions={permissions}>
      {content}
    </RouteProtection>
  );
}
