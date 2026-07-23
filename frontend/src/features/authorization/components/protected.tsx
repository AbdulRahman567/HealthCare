'use client';

import type { ReactNode } from 'react';

import { Can } from '@/features/authorization/components/can';
import type { AccessMode } from '@/features/authorization/types';

type ProtectedProps = {
  children: ReactNode;
  roles?: string[];
  permissions?: string[];
  mode?: AccessMode;
  /**
   * Optional fallback when unauthorized (default: hide).
   */
  fallback?: ReactNode;
};

/**
 * Protected component — wraps UI that should only render when authorized.
 * Prefer {@link Can} for inline permission-based rendering; use this for blocks/panels.
 */
export function Protected({ children, roles, permissions, mode, fallback }: ProtectedProps) {
  return (
    <Can roles={roles} permissions={permissions} mode={mode} fallback={fallback}>
      {children}
    </Can>
  );
}
