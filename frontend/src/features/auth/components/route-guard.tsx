'use client';

import { useRouter } from 'next/navigation';
import { useEffect, type ReactNode } from 'react';

import { useSession } from '@/providers/session-provider';

type RouteGuardProps = {
  children: ReactNode;
  /**
   * When set, user must hold at least one listed role.
   */
  roles?: string[];
  /**
   * When set, user must hold at least one listed permission.
   */
  permissions?: string[];
  /**
   * Redirect target when unauthenticated.
   */
  loginRedirect?: string;
  /**
   * Redirect target when authenticated but unauthorized.
   */
  forbiddenRedirect?: string;
};

export function RouteGuard({
  children,
  roles,
  permissions,
  loginRedirect = '/login',
  forbiddenRedirect = '/forbidden',
}: RouteGuardProps) {
  const router = useRouter();
  const { status, isAuthenticated, hasRole, hasPermission } = useSession();

  const roleAllowed = !roles || roles.length === 0 || hasRole(roles);
  const permissionAllowed = !permissions || permissions.length === 0 || hasPermission(permissions);
  const authorized = roleAllowed && permissionAllowed;

  useEffect(() => {
    if (status === 'loading') {
      return;
    }
    if (!isAuthenticated) {
      router.replace(loginRedirect);
      return;
    }
    if (!authorized) {
      router.replace(forbiddenRedirect);
    }
  }, [status, isAuthenticated, authorized, router, loginRedirect, forbiddenRedirect]);

  if (status === 'loading') {
    return (
      <div className="text-muted-foreground flex min-h-[40vh] items-center justify-center text-sm">
        Checking session…
      </div>
    );
  }

  if (!isAuthenticated || !authorized) {
    return (
      <div className="text-muted-foreground flex min-h-[40vh] items-center justify-center text-sm">
        Redirecting…
      </div>
    );
  }

  return <>{children}</>;
}
