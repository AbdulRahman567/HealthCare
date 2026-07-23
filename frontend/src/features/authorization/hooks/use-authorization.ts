'use client';

import { useMemo } from 'react';

import { usePermission } from '@/features/authorization/hooks/use-permission';
import { useRole } from '@/features/authorization/hooks/use-role';
import { matchesAccess } from '@/features/authorization/lib/access';
import type { AccessRequirement } from '@/features/authorization/types';
import { selectAuthorization } from '@/features/authorization/store/authorization-slice';
import { useAppSelector } from '@/store/hooks';

/**
 * Combined authorization hook (roles + permissions + requirement matcher).
 */
export function useAuthorization() {
  const authz = useAppSelector(selectAuthorization);
  const { roles, hasRole, hasAllRoles } = useRole();
  const { permissions, can, canAll } = usePermission();

  const canAccess = useMemo(
    () => (requirement?: AccessRequirement) => matchesAccess(roles, permissions, requirement),
    [roles, permissions],
  );

  return {
    status: authz.status,
    userId: authz.userId,
    tenantId: authz.tenantId,
    email: authz.email,
    roles,
    permissions,
    error: authz.error,
    hasRole,
    hasAllRoles,
    can,
    canAll,
    canAccess,
  };
}
