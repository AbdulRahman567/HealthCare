'use client';

import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  type ReactNode,
} from 'react';

import { hasAll, hasAny } from '@/features/authorization/lib/access';
import { selectAuthorization, selectRoles } from '@/features/authorization/store/authorization-slice';
import { useAppSelector } from '@/store/hooks';

type RoleContextValue = {
  roles: string[];
  status: 'idle' | 'loading' | 'ready' | 'error';
  hasRole: (role: string | string[]) => boolean;
  hasAllRoles: (role: string | string[]) => boolean;
};

const RoleContext = createContext<RoleContextValue | null>(null);

type RoleProviderProps = {
  children: ReactNode;
  /** Optional override for tests; defaults to Redux authorization state. */
  roles?: string[];
};

/**
 * Exposes backend-issued role codes for UX checks.
 */
export function RoleProvider({ children, roles: override }: RoleProviderProps) {
  const authz = useAppSelector(selectAuthorization);
  const storeRoles = useAppSelector(selectRoles);
  const roles = override ?? storeRoles;

  const hasRole = useCallback((role: string | string[]) => hasAny(roles, role), [roles]);

  const hasAllRoles = useCallback((role: string | string[]) => hasAll(roles, role), [roles]);

  const value = useMemo<RoleContextValue>(
    () => ({
      roles,
      status: authz.status,
      hasRole,
      hasAllRoles,
    }),
    [roles, authz.status, hasRole, hasAllRoles],
  );

  return <RoleContext.Provider value={value}>{children}</RoleContext.Provider>;
}

export function useRoleContext(): RoleContextValue {
  const context = useContext(RoleContext);
  if (!context) {
    throw new Error('useRoleContext must be used within RoleProvider');
  }
  return context;
}
