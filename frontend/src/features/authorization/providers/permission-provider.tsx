'use client';

import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  type ReactNode,
} from 'react';

import { hasAll, hasAny } from '@/features/authorization/lib/access';
import {
  selectPermissions,
  selectAuthorization,
} from '@/features/authorization/store/authorization-slice';
import { useAppSelector } from '@/store/hooks';

type PermissionContextValue = {
  permissions: string[];
  status: 'idle' | 'loading' | 'ready' | 'error';
  can: (permission: string | string[]) => boolean;
  canAll: (permission: string | string[]) => boolean;
};

const PermissionContext = createContext<PermissionContextValue | null>(null);

type PermissionProviderProps = {
  children: ReactNode;
  /** Optional override for tests; defaults to Redux authorization state. */
  permissions?: string[];
};

/**
 * Exposes backend-issued permission codes for UX checks (buttons, menus, pages).
 */
export function PermissionProvider({ children, permissions: override }: PermissionProviderProps) {
  const authz = useAppSelector(selectAuthorization);
  const storePermissions = useAppSelector(selectPermissions);
  const permissions = override ?? storePermissions;

  const can = useCallback(
    (permission: string | string[]) => hasAny(permissions, permission),
    [permissions],
  );

  const canAll = useCallback(
    (permission: string | string[]) => hasAll(permissions, permission),
    [permissions],
  );

  const value = useMemo<PermissionContextValue>(
    () => ({
      permissions,
      status: authz.status,
      can,
      canAll,
    }),
    [permissions, authz.status, can, canAll],
  );

  return <PermissionContext.Provider value={value}>{children}</PermissionContext.Provider>;
}

export function usePermissionContext(): PermissionContextValue {
  const context = useContext(PermissionContext);
  if (!context) {
    throw new Error('usePermissionContext must be used within PermissionProvider');
  }
  return context;
}
