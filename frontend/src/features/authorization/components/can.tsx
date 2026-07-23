'use client';

import type { ReactNode } from 'react';

import { useAuthorization } from '@/features/authorization/hooks/use-authorization';
import type { AccessMode, AccessRequirement } from '@/features/authorization/types';

type CanProps = AccessRequirement & {
  children: ReactNode;
  /**
   * Rendered when the principal lacks access (defaults to nothing — hide unauthorized UI).
   */
  fallback?: ReactNode;
  mode?: AccessMode;
};

/**
 * Permission-based rendering. Hides unauthorized buttons/sections for UX only.
 */
export function Can({ children, fallback = null, roles, permissions, mode = 'any' }: CanProps) {
  const { canAccess } = useAuthorization();
  const allowed = canAccess({ roles, permissions, mode });
  return <>{allowed ? children : fallback}</>;
}
