'use client';

import { usePathname, useRouter } from 'next/navigation';
import { useEffect, type ReactNode } from 'react';

import { resolveRouteAccess } from '@/features/authorization/config/routes';
import { useAuthorization } from '@/features/authorization/hooks/use-authorization';
import type { AccessRequirement } from '@/features/authorization/types';
import { useSession } from '@/providers/session-provider';

type RouteProtectionProps = {
  children: ReactNode;
  /**
   * Explicit requirement. When omitted, resolved from the protected route catalog.
   */
  requirement?: AccessRequirement;
  roles?: string[];
  permissions?: string[];
  loginRedirect?: string;
  forbiddenRedirect?: string;
  /**
   * When true (default), also apply the catalog requirement for the current pathname.
   */
  useRouteCatalog?: boolean;
};

/**
 * Client-side route protection (UX). Redirects unauthenticated users to login and
 * unauthorized users to forbidden. Backend APIs remain authoritative.
 *
 * Unknown `/app/*` routes fail closed when the catalog is enabled.
 */
export function RouteProtection({
  children,
  requirement,
  roles,
  permissions,
  loginRedirect = '/login',
  forbiddenRedirect = '/forbidden',
  useRouteCatalog = true,
}: RouteProtectionProps) {
  const router = useRouter();
  const pathname = usePathname();
  const { status, isAuthenticated } = useSession();
  const { canAccess, status: authzStatus } = useAuthorization();

  let authorized = true;
  if (requirement || roles || permissions) {
    authorized = canAccess(requirement ?? { roles, permissions });
  } else if (useRouteCatalog) {
    const resolved = resolveRouteAccess(pathname);
    authorized =
      resolved.status === 'allow' ? canAccess(resolved.requirement) : false;
  }

  const authzReady = authzStatus === 'ready' || authzStatus === 'error' || !isAuthenticated;

  useEffect(() => {
    if (status === 'loading' || !authzReady) {
      return;
    }
    if (!isAuthenticated) {
      const next = pathname ? `?next=${encodeURIComponent(pathname)}` : '';
      router.replace(`${loginRedirect}${next}`);
      return;
    }
    if (!authorized) {
      router.replace(forbiddenRedirect);
    }
  }, [
    status,
    authzReady,
    isAuthenticated,
    authorized,
    router,
    loginRedirect,
    forbiddenRedirect,
    pathname,
  ]);

  if (status === 'loading' || (isAuthenticated && !authzReady)) {
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
